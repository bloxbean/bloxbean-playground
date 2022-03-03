package com.bloxbean.playground.tokenfaucet.admin.model;

import com.bloxbean.cardano.client.crypto.cip1852.DerivationPath;

public record AddressInfo(String address, DerivationPath derivationPath) {
}
