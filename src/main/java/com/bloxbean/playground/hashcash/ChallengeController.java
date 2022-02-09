package com.bloxbean.playground.hashcash;

import com.bloxbean.playground.hashcash.model.Challenge;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

//import org.springframework.util.StringUtils;

@Slf4j
@Controller("/pow")
public class ChallengeController {

    @Inject
    private HashCacheService hashCacheService;

    @Inject
    private HashCashService hashCashService;

    @Get("challenge")
    public Challenge getChallenge(HttpRequest<?> request) {
        String clientIp = RequestUtil.getClientIpAddress(request);
        if (log.isDebugEnabled())
            log.debug("Client IP >> " + clientIp);

        String origin = request.getHeaders().get("Origin");
        if (StringUtils.isEmpty(origin)) {
            origin = request.getHeaders().get("Referer");
        }

        return hashCacheService.getChallenge(clientIp, true);
    }

    /*
    @PostMapping(value = "/register", consumes = MediaType.TEXT_PLAIN_VALUE)
    public TopupResult register(@RequestBody String hashcash) throws InvalidMessageException, AlreadyUsedException {
        String account = null;
        String network = null;
        try {
            HashCashService.Extension extension = hashCashService.validate(hashcash);
            if (extension == null || extension.getAccount() == null) {
                return TopupResult.builder().error("Validation failed").build();
            }
            account = extension.getAccount();
            network = extension.getNetwork();

            if (StringUtils.isEmpty(network))
                network = ConfigHelper.MASTERY_NETWORK;

        } catch (AlreadyUsedException e) {
            log.error("Error in validation", e);
            return TopupResult.builder().error("Hashcash message has alread been used.").build();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error in validation", e);
            return TopupResult.builder().error("Invalid hashcash message.").build();
        } catch (InvalidMessageException e) {
            log.error("Error in validation", e);
            return TopupResult.builder().error("Invalid hashcash message.").build();
        }

        String operatorKey = ConfigHelper.getOperatorKey(network);
        String nodeUrl = ConfigHelper.getNodeUrl(network);

        if (StringUtils.isEmpty(nodeUrl)) {
            return TopupResult.builder().error("Node url not found").build();
        }

        if (StringUtils.isEmpty(operatorKey)) {
            return TopupResult.builder().error("Operator's key not found").build();
        }

        String faucetContractAddress = networkService.getFaucetContractAddress(network);
        if (faucetContractAddress == null) {
            faucetContractAddress = ConfigHelper.getCustomFaucetContractAddress();
        }

        if (StringUtils.isEmpty(faucetContractAddress)) {
            return TopupResult.builder().error("Faucet contract address could not be found for the network : " + network).build();
        }

        Log defaultLog = new DefaultLog();

        if (log.isDebugEnabled()) {
            log.debug("Node url : " + nodeUrl);
            log.debug("Faucet contract address :" + faucetContractAddress);
        }

        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(nodeUrl, defaultLog);
        try {

            String encodedMethodCall = LocalAvmNode.encodeMethodCall("registerAddress", new Object[]{new Address(HexUtil.hexStringToBytes(account))});
            log.info("Encoded method call data: " + encodedMethodCall);

            String txHash = remoteAVMNode.sendRawTransaction(faucetContractAddress, operatorKey, encodedMethodCall, BigInteger.ZERO, defaultGas, defaultGasPrice);

            if (txHash != null) {
                return TopupResult.builder().txHash(txHash).build();
            } else {
                return TopupResult.builder().error("Txn hash is null").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in transaction", e);
            return TopupResult.builder().error("Error in topup: " + e.getMessage()).build();
        }
    }*/

}
