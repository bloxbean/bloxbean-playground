package com.bloxbean.playground.hashcash;

import com.bloxbean.playground.hashcash.exception.AlreadyUsedException;
import com.bloxbean.playground.hashcash.exception.InvalidMessageException;
import com.bloxbean.playground.hashcash.model.Challenge;
import com.nettgryppa.security.HashCash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;

@Service
public class HashCashService {
    private static final Logger logger = LoggerFactory.getLogger(HashCash.class);

    @Autowired
    private HashCacheService hashCacheService;

    public Extension validate(String hashCashStr) throws AlreadyUsedException, NoSuchAlgorithmException, InvalidMessageException {
        Object hashCashOj = hashCacheService.getHashCash(hashCashStr);

        if (hashCashOj != null)
            throw new AlreadyUsedException("Hashcash value has already been used");

        HashCash hashCash = new HashCash(hashCashStr);

        //Get value
        int value = hashCash.getValue();
        Calendar date = hashCash.getDate();

        String message = hashCash.getResource();
        Long counter = null;
        String account = null;
        String network = null;

        List<String> list = hashCash.getExtensions().get("data");

        if (list != null && list.size() >= 2) {
            counter = Long.parseLong(list.get(0));
            account = list.get(1);

            if (logger.isDebugEnabled())
                logger.debug("Extension size >>> " + list.size());

            if (list.size() >= 3) { //Only sent after Amity network support
                network = list.get(2);
            }

            if (logger.isDebugEnabled())
                logger.debug("Network > " + network);
        }

        if (counter == null || counter == 0)
            throw new InvalidMessageException("Invalid or null counter");

        if (StringUtils.isEmpty(account))
            throw new InvalidMessageException("Invalid account in the message");

        Challenge challengeInRedis = hashCacheService.getChallengeForCounter(counter);

        if (challengeInRedis == null) {
            System.out.println("Challenge not found for counter >> " + counter);
            return new Extension(network, account);
        }

        if (!challengeInRedis.getMessage().equals(message) || challengeInRedis.getValue() != value) {
            System.out.println("Tempered message");
            return null;
        } else {
            return new Extension(network, account);
        }
    }

    public static class Extension {
        private String network;
        private String account;

        public Extension(String network, String account) {
            this.network = network;
            this.account = account;
        }

        public String getNetwork() {
            return network;
        }

        public String getAccount() {
            return account;
        }
    }
}
