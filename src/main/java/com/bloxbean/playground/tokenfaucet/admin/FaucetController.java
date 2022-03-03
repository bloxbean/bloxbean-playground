package com.bloxbean.playground.tokenfaucet.admin;

import com.bloxbean.playground.nftaccess.SessionStorage;
import com.bloxbean.playground.tokenfaucet.admin.model.AddressInfo;
import com.bloxbean.playground.tokenfaucet.admin.model.FaucetAddressRequest;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Controller("/faucet")
@Slf4j
public class FaucetController {

    @Inject
    private FaucetService faucetService;

    @Inject
    private SessionStorage sessionStorage;

    @Post("{name}")
    public void setUpNewFaucet(@Header(name = "SessionId") String sessionId, @PathVariable String name) throws Exception {
        sessionStorage.getSession(sessionId).orElseThrow(() -> {
            return new RuntimeException("Invalid session Id");
        });

        faucetService.createFaucet(name);
    }

    @Post("{name}/addresses")
    public List<AddressInfo> createFaucetAddresses(@Header(name = "SessionId") String sessionId, @PathVariable String name, @Body FaucetAddressRequest faucetAddressRequest) {
        sessionStorage.getSession(sessionId).orElseThrow(() -> {
            return new RuntimeException("Invalid session Id");
        });

        return faucetService.generateFaucetAddresses(name, faucetAddressRequest, false);
    }

    @Get("{name}/addresses")
    public List<AddressInfo> getAddresses(@Header(name = "SessionId") String sessionId, @PathVariable String name) {
        sessionStorage.getSession(sessionId).orElseThrow(() -> {
            return new RuntimeException("Invalid session Id");
        });

        return faucetService.getAddresses(name);
    }

    @Post("{name}/addresses/refresh")
    public List<AddressInfo> refreshFaucetAddresses(@Header(name = "SessionId") String sessionId, @PathVariable String name, @Body FaucetAddressRequest faucetAddressRequest) {
        sessionStorage.getSession(sessionId).orElseThrow(() -> {
            return new RuntimeException("Invalid session Id");
        });

        return faucetService.generateFaucetAddresses(name, faucetAddressRequest, true);
    }

    //--Transfer tokens to addresses
}
