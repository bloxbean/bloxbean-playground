package com.bloxbean.playground.tokenfaucet.distribution;

import com.bloxbean.playground.tokenfaucet.common.SignTxnRequest;
import com.bloxbean.playground.tokenfaucet.common.TxnBody;
import com.bloxbean.playground.tokenfaucet.distribution.model.TokenDistRequest;
import com.bloxbean.playground.tokenfaucet.distribution.model.TokenDistResult;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Controller("/faucet/{name}/distribution")
@Slf4j
public class TokenDistributionController {

    @Inject
    private TokenDistributionService tokenDistributionService;

    @Post("/")
    public TxnBody buildTxn(@PathVariable String name, @Body TokenDistRequest distRequest) {
        try {
            return tokenDistributionService.buildTxn(name, distRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Post("/transfer")
    public TokenDistResult assembleAndTransfer(@Body SignTxnRequest signRequest) {
        try {
            return tokenDistributionService.assembleAndTransfer(signRequest.reqId(), signRequest.walletWitnessHex());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
