package com.bloxbean.playground.tokenfaucet.admin.model;

import java.math.BigInteger;

public record TopupTxnRequest(String sender, String policyId, String assetName, BigInteger qty) {
}
