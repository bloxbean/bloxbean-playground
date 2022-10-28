package com.bloxbean.playground.minter;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.cip.cip25.NFT;
import com.bloxbean.cardano.client.cip.cip25.NFTMetadata;
import com.bloxbean.cardano.client.coinselection.impl.LargestFirstUtxoSelectionStrategy;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.Output;
import com.bloxbean.cardano.client.function.TxBuilder;
import com.bloxbean.cardano.client.function.TxBuilderContext;
import com.bloxbean.cardano.client.transaction.TransactionSigner;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bloxbean.cardano.client.common.ADAConversionUtil.adaToLovelace;
import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static com.bloxbean.cardano.client.function.helper.AuxDataProviders.metadataProvider;
import static com.bloxbean.cardano.client.function.helper.BalanceTxBuilders.balanceTx;
import static com.bloxbean.cardano.client.function.helper.InputBuilders.createFromSender;
import static com.bloxbean.cardano.client.function.helper.MintCreators.mintCreator;
import static com.bloxbean.cardano.client.function.helper.OutputBuilders.createFromMintOutput;

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
            throws CborSerializationException, IOException, CborDeserializationException {

        if (quantity <= 0 && quantity > 3)
            throw new RuntimeException("Invalid quantity : " + quantity);

        //For Mint - Get policy id and create NFT, Metadata and MultiAsset
        Policy policy = policyProvider.getPolicy();
        String policyId = policy.getPolicyId();

        List<NFT> nfts = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            NFT nft = nftProvider.getRandomNft();
            nfts.add(nft);
        }

        NFTMetadata nftMetadata = NFTMetadata.create();
        nfts.forEach(nft -> nftMetadata.addNFT(policyId, nft));

        //Create multi assets
        MultiAsset multiAsset = new MultiAsset();
        multiAsset.setPolicyId(policyId);
        List<MultiAsset> multiAssetList = Collections.singletonList(multiAsset);
        nfts.forEach(nft -> {
            Asset asset1 = new Asset(nft.getName(), BigInteger.valueOf(1));
            multiAsset.getAssets().add(asset1);
        });

        //Calculate total token price
        BigInteger amountToTransfer = TOKEN_PRICE.multiply(BigInteger.valueOf(quantity));

        //Define expected outputs

        //Receiver -- Mint token
        //Alternatively, Output.builder() can be used here. But for multiple tokens, multiple Output objects are required.
        TransactionOutput mintOutput = TransactionOutput.builder()
                .address(mintingAddress)
                .value(new Value(BigInteger.ZERO, multiAssetList)) //actual coin (min ada req) will be calculated automatically
                .build();

        //Seller Output
        Output sellerOutput = Output.builder()
                .address(sellAddress)
                .assetName(LOVELACE)
                .qty(amountToTransfer)
                .build();

        //Define TxBuilder
        TxBuilder txBuilder = createFromMintOutput(mintOutput)
                        .and(sellerOutput.outputBuilder())
                        .buildInputs(createFromSender(mintingAddress, mintingAddress)) //Get required inputs
                        .andThen(mintCreator(policy.getPolicyScript(), multiAsset))    //Set mint attribute of tx
                        .andThen(metadataProvider(nftMetadata))                        //Set metadata
                        .andThen((context, txn) -> { //Setting ttl (Optional)
                            try {
                                long ttl = blockchainService.getBlockService().getLatestBlock().getValue().getSlot() + 20000;
                                txn.getBody().setTtl(ttl);
                            } catch (ApiException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .andThen(balanceTx(mintingAddress, 2));

        //setup TxBuilderContext
        TxBuilderContext txBuilderContext = TxBuilderContext.init(blockchainService.getUtxoSupplier(), blockchainService.getProtocolParamsSupplier());
        txBuilderContext.setUtxoSelectionStrategy(new LargestFirstUtxoSelectionStrategy(blockchainService.getUtxoSupplier())); //Optional, otherwise DefaultUtxoSelectionStrategy is used

        //Build transaction
        Transaction transaction = txBuilderContext.build(txBuilder);

        //Get transaction hex and store it in server-side (redis) for future reference
        String transactionHex = transaction.serializeToHex();
        String key = randomGenerator.getRandomRequestId();
        connection.sync().set(key, transactionHex);
        connection.sync().expire(key, Duration.ofSeconds(180));

        //clone txn and remove witnessset and auxiliary data. This will be sent to browser for signing
        Transaction cloneTransaciton = Transaction.deserialize(transaction.serialize());
        cloneTransaciton.setWitnessSet(null); //clear witness
        cloneTransaciton.setAuxiliaryData(null); //clear metadata
        String cloneTxnHex = cloneTransaciton.serializeToHex();

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
        DataItem witnessDI = CborSerializationUtil.deserialize(HexUtil.decodeHexString(walletWitnessHex));
        TransactionWitnessSet walletWitnessSet = TransactionWitnessSet.deserialize((Map) witnessDI);

        //Set witness to original (cached) transaction
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
