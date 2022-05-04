package com.bloxbean.playground.nftaccess;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.coinselection.UtxoSelectionStrategy;
import com.bloxbean.cardano.client.coinselection.impl.DefaultUtxoSelectionStrategyImpl;
import com.bloxbean.playground.common.BlockchainService;
import com.bloxbean.playground.nftaccess.model.NFTSession;
import com.bloxbean.playground.nftaccess.model.NFTSessionCreateRequest;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class NFTAccessService {

    @Inject
    private BlockchainService blockchainService;

    @Inject
    private SessionStorage sessionStorage;

    @Value("${admin_token_unit}")
    private String adminTokenUnit;

    public NFTSession createSession(NFTSessionCreateRequest sessionRequest) {
        if (!isAdminTokenAvailable(sessionRequest.address())) {
            throw new RuntimeException("Admin session creation failed. The address doesn't have any admin token.");
        }

        return sessionStorage.createSession(sessionRequest.address(), "admin");
    }

    public Optional<NFTSession> getSession(String sessionId) {
        return sessionStorage.getSession(sessionId);
    }

    private boolean isAdminTokenAvailable(String address) {

        UtxoSelectionStrategy utxoSelectionStrategy = new DefaultUtxoSelectionStrategyImpl(new DefaultUtxoSupplier(blockchainService.getUtxoService()));
        try {
            List<Utxo> utxoList = utxoSelectionStrategy.selectUtxos(address, adminTokenUnit, BigInteger.valueOf(1), Collections.EMPTY_SET);
            if (utxoList.size() > 0)
                return true;
            else
                return false;
        } catch (ApiException apiException) {
            return false;
        }
    }
}
