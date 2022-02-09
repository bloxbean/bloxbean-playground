package com.bloxbean.playground.common;

import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class RandomGenerator {
    private final static String REQ_ID = "req_id_";

    public String getRandomRequestId() {
        return REQ_ID + UUID.randomUUID().toString();
    }
}
