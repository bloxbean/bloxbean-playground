package com.bloxbean.playground.nftaccess;

import com.bloxbean.playground.nftaccess.model.NFTSession;
import com.bloxbean.playground.nftaccess.model.NFTSessionCreateRequest;
import com.bloxbean.playground.nftaccess.model.ValidateSessionRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

import java.util.Optional;

@Controller("/auth")
public class NFTAccessController {

    @Inject
    private NFTAccessService nftAccessService;

    @Post("/session")
    public NFTSession createSession(NFTSessionCreateRequest sessionRequest) {
        return nftAccessService.createSession(sessionRequest);
    }

    @Post("/session/validate")
    public NFTSession validateSession(ValidateSessionRequest sessionRequest) {
        Optional<NFTSession> nftSession =
                nftAccessService.getSession(sessionRequest.sessionId());

        return nftSession.filter(ns -> ns.address().equals(sessionRequest.address()))
                .orElseThrow();
    }

}
