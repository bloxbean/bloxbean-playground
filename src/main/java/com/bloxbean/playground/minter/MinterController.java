package com.bloxbean.playground.minter;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.playground.minter.model.MintingResult;
import com.bloxbean.playground.minter.model.MintingTxnBody;
import com.bloxbean.playground.minter.model.MintingTxnRequest;
import com.bloxbean.playground.minter.model.SignMintRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

import java.io.IOException;

@Controller("/minter")
public class MinterController {

    @Inject
    private MinterService minterService;

    @Get("/")
    public String hello() {
        return "hello minter";
    }

    @Post("tx-body")
    public MintingTxnBody buildMintTransaction(@Body MintingTxnRequest mintingRequest)
            throws ApiException, CborSerializationException, CborException, AddressExcepion, IOException, CborDeserializationException {
        return minterService.buildMintTxnBody(mintingRequest.address(), mintingRequest.address(), mintingRequest.quantity());
    }

    @Post("mint")
    public MintingResult signAndMint(@Body SignMintRequest mintRequest)
            throws CborException, CborDeserializationException, CborSerializationException, ApiException, IOException {
        return minterService.assembleAndMint(mintRequest.reqId(), mintRequest.walletWitnessHex());
    }
}
