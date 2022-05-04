package com.bloxbean.playground.tokenfaucet.distribution;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.exception.ApiRuntimeException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.DefaultProtocolParamsSupplier;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.coinselection.UtxoSelectionStrategy;
import com.bloxbean.cardano.client.coinselection.impl.DefaultUtxoSelectionStrategyImpl;
import com.bloxbean.cardano.client.common.MinAdaCalculator;
import com.bloxbean.cardano.client.function.Output;
import com.bloxbean.cardano.client.function.TxBuilder;
import com.bloxbean.cardano.client.function.TxBuilderContext;
import com.bloxbean.cardano.client.function.helper.ChangeOutputAdjustments;
import com.bloxbean.cardano.client.function.helper.FeeCalculators;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.util.CborSerializationUtil;
import com.bloxbean.cardano.client.util.AssetUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.playground.common.BlockchainService;
import com.bloxbean.playground.common.Configuration;
import com.bloxbean.playground.common.RandomGenerator;
import com.bloxbean.playground.tokenfaucet.admin.FaucetStorage;
import com.bloxbean.playground.tokenfaucet.admin.model.AddressAmount;
import com.bloxbean.playground.tokenfaucet.common.TxnBody;
import com.bloxbean.playground.tokenfaucet.distribution.model.TokenDistRequest;
import com.bloxbean.playground.tokenfaucet.distribution.model.TokenDistResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static com.bloxbean.cardano.client.function.helper.InputBuilders.createFromSender;
import static com.bloxbean.cardano.client.function.helper.InputBuilders.createFromUtxos;
import static com.bloxbean.cardano.client.function.helper.SignerProviders.signerFrom;

@Controller("/faucet/{faucetName}/distribute")
@Slf4j
public class TokenDistributionService {

    @Inject
    private BlockchainService blockchainService;

    @Inject
    private Configuration configuration;

    @Inject
    private FaucetAddressLockService faucetAddressLockService;

    @Inject
    private FaucetStorage faucetStorage;

    @Inject
    private RandomGenerator randomGenerator;

    @Inject
    private StatefulRedisConnection<String, String> connection;

    public TxnBody buildTxn(String faucetName, TokenDistRequest distRequest) throws Exception {
        if (!faucetStorage.isAvailable(faucetName))
            throw new IllegalArgumentException("Invalid faucet name : " + faucetName);

        //Get an random available faucet address and lock it for this user
        Account faucetAccount = faucetAddressLockService.getAccount(faucetName);
        String faucetAddress = faucetAccount.baseAddress();

        String receiver = distRequest.receiver();

        log.info("Receiver address: " + receiver);

        Output receiverOuput = Output.builder()
                .address(receiver)
                .policyId(distRequest.policyId())
                .assetName(HexUtil.encodeHexString(distRequest.assetName().getBytes(StandardCharsets.UTF_8), true))
                .qty(distRequest.qty()).build();

        String unit = AssetUtil.getUnit(distRequest.policyId(), Asset.builder().name(distRequest.assetName()).build());
        UtxoSelectionStrategy utxoSelectionStrategy =
                new DefaultUtxoSelectionStrategyImpl(new DefaultUtxoSupplier(blockchainService.getUtxoService()));
        List<Utxo> faucetUtxos = utxoSelectionStrategy.selectUtxos(faucetAddress, unit, distRequest.qty(), Collections.EMPTY_SET);

        //For receiver output
        TxBuilder receiverOutputTxBuilder = receiverOuput.outputBuilder()
//                .buildInputs(createFromSender(faucetAddress, faucetAddress));
                .buildInputs(createFromUtxos(faucetUtxos, faucetAddress));

        Transaction txn = TxBuilderContext.init(new DefaultUtxoSupplier(blockchainService.getUtxoService()),
                new DefaultProtocolParamsSupplier(blockchainService.getEpochService()))
                .build(receiverOutputTxBuilder);

        BigInteger refundLovelace = getMinAdaForSingleTokenTransfer(distRequest.policyId(), distRequest.assetName());
        Output faucetOutput = Output.builder()
                .address(faucetAddress)
                .assetName(LOVELACE)
                .qty(refundLovelace)
                .build();

        TxBuilder faucetOutputTxBuilder = faucetOutput.outputBuilder()
                .buildInputs(createFromSender(distRequest.receiver(), distRequest.receiver()));

        TxBuilder txBuilder = receiverOutputTxBuilder
                .andThen(faucetOutputTxBuilder)
                .andThen((txBuilderContext, transaction) -> {
                    //merge outputs to same address
                    var mergedOutput = transaction.getBody().getOutputs().stream()
                            .collect(Collectors.groupingBy(transactionOutput -> transactionOutput.getAddress()))
                            .values()
                            .stream()
                            .map(outputs -> {
                                Value value = outputs.stream().map(o -> o.getValue())
                                        .reduce(Value.builder().coin(BigInteger.ZERO).build(), Value::plus);
                                return new TransactionOutput(outputs.get(0).getAddress(), value);
                            }).collect(Collectors.toList());

                    transaction.getBody().getOutputs().clear();
                    transaction.getBody().getOutputs().addAll(mergedOutput);
                })
                .andThen(FeeCalculators.feeCalculator(distRequest.receiver(), 2))
                .andThen(ChangeOutputAdjustments.adjustChangeOutput(distRequest.receiver(), 2));


        TxBuilderContext txBuilderContext = new TxBuilderContext(new DefaultUtxoSupplier(blockchainService.getUtxoService()),
                new DefaultProtocolParamsSupplier(blockchainService.getEpochService()));
//        txBuilderContext.setUtxoSelectionStrategy(new LargestFirstUtxoSelectionStrategy(blockchainService.getUtxoService()));
        Transaction transaction = txBuilderContext.build(txBuilder);

        transaction = signerFrom(faucetAccount).sign(transaction);

        System.out.println(transaction);

        //clone
        Transaction cloneTransaciton = Transaction.deserialize(transaction.serialize());
        cloneTransaciton.setWitnessSet(null); //clear witness
        cloneTransaciton.setAuxiliaryData(null); //clear metadata
        String cloneTxnHex = cloneTransaciton.serializeToHex();

        String key = randomGenerator.getRandomRequestId();
        connection.sync().set(key, transaction.serializeToHex());
        connection.sync().expire(key, Duration.ofSeconds(180));

        TxnBody topupTxnBody = new TxnBody(key, cloneTxnHex);

        return topupTxnBody;

    }

    public TokenDistResult assembleAndTransfer(String reqId, String walletWitnessHex) throws Exception {
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

        Result<String> result = blockchainService.getTransactionService().submitTransaction(transaction.serialize());

        List<AddressAmount> addressAmounts = transaction.getBody().getOutputs().stream()
                .map(to -> new AddressAmount(to.getAddress(), to.getValue()))
                .collect(Collectors.toList());

        if (result.isSuccessful()) {
            return new TokenDistResult(result.getValue());
        } else {
            throw new RuntimeException("Transaction failed. Error message: " + result.getResponse());
        }
    }

    private BigInteger getMinAdaForSingleTokenTransfer(String policyId, String assetName) {
        Asset asset = Asset.builder().name(assetName).build();

        MultiAsset ma = AssetUtil.getMultiAssetFromUnitAndAmount(AssetUtil.getUnit(policyId, asset), BigInteger.valueOf(1000));

        try {
            MinAdaCalculator minAdaCalculator =
                    new MinAdaCalculator(blockchainService.getEpochService().getProtocolParameters().getValue());

            return minAdaCalculator.calculateMinAda(List.of(ma), false);
        } catch (ApiException e) {
            throw new ApiRuntimeException("Error getting protocol parameters: ", e);
        }
    }
}
