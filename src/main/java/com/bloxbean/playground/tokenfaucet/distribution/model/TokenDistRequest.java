package com.bloxbean.playground.tokenfaucet.distribution.model;

import java.math.BigInteger;

public record TokenDistRequest(String receiver, String policyId, String assetName, BigInteger qty) {
}
