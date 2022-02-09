package com.bloxbean.playground.minter;

import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class PolicyProvider {

    private ObjectMapper objectMapper = new ObjectMapper();
    private String policyFileName = "policy.json";
    private Policy policy;

    public Policy getPolicy() throws IOException {
        if (policy == null) {
            InputStream is = getClass().getClassLoader().getResourceAsStream(policyFileName);
            policy = objectMapper.readValue(is, Policy.class);
        }

        return policy;
    }
}
