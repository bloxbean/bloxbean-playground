package com.bloxbean.playground.tokenfaucet.admin;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.playground.tokenfaucet.admin.model.TopupResult;
import com.bloxbean.playground.tokenfaucet.admin.model.TopupTxnRequest;
import com.bloxbean.playground.tokenfaucet.common.SignTxnRequest;
import com.bloxbean.playground.tokenfaucet.common.TxnBody;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Controller("/faucet/{name}/topup")
@Slf4j
public class TopupFaucetController {

    @Inject
    private TopupFaucetService topupFaucetService;

    @Post("/")
    public TxnBody buildTxn(@PathVariable String name, @Body TopupTxnRequest topupTxnRequest) {
        try {
            return topupFaucetService.buildTxn(name, topupTxnRequest);
        } catch (AddressExcepion | CborSerializationException | CborException | CborDeserializationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Post("/transfer")
    public TopupResult assembleAndTransfer(@Body SignTxnRequest signTxnRequest) {
        try {
            return topupFaucetService.assembleAndTransfer(signTxnRequest.reqId(), signTxnRequest.walletWitnessHex());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
