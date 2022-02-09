package com.bloxbean.playground.hashcash;

import com.bloxbean.playground.hashcash.model.Challenge;
import com.nettgryppa.security.HashCash;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

@Singleton
public class HashCacheService {

    private final static long HASHCASH_USED_MESSAGE_EXPIRY = 60;
    private final static long CHALLENGE_TIMEOUT = 240;
    private final static int defaultWebChallengeValue = 4; //For request from web
    private final static int defaultChallengeValue = 24;
    private final static int MAX_CHALLENGE_VALUE = 45; //Maximum challenge value. After that no challenge will be generated.

    private final static String HASHCASH_KEY = "hashcash_key";
    private final static String CHALLEGE_VALUE_KEY = "current_challenge_value";
    private final static String CHALLENGE_COUNTER_PREFIX = "CC:";

//    private RedisTemplate redisTemplate;
//    private ValueOperations<String, Challenge> challengeOperation;

    @Inject
    private StatefulRedisConnection<String,String> dfd;

    @Inject
    private StatefulRedisConnection<String,Challenge> connection;

    @Inject
    private StatefulRedisConnection<String,Integer> hashCashConnection;

//    public HashCacheService() {
////        this.redisTemplate = redisTemplate;
////        this.challengeOperation = redisTemplate.opsForValue();
//    }

    public Long nextChallengeNo() {
        return connection.sync().incrby("challenge_counter", 1);
        //return redisTemplate.opsForValue().increment("challenge_counter", 1);
    }

    public void addHashCash(HashCash hashCash) {
        hashCashConnection.sync().setex(hashCash.toString(), HASHCASH_USED_MESSAGE_EXPIRY, hashCash.getValue());
//        redisTemplate.opsForValue().set(hashCash.toString(), hashCash.getValue(), HASHCASH_USED_MESSAGE_EXPIRY, TimeUnit.SECONDS);
    }

    public Object getHashCash(String hashcash) {
        return hashCashConnection.sync().get(hashcash);
//        return redisTemplate.opsForValue().get(hashcash);
    }

    public int getHashCashValue(String hashCashString) {
        return hashCashConnection.sync().get(hashCashString);
//            return Integer.parseInt((String) redisTemplate.opsForValue().get(hashCashString));
    }

    public Challenge getChallenge(String clientIp, boolean requestFromWeb) {
        Integer currentChallengeValue = null;
        if (requestFromWeb) {
            currentChallengeValue = defaultWebChallengeValue;
        } else {
            currentChallengeValue = defaultChallengeValue;
        }

        if (!StringUtils.isEmpty(clientIp)) {
            currentChallengeValue = getChallengeFromRateLimit(clientIp, requestFromWeb);
        }

        if (currentChallengeValue > MAX_CHALLENGE_VALUE) { //Too many requests from this ip. Don't generate any challenge..probably an attacker.
            return Challenge.builder()
                    .value(Integer.MAX_VALUE)
                    .message("Too many requests from this ip")
                    .counter(Integer.MAX_VALUE)
                    .build();
        }

        Long challengeCounter = nextChallengeNo();

        String randomString = RandomStringUtils.randomAlphabetic(5);

        Challenge challenge = Challenge.builder().value(currentChallengeValue)
                .message(randomString)
                .counter(challengeCounter)
                .build();

        connection.sync().setex(CHALLENGE_COUNTER_PREFIX + challengeCounter, CHALLENGE_TIMEOUT, challenge);

        return challenge;
    }

    private int getChallengeFromRateLimit(String clientIp, boolean requestFromWeb) {
        String key = clientIp + ":rightnow";
        Object valueObj = hashCashConnection.sync().get(key);

        int challenge = defaultChallengeValue;
        if (requestFromWeb)
            challenge = defaultWebChallengeValue;

        if (valueObj != null) {

            int value = 0;
            try {
                value = Integer.parseInt(String.valueOf(valueObj));
            } catch (Exception e) {

            }

            int reminder = (int) value / 3; //Every 3 requests within six hour will increase difficulty
            challenge = challenge + (reminder * 2);

            //Update
            hashCashConnection.sync().incrby(key, 1);

        } else { //value null,set the key

            connection.sync().multi();
            connection.sync().incrby(key, 1);
            connection.sync().expire(key, Duration.ofSeconds(6));
            connection.sync().exec();
//
//            //execute a transaction
//            Object txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
//                public List<Object> execute(RedisOperations operations) throws DataAccessException {
//                    operations.multi();
//                    operations.opsForValue().increment(key, 1);
//                    operations.expire(key, 6, TimeUnit.HOURS);
//
//                    return operations.exec();
//                }
//            });
        }

        return challenge;
    }

    public Challenge getChallengeForCounter(long counter) {
        return connection.sync().get(CHALLENGE_COUNTER_PREFIX + counter);
    }

    public String ping() {
        return connection.sync().ping();
//        Object result = redisTemplate.execute(new RedisCallback<String>() {
//            @Override
//            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
//                return redisConnection.ping();
//            }
//        });
//
//        if (result != null)
//            return result.toString();
//        else
//            return "ERROR";

    }

}
