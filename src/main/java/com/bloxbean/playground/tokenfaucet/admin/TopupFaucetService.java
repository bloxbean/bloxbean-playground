package com.bloxbean.playground.tokenfaucet.admin;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.bloxbean.cardano.client.backend.model.Result;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.Output;
import com.bloxbean.cardano.client.function.TxBuilder;
import com.bloxbean.cardano.client.function.TxBuilderContext;
import com.bloxbean.cardano.client.function.TxOutputBuilder;
import com.bloxbean.cardano.client.function.helper.ChangeOutputAdjustments;
import com.bloxbean.cardano.client.function.helper.FeeCalculators;
import com.bloxbean.cardano.client.function.helper.InputBuilders;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.playground.common.BlockchainService;
import com.bloxbean.playground.common.RandomGenerator;
import com.bloxbean.playground.tokenfaucet.admin.model.AddressAmount;
import com.bloxbean.playground.tokenfaucet.admin.model.AddressInfo;
import com.bloxbean.playground.tokenfaucet.admin.model.TopupResult;
import com.bloxbean.playground.tokenfaucet.admin.model.TopupTxnRequest;
import com.bloxbean.playground.tokenfaucet.common.TxnBody;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class TopupFaucetService {

    @Inject
    private BlockchainService blockchainService;

    @Inject
    private FaucetService faucetService;

    @Inject
    private RandomGenerator randomGenerator;

    @Inject
    private StatefulRedisConnection<String, String> connection;

    public TxnBody buildTxn(String name, TopupTxnRequest topupTxnRequest)
            throws AddressExcepion, CborSerializationException, CborException, CborDeserializationException {
        String sender = topupTxnRequest.sender();

        log.info("Sender address: " + sender);

        List<AddressInfo> addressInfoList = faucetService.getAddresses(name);

        BigInteger qtyPerAddress = topupTxnRequest.qty().divide(BigInteger.valueOf(addressInfoList.size()));

        log.info("Qty per address : " + qtyPerAddress);

        TxOutputBuilder txOutputBuilder = (txBuilderContext, list) -> {
        };
        for (AddressInfo addressInfo : addressInfoList) {
            Output output = Output.builder()
                    .address(addressInfo.address())
                    .policyId(topupTxnRequest.policyId())
                    .assetName(topupTxnRequest.assetName())
                    .qty(qtyPerAddress).build();
            txOutputBuilder = txOutputBuilder.and(output.outputBuilder());
        }

        TxBuilder txBuilder = txOutputBuilder
                .buildInputs(InputBuilders.createFromSender(topupTxnRequest.sender(), topupTxnRequest.sender()))
                .andThen(FeeCalculators.feeCalculator(topupTxnRequest.sender(), 1))
                .andThen(ChangeOutputAdjustments.adjustChangeOutput(topupTxnRequest.sender()));

        TxBuilderContext txBuilderContext = new TxBuilderContext(blockchainService.getBackendService());
       // txBuilderContext.setUtxoSelectionStrategy(new LargestFirstUtxoSelectionStrategy(blockchainService.getUtxoService()));
        Transaction transaction = txBuilderContext.build(txBuilder);

        System.out.println(transaction);
        TransactionBody txnBody = transaction.getBody();

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

    public TopupResult assembleAndTransfer(String reqId, String walletWitnessHex)
            throws CborSerializationException, CborDeserializationException, CborException, ApiException {
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
            return new TopupResult(result.getValue(), addressAmounts);
        } else {
            throw new RuntimeException("Transaction failed. Error message: " + result.getResponse());
        }
    }
}
