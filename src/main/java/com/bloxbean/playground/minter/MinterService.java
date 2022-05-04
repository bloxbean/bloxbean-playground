package com.bloxbean.playground.minter;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.cip.cip25.NFT;
import com.bloxbean.cardano.client.cip.cip25.NFTMetadata;
import com.bloxbean.cardano.client.coinselection.UtxoSelectionStrategy;
import com.bloxbean.cardano.client.coinselection.UtxoSelector;
import com.bloxbean.cardano.client.coinselection.impl.DefaultUtxoSelectionStrategyImpl;
import com.bloxbean.cardano.client.coinselection.impl.DefaultUtxoSelector;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.TransactionSigner;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.util.CborSerializationUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.client.util.JsonUtil;
import com.bloxbean.playground.common.BlockchainService;
import com.bloxbean.playground.common.RandomGenerator;
import com.bloxbean.playground.minter.model.MintingResult;
import com.bloxbean.playground.minter.model.MintingTxnBody;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static com.bloxbean.cardano.client.common.ADAConversionUtil.adaToLovelace;
import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static com.bloxbean.playground.common.UtxoUtil.copyUtxoValuesToOutput;

@Singleton
@Slf4j
public class MinterService {

    @Inject
    private BlockchainService blockchainService;

    @Inject
    private StatefulRedisConnection<String, String> connection;

    @Inject
    private RandomGenerator randomGenerator;

    @Inject
    private PolicyProvider policyProvider;

    @Inject
    private NFTProvider nftProvider;

    private final BigInteger TOKEN_PRICE = adaToLovelace(15);

    private final String sellAddress = "addr_test1qrynkm9vzsl7vrufzn6y4zvl2v55x0xwc02nwg00x59qlkxtsu6q93e6mrernam0k4vmkn3melezkvgtq84d608zqhnsn48axp";

    public MintingTxnBody buildMintTxnBody(String mintingAddress, String receiver, int quantity)
            throws ApiException, CborSerializationException, AddressExcepion, CborException, IOException, CborDeserializationException {

        if (quantity <= 0 && quantity > 3)
            throw new RuntimeException("Invalid quantity : " + quantity);

        //For Mint
        Policy policy = policyProvider.getPolicy();
        String policyId = policy.getPolicyId();

        List<NFT> nfts = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            NFT nft = nftProvider.getRandomNft();
            nfts.add(nft);
        }

        NFTMetadata nftMetadata = NFTMetadata.create();

        nfts.forEach(nft -> nftMetadata.addNFT(policyId, nft));
        AuxiliaryData auxiliaryData = AuxiliaryData.builder()
                .metadata(nftMetadata)
                .build();

        MultiAsset multiAsset = new MultiAsset();
        multiAsset.setPolicyId(policyId);
        List<MultiAsset> multiAssetList = Collections.singletonList(multiAsset);

        nfts.forEach(nft -> {
            Asset asset1 = new Asset(nft.getName(), BigInteger.valueOf(1));
            multiAsset.getAssets().add(asset1);
        });

        long ttl = blockchainService.getBlockService().getLatestBlock().getValue().getSlot() + 20000;
        TransactionDetailsParams detailsParams = TransactionDetailsParams.builder().ttl(ttl).build();

        //Total token price
        BigInteger amountToTransfer = TOKEN_PRICE.multiply(BigInteger.valueOf(quantity));

        //Find totalAmount approx -- //TODO Min ada calculation
        BigInteger minAdaInMintOutput = adaToLovelace(2);
        BigInteger estimatedFeeAndMinAdaForChange = adaToLovelace(4); //Just an estimation to get utxos. Actual fee will be calculated later
        BigInteger totalAmount = amountToTransfer.add(minAdaInMintOutput).add(estimatedFeeAndMinAdaForChange);

        //Receiver -- Mint token
        TransactionOutput mintOutput = TransactionOutput.builder()
                .address(mintingAddress)
                .value(new Value(minAdaInMintOutput, multiAssetList))
                .build();

        //Seller Output
        TransactionOutput sellerOutput = TransactionOutput.builder()
                .address(sellAddress)
                .value(new Value(amountToTransfer, new ArrayList<>()))
                .build();

        //Change Output
        TransactionOutput changeOutput = TransactionOutput.builder()
                .address(mintingAddress)
                .value(new Value(BigInteger.ZERO, new ArrayList<>()))
                .build();


        //Build Inputs
        //Find required utxos
        UtxoSelector utxoSelector = new DefaultUtxoSelector(new DefaultUtxoSupplier(blockchainService.getUtxoService()));

        //Find utxo with only lovelace
        Optional<Utxo> optionalUtxo = utxoSelector.findFirst(mintingAddress, (ux) -> ux.getAmount().size() == 1 && ux.getAmount().stream()
                .filter(amount -> amount.getUnit().equals(LOVELACE)
                        && amount.getQuantity().compareTo(totalAmount) == 1)
                .findFirst().isPresent());

        if (optionalUtxo.isEmpty()) { //If no utxo with only LOVELACE found, check utxo with multiasset
            optionalUtxo = utxoSelector.findFirst(mintingAddress, (ux) -> ux.getAmount().stream()
                    .filter(amount -> amount.getUnit().equals(LOVELACE)
                            && amount.getQuantity().compareTo(totalAmount) == 1)
                    .findFirst().isPresent());
        }

        List<Utxo> utxos = new ArrayList<>();

        if (optionalUtxo.isEmpty()) {
            UtxoSelectionStrategy utxoSelectionStrategy =
                    new DefaultUtxoSelectionStrategyImpl(new DefaultUtxoSupplier(blockchainService.getUtxoService()));
            utxos = utxoSelectionStrategy.selectUtxos(mintingAddress, LOVELACE, totalAmount, Collections.EMPTY_SET);
        } else {
            utxos.add(optionalUtxo.get());
        }

        if (utxos == null || utxos.size() == 0)
            throw new RuntimeException("Utxo with amount " + amountToTransfer + " not found");

        //Inputs
        List<TransactionInput> inputs = new ArrayList<>();
        for (Utxo utxo : utxos) {
            TransactionInput input = TransactionInput.builder()
                    .transactionId(utxo.getTxHash())
                    .index(utxo.getOutputIndex()).build();
            inputs.add(input);

            //Update change output
            copyUtxoValuesToOutput(changeOutput, utxo);
        }

        //Sort inputs
        inputs.sort(new Comparator<TransactionInput>() {
            @Override
            public int compare(TransactionInput o1, TransactionInput o2) {
                return (o1.getTransactionId() + "#" + o1.getIndex()).compareTo(o2.getTransactionId() + "#" + o2.getIndex());
            }
        });

        //Deduct token price + mintOutput amount
        BigInteger remainingAmount = changeOutput.getValue().getCoin()
                .subtract(amountToTransfer)
                .subtract(mintOutput.getValue().getCoin());

        changeOutput.getValue().setCoin(remainingAmount);
        List<TransactionOutput> outputs = Arrays.asList(mintOutput, sellerOutput, changeOutput);

        TransactionBody body = TransactionBody.builder()
                .inputs(inputs)
                .outputs(outputs)
                .fee(BigInteger.valueOf(170000)) //dummy fee
                .ttl(detailsParams.getTtl())
                //.validityStartInterval(detailsParams.getValidityStartInterval())
                .mint(multiAssetList)
                .auxiliaryDataHash(auxiliaryData.getAuxiliaryDataHash())
                .build();

        TransactionWitnessSet transactionWitnessSet = new TransactionWitnessSet();
        transactionWitnessSet.getNativeScripts().add(policy.getPolicyScript());

        Transaction transaction = Transaction.builder()
                .body(body)
                .witnessSet(transactionWitnessSet)
                .auxiliaryData(auxiliaryData)
                .build();

        //sign with a dummy account to get actual txn size
        Account dummyAccount = new Account(Networks.testnet()); //TODO -- check if network required
        Transaction signedTxn = dummyAccount.sign(transaction);

        for (SecretKey sk : policy.getPolicyKeys()) {
            signedTxn = TransactionSigner.INSTANCE.sign(signedTxn, sk);
        }

        //Calculate fee
        BigInteger fee = blockchainService.getFeeCalculationService().calculateFee(signedTxn);

        transaction.getBody().setFee(fee);
        BigInteger newChangeAmt = changeOutput.getValue().getCoin().subtract(fee);
        changeOutput.getValue().setCoin(newChangeAmt);

        String txnBodyHex = HexUtil.encodeHexString(CborSerializationUtil.serialize(body.serialize()));
        String transactionHex = transaction.serializeToHex();

        //clone
        Transaction cloneTransaciton = Transaction.deserialize(transaction.serialize());
        cloneTransaciton.setWitnessSet(null); //clear witness
        cloneTransaciton.setAuxiliaryData(null); //clear metadata
        String cloneTxnHex = cloneTransaciton.serializeToHex();

        //Get the output. This txnHex is sent to Nami for signing
        System.out.println(txnBodyHex);

        String key = randomGenerator.getRandomRequestId();
        connection.sync().set(key, transactionHex);
        connection.sync().expire(key, Duration.ofSeconds(180));

        System.out.println(JsonUtil.getPrettyJson(transaction));
        return new MintingTxnBody(key, cloneTxnHex);
    }

    public MintingResult assembleAndMint(String reqId, String walletWitnessHex)
            throws CborSerializationException, CborDeserializationException, CborException, ApiException, IOException {
        String txnHex = connection.sync().getdel(reqId);
        //TODO -- throw error if txnHex not found timeout

        //De-serialize original txn hash
        Transaction transaction = Transaction.deserialize(HexUtil.decodeHexString(txnHex));

        //Decode Nami's witness cbor
        List<DataItem> dis = CborDecoder.decode(HexUtil.decodeHexString(walletWitnessHex));
        co.nstant.in.cbor.model.Map witnessMap = (Map) dis.get(0);
        TransactionWitnessSet walletWitnessSet = TransactionWitnessSet.deserialize(witnessMap);

        if (transaction.getWitnessSet() == null) {
            transaction.setWitnessSet(new TransactionWitnessSet());
        }

        if (transaction.getWitnessSet().getVkeyWitnesses() == null)
            transaction.getWitnessSet().setVkeyWitnesses(new ArrayList<>());

        transaction.getWitnessSet().getVkeyWitnesses().addAll(walletWitnessSet.getVkeyWitnesses());

        Policy policy = policyProvider.getPolicy();

        Transaction signedTxn = TransactionSigner.INSTANCE.sign(transaction, policy.getPolicyKeys().get(0));

        Result<String> result = blockchainService.getTransactionService().submitTransaction(signedTxn.serialize());

        if (result.isSuccessful()) {
            System.out.println(result);
            NFTMetadata nftMetadata = NFTMetadata.create(signedTxn.getAuxiliaryData().getMetadata().serialize());
            MintingResult mintingResult = new MintingResult(true, result.getValue(), nftMetadata.toJson());

            return mintingResult;
        } else {
            throw new RuntimeException("Transaction failed. Error message: " + result.getResponse());
        }
    }

}
