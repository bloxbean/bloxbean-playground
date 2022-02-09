package com.bloxbean.playground.cache;

import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CacheService {

    @Inject
    private StatefulRedisConnection<String,String> connection;

//    public void

}
