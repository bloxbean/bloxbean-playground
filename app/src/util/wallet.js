import {Buffer} from "buffer";
import {Address, BaseAddress} from "@emurgo/cardano-serialization-lib-browser";

class Wallet {
  api
  walletType

  constructor() {

  }

  namiAvailable() {
    return namiWallet ? true : false;
  }

  ccvalutAvailable() {
    return ccWallet ? true : false;
  }

  async connect(walletType) {
    this.walletType = walletType
    this.api = await cardano[this.walletType].enable()
  }

  async getUseAddress() {
    console.log(this.api)
    const addresses = await this.api.getUsedAddresses()

    const addressHex = Buffer.from(
      addresses[0],
      "hex"
    );

    const address = BaseAddress.from_address(
      Address.from_bytes(addressHex)
    ).to_address().to_bech32()

    return address
  }

  async signTx(txnBody) {
    const txCbor = await this.api.signTx(txnBody, true)

    return txCbor
  }

  reset() {
    this.walletType = null
    this.api = null
  }
}

export default Wallet
