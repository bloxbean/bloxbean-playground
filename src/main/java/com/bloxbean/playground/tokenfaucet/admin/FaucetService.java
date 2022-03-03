package com.bloxbean.playground.tokenfaucet.admin;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.crypto.bip39.MnemonicCode;
import com.bloxbean.cardano.client.crypto.bip39.MnemonicException;
import com.bloxbean.cardano.client.crypto.bip39.Words;
import com.bloxbean.cardano.client.crypto.cip1852.DerivationPath;
import com.bloxbean.playground.common.Configuration;
import com.bloxbean.playground.tokenfaucet.admin.model.AddressInfo;
import com.bloxbean.playground.tokenfaucet.admin.model.FaucetAddressRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class FaucetService {

    @Inject
    private Configuration configuration;

    @Inject
    private FaucetStorage faucetStorage;

    public void createFaucet(String name) throws MnemonicException.MnemonicLengthException {
        //Create account. Send mnemonic
        List<String> mnemonics = MnemonicCode.INSTANCE.createMnemonic(Words.TWENTY_FOUR);
        String mnemonicsStr = mnemonics.stream().collect(Collectors.joining(" "));

        if (faucetStorage.isAvailable(name)) {
            throw new IllegalArgumentException("Faucet with name is already available : " + name);
        }

        faucetStorage.storeMnemonic(name, mnemonicsStr);
    }

    public List<AddressInfo> generateFaucetAddresses(String name, FaucetAddressRequest request, boolean refreshIfAvailable) {
        if (faucetStorage.getAddresses(name).size() != 0 && !refreshIfAvailable)
            throw new IllegalArgumentException("Address list already found for name : " + name);

        if (request.noOfAddresses() > 0 && request.noOfAccounts() > 0)
            throw new IllegalArgumentException("Only one of the value noOfAddresses or noOfAccounts should be set");

        String mnemonic = faucetStorage.getMnemonic(name).orElseGet(() -> {
            throw new IllegalArgumentException("Invalid faucet name. Please create the faucet first. " + name);
        });

        Account account = new Account(configuration.getNetwork(), mnemonic);

        List<AddressInfo> addressInfoList = new ArrayList<>();
        if (request.noOfAccounts() > 0) {
            for (int i = 0; i < request.noOfAccounts(); i++) {
                DerivationPath derivationPath = DerivationPath.createExternalAddressDerivationPathForAccount(i);
                account = new Account(configuration.getNetwork(), mnemonic, derivationPath);

                AddressInfo addressInfo = new AddressInfo(account.baseAddress(), derivationPath);
                addressInfoList.add(addressInfo);
            }
        }

        if (request.noOfAddresses() > 0) {
            for (int i = 0; i < request.noOfAddresses(); i++) {
                DerivationPath derivationPath = DerivationPath.createExternalAddressDerivationPath(i);
                account = new Account(configuration.getNetwork(), mnemonic, derivationPath);

                AddressInfo addressInfo = new AddressInfo(account.baseAddress(), derivationPath);
                addressInfoList.add(addressInfo);
            }
        }

        faucetStorage.storeAddresses(name, addressInfoList);

        return addressInfoList;
    }

    public List<AddressInfo> getAddresses(String name) {
        return faucetStorage.getAddresses(name);
    }

    public Account getAddressForTxn(String name) {
        int size = faucetStorage.getAddresses(name).size();

        Random random = new Random();
        int randomIndex = random.nextInt(0, size);

        AddressInfo addressInfo = faucetStorage.getAddresses(name).get(randomIndex);
        String mnemonic = faucetStorage.getMnemonic(name).orElseGet(() -> {
            throw new IllegalArgumentException("No mnemonic found for faucet : " + name);
        });

        return new Account(configuration.getNetwork(), mnemonic, addressInfo.derivationPath());
    }
}
