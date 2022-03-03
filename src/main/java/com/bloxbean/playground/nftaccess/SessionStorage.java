package com.bloxbean.playground.nftaccess;

import com.bloxbean.playground.common.JsonHelper;
import com.bloxbean.playground.common.RandomGenerator;
import com.bloxbean.playground.nftaccess.model.NFTSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Singleton
@Slf4j
public class SessionStorage {
    @Inject
    private StatefulRedisConnection<String, String> connection;

    @Inject
    private RandomGenerator randomGenerator;

    @Inject
    private JsonHelper jsonHelper;

    public NFTSession createSession(String address, String role) {
        String sessionId = randomGenerator.getSessionId();

        LocalDateTime expiryAt = LocalDateTime.now();
        expiryAt.plusHours(2);

        NFTSession nftSession = new NFTSession(sessionId, address, role, expiryAt.toString());

        String json = jsonHelper.getJson(nftSession);

        long twoHours = 7200;
        connection.sync().setex(sessionId, twoHours, json);

        return nftSession;
    }

    public Optional<NFTSession> getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty())
            return Optional.empty();

        String content = connection.sync().get(sessionId);

        if (content == null)
            return Optional.empty();
        else {
            try {
                return Optional.ofNullable(jsonHelper.toObject(content, NFTSession.class));
            } catch (JsonProcessingException e) {
                log.error("Json parsing error for nftsession", e);
                return Optional.empty();
            }
        }
    }
}
