<template>
  <wallet-connect/>
  <main-section>
    <tiles>
      <card-component title="Get Token" :icon="mdiAccountCircle" class="tile is-child">
        <div class="flex justify-center space-x-3.5 mt-8">
          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="faucet_id">
              Faucet
            </label>
            <input
              v-model="faucet"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="faucet_id" type="text" placeholder="Faucet Name">
          </div>

          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="policyid">
              Policy Id
            </label>
            <input
              v-model="policyId"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="policyid" type="text" placeholder="Policy Id">
          </div>

          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="assetname">
              Asset Name
            </label>
            <input
              v-model="assetName"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="assetname" type="text" placeholder="Asset Name">
          </div>

          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="amount">
              Amount
            </label>
            <input
              v-model="amount"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="amount" type="text" placeholder="0">
          </div>

        </div>
        <form @submit.prevent="submit">
          <divider/>
          <field>
            <control>
              <div class="flex justify-center">
                <button :class="address? '': 'disabled:opacity-75'"
                        @click="buildTransaction"
                        type="submit"
                        class="button small blue"
                        v-if="!processing && connected">
                  <span>Get Token</span>
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


        <div class="font-semibold text-gray-500 mt-8">Transaction Hash:</div>
        <div class="text-gray-500 mt-3.5"><a
          v-bind:href="'https://testnet.cardanoscan.io/transaction/' + transactionHash"
          target="_blank">{{ transactionHash }}</a></div>
        <div class="font-semibold mt-3.5 text-gray-500">Response:</div>
        <div class="flex justify-center mt-3.5">
          <textarea class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none"
                    rows="10">{{resultJson}}</textarea>
        </div>

      </card-component>

    </tiles>

  </main-section>
</template>

<script>
import CardComponent from '@/components/CardComponent'
import Tiles from '@/components/Tiles'
import Field from '@/components/Field'
import {api_base_url} from "../../../config";
import WalletConnect from "../../app-components/WalletConnect";
import FaucetCreate from "./FaucetCreate";

export default {
  name: 'Tables',
  components: {
    FaucetCreate,
    WalletConnect,
    Tiles,
    CardComponent,
    Field,
  },
  data() {
    return ({
      processing: false,
      processingMsg: '',
      faucet: '',
      policyId: '',
      assetName: '',
      amount: 0,
      resultJson: '',
      transactionHash: ''
    });
  },
  setup() {

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
      this.resultJson = ''

      try {
        res = await fetch(`${api_base_url}/faucet/${this.faucet}/distribution`, {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            'receiver': this.address,
            'policyId': this.policyId,
            'assetName': this.assetName,
            'qty': this.amount
          })
        })

        this.processingStopped()

        if (res.status == 200) {
          let body = await res.json()
          reqId = body.reqId
          txnHex = body.txnBody

          console.log(txnHex)
        } else {
          alert('hello')
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

      this.processingStarted("Posting transaction ...")

      let txnResponse;
      try {
        txnResponse = await fetch(`${api_base_url}/faucet/${this.faucet}/distribution/transfer`, {
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
        this.resultJson = e
        this.processingStopped()
        return
      }

      this.processingStopped()

      if (txnResponse.status == 200) {
        let txnResp = await txnResponse.json()
        this.transactionHash = txnResp.txHash;
        this.resultJson = txnResp.addressAmounts;

        console.log(txnResp);
      } else {
        this.transactionHash = ''
        this.resultJson = await txnResponse.text()
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
