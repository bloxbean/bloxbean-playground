package com.bloxbean.playground.tokenfaucet.admin.model;

import java.util.List;

public record TopupResult(String txHash, List<AddressAmount> addressAmounts) {

}


