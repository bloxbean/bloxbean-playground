package com.bloxbean.playground.tokenfaucet.distribution;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.playground.common.Configuration;
import com.bloxbean.playground.tokenfaucet.admin.FaucetStorage;
import com.bloxbean.playground.tokenfaucet.admin.model.AddressInfo;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Random;

@Singleton
public class FaucetAddressLockService {

    @Inject
    private Configuration configuration;

    @Inject
    private FaucetStorage faucetStorage;

    public Account getAccount(String faucetName) {
        if (faucetStorage.getAddresses(faucetName).size() == 0)
            throw new RuntimeException("Faucet not found or no address found in the faucet: " + faucetName);

        List<AddressInfo> addressInfoList = faucetStorage.getAddresses(faucetName);
        Random random = new Random();
        int randomIndex = random.nextInt(0, addressInfoList.size());

        AddressInfo addressInfo = faucetStorage.getAddresses(faucetName).get(randomIndex);

        String mnemonic = faucetStorage.getMnemonic(faucetName).orElseGet(() -> {
            throw new RuntimeException("Mnemonic not found for the faucet : " + faucetName);
        });

        Account account = new Account(configuration.getNetwork(), mnemonic, addressInfo.derivationPath());

        System.out.println("AddressInfo address: " + addressInfo.address());
        System.out.printf("Account address: " + account.baseAddress());
        return account;
    }
}
