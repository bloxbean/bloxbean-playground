package com.bloxbean.playground.tokenfaucet.admin;

import com.bloxbean.cardano.client.util.JsonUtil;
import com.bloxbean.playground.common.JsonHelper;
import com.bloxbean.playground.tokenfaucet.admin.model.AddressInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class FaucetStorage {
    private final static String ADDRESS_MAP = "address_map";
    private final static String MNEMONIC_MAP = "mnemonic_map";

    @Inject
    private StatefulRedisConnection<String, String> connection;

    @Inject
    private JsonHelper jsonHelper;

    public void storeMnemonic(String name, String mnemonic) {
        //mnemonicMap.put(name, mnemonic);
        connection.sync().hset(MNEMONIC_MAP, name, mnemonic);
    }

    public boolean isAvailable(String name) {
        return connection.sync().hget(MNEMONIC_MAP, name) != null;
    }

    public Optional<String> getMnemonic(String name) {
        String mnemonic = connection.sync().hget(MNEMONIC_MAP, name);
        return Optional.ofNullable(mnemonic);
    }

    public void storeAddresses(String name, List<AddressInfo> addressInfos) {
        connection.sync().hset(ADDRESS_MAP, name, JsonUtil.getPrettyJson(addressInfos));
    }

    public List<AddressInfo> getAddresses(String name) {
        String content = connection.sync().hget(ADDRESS_MAP, name);
        if (content == null || content.isEmpty())
            return Collections.EMPTY_LIST;
        try {
            return (List<AddressInfo>) jsonHelper.toList(content, new TypeReference<List<AddressInfo>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Error parsing address content for faucet: " + name, e);
            return Collections.EMPTY_LIST;
        }
    }
}
