package com.bloxbean.playground.tokenfaucet.admin.model;

import com.bloxbean.cardano.client.transaction.spec.Value;

public record AddressAmount(String address, Value value) {
}
