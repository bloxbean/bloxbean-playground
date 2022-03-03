package com.bloxbean.playground.common;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class Configuration {

    public Network getNetwork() {
        return Networks.testnet();
    }
}
