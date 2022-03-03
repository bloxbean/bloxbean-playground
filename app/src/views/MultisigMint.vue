<template>
  <wallet-connect/>
  <main-section>
    <tiles>
      <card-component title="Random NFT generator" :icon="mdiAccountCircle" class="tile is-child">
        <div class="flex justify-center mb-3.5">
          <span class="font-semibold">How many ? (15 tAda / NFT)</span>
        </div>
        <div class="flex justify-center space-x-3.5 mt-3.5">
          <div class="form-check form-check-inline">
            <input

              v-model="quantity"
              :value="1"
              class="form-check-input form-check-input appearance-none rounded-full h-4 w-4 border border-gray-300 bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
              type="radio" name="inlineRadioOptions" id="inlineRadio1" value="option1">
            <label class="form-check-label inline-block text-gray-800">1</label>
          </div>
          <div class="form-check form-check-inline">
            <input
              v-model="quantity"
              :value="2"
              class="form-check-input form-check-input appearance-none rounded-full h-4 w-4 border border-gray-300 bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
              type="radio" name="inlineRadioOptions" id="inlineRadio2" value="option2">
            <label class="form-check-label inline-block text-gray-800">2</label>
          </div>
          <div class="form-check form-check-inline">
            <input
              v-model="quantity"
              :value="3"
              class="form-check-input form-check-input appearance-none rounded-full h-4 w-4 border border-gray-300 bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
              type="radio" name="inlineRadioOptions" id="inlineRadio3" value="option3">
            <label class="form-check-label inline-block text-gray-800">3</label>
          </div>
        </div>
        <form @submit.prevent="submit">
          <divider/>
          <field>
            <control>
              <div class="flex justify-center mt-5">
                <button :class="address? '': 'disabled:opacity-75'"
                        @click="buildTransaction"
                        type="submit"
                        class="button small blue"
                        v-if="!processing && connected">
                  <span>Click To Mint</span>
                </button>
                <span v-else>{{ processingMsg }}</span>

                <button :class="address? '': 'disabled:opacity-75'"
                        type="submit"
                        class="button small blue"
                        v-if="!connected" disabled>
                  <span>Not Connected</span>
                </button>
              </div>
            </control>
          </field>
        </form>
        <div v-if="processing" class="mt-3.5">
          <div class="flex items-center justify-center">
            <div class="w-16 h-16 border-b-2 border-gray-900 rounded-full animate-spin"></div>
          </div>
        </div>
      </card-component>

    </tiles>
    <tiles>
      <card-component title="NFT Details" :icon="mdiAccount" class="tile is-child">
        <div class="font-semibold text-gray-500">Transaction Hash:</div>
        <div class="text-gray-500 mt-3.5"><a
          v-bind:href="'https://testnet.cardanoscan.io/transaction/' + transactionHash"
          target="_blank">{{ transactionHash }}</a></div>
        <div class="font-semibold mt-3.5 text-gray-500">NFT Metadata:</div>
        <div class="flex justify-center mt-3.5">
          <textarea class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none"
                    rows="10">{{nftJson}}</textarea>
        </div>

      </card-component>
    </tiles>

  </main-section>
</template>

<script>
import CardComponent from '@/components/CardComponent'
import Tiles from '@/components/Tiles'
import Field from '@/components/Field'
import Control from '@/components/Control'
import {api_base_url} from "../../config";
import WalletConnect from "../app-components/WalletConnect";

export default {
  name: 'Tables',
  components: {
    WalletConnect,
    Tiles,
    CardComponent,
    Field,
    Control
  },
  data() {
    return ({
      processing: false,
      processingMsg: '',
      quantity: 0,
      nftJson: '',
      transactionHash: ''
    });
  },
  setup() {
    return {}
  },
  computed: {
    connected() {
      return this.$store.state.connected
    },
    address() {
      return this.$store.state.address
    }
  },
  methods: {
    async buildTransaction() {

      this.processingStarted("Building transaction ...")
      let res;
      let witnessCbor;
      let reqId;
      let txnHex;

      this.transactionHash = ''
      this.nftJson = ''

      try {
        res = await fetch(`${api_base_url}/minter/tx-body`, {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            'address': this.address,
            'quantity': this.quantity
          })
        })

        this.processingStopped()

        if (res.status == 200) {
          let body = await res.json()
          reqId = body.reqId
          txnHex = body.txnBody

          console.log(txnHex)
        } else {
          this.resultJson = await res.text()
          return
        }

        this.processingStarted("Waiting for transaction sign ...")
        witnessCbor = await this.$store.state.wallet.signTx(txnHex)
        console.log(witnessCbor)
      } catch (e) {
        console.log(e);
        this.processingStopped()
        return
      }

      this.processingStarted("Minting ...")

      let txnResponse;
      try {
        txnResponse = await fetch(`${api_base_url}/minter/mint`, {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            'reqId': reqId,
            'walletWitnessHex': witnessCbor
          })
        })
      } catch (e) {
        console.log(e);
      }

      this.processingStopped()

      if (txnResponse.status == 200) {
        let txnResp = await txnResponse.json()
        this.transactionHash = txnResp.txHash;
        this.nftJson = txnResp.nftJson;

        console.log(txnResp);
      } else {
        this.transactionHash = ''
        this.nftJson = await txnResponse.text()
      }
    },
    processingStarted(msg) {
      this.processing = true
      this.processingMsg = msg
    },
    processingStopped() {
      this.processing = false
      this.processingMsg = ''
    }
  }
}
</script>
