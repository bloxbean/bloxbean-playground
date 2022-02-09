package com.bloxbean.playground.common;

import com.bloxbean.cardano.client.backend.api.*;
import com.bloxbean.cardano.client.backend.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.backend.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.backend.api.helper.UtxoTransactionBuilder;
import com.bloxbean.cardano.client.backend.factory.BackendFactory;
import com.bloxbean.cardano.client.backend.impl.blockfrost.common.Constants;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class BlockchainService {

//    @Value("${bf_project_id}")
//    private String bfProjectId;

    FeeCalculationService feeCalculationService;
    TransactionHelperService transactionHelperService;
    TransactionService transactionService;
    BlockService blockService;
    AssetService assetService;
    NetworkInfoService networkInfoService;
    UtxoService utxoService;
    EpochService epochService;
    UtxoTransactionBuilder utxoTransactionBuilder;

    public BlockchainService(@Value("${bf_project_id}")String bfProjectId) {
        BackendService backendService =
                BackendFactory.getBlockfrostBackendService(Constants.BLOCKFROST_TESTNET_URL, bfProjectId);

        feeCalculationService = backendService.getFeeCalculationService();
        transactionHelperService = backendService.getTransactionHelperService();
        transactionService = backendService.getTransactionService();
        blockService = backendService.getBlockService();
        assetService = backendService.getAssetService();
        utxoService = backendService.getUtxoService();
        networkInfoService = backendService.getNetworkInfoService();
        epochService = backendService.getEpochService();
        utxoTransactionBuilder = backendService.getUtxoTransactionBuilder();
    }

    public FeeCalculationService getFeeCalculationService() {
        return feeCalculationService;
    }

    public TransactionHelperService getTransactionHelperService() {
        return transactionHelperService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public BlockService getBlockService() {
        return blockService;
    }

    public AssetService getAssetService() {
        return assetService;
    }

    public NetworkInfoService getNetworkInfoService() {
        return networkInfoService;
    }

    public UtxoService getUtxoService() {
        return utxoService;
    }

    public EpochService getEpochService() {
        return epochService;
    }

    public UtxoTransactionBuilder getUtxoTransactionBuilder() {
        return utxoTransactionBuilder;
    }
}
