<template>
  <div>
    <tiles>
      <card-component title="Create Token Faucet" :icon="mdiAccountCircle" class="tile is-child">
        <div class="flex justify-center md-8">
          <figcaption class="text-sm text-gray-600">
            <cite>Create a token faucet. Generate multiple faucet addresses from the same key for token distribution. Provide an unique faucet name.</cite>
          </figcaption>
        </div>
        <div class="flex justify-center space-x-3.5 mt-10">
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
            <label class="block text-gray-700 text-sm font-bold mb-2" for="address_id">
              # of Addresses
            </label>
            <input
              v-model="noOfAddresses"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="address_id" type="text" placeholder="0">
          </div>

          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="account_id">
              # of Accounts
            </label>
            <input
              v-model="noOfAccounts"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="account_id" type="text" placeholder="0">
          </div>
        </div>
        <form @submit.prevent="submit">
          <divider/>
          <field>
            <control>
              <div class="flex space-x-2 justify-center">
                <button
                  @click="createFaucet"
                  type="submit"
                  class="button small blue"
                  v-if="!processing">
                  <span>Create Faucet</span>
                </button>

                <button
                  @click="generateAddress"
                  type="submit"
                  class="button small amber"
                  v-if="!processing">
                  <span>Create Addresses</span>
                </button>

                <button
                  @click="getAddresses"
                  type="submit"
                  class="button small cyan"
                  v-if="!processing">
                  <span>Get Addresses</span>
                </button>

                <span v-else>{{ processingMsg }}</span>
              </div>
            </control>
          </field>
        </form>
        <div v-if="processing" class="mt-3.5">
          <div class="flex items-center justify-center">
            <div class="w-16 h-16 border-b-2 border-gray-900 rounded-full animate-spin"></div>
          </div>
        </div>

        <div class="flex mt-3.5 font-bold">Response</div>
        <div class="flex justify-center mt-3.5">

          <textarea class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none"
                    rows="10">{{this.response}}</textarea>
        </div>
      </card-component>

    </tiles>
  </div>
</template>

<script>
import MainSection from '@/components/MainSection'
import CardComponent from '@/components/CardComponent'
import Tiles from '@/components/Tiles'
import Field from '@/components/Field'
import {api_base_url} from "../../../config";
import WalletConnect from "../../app-components/WalletConnect";

export default {
  name: 'Tables',
  components: {
    WalletConnect,
    MainSection,
    Tiles,
    CardComponent,
    Field,
  },
  data() {
    return ({
      processing: false,
      processingMsg: '',
      faucet: '',
      noOfAddresses: 0,
      noOfAccounts: 0,
      response: ''
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
    },
    sessionId() {
      return this.$store.state.sessionId
    },
    role() {
      return this.$store.state.role
    }
  },
  methods: {
    async createFaucet() {

      this.processingStarted("Create Faucet ...")
      let res;

      try {
        res = await fetch(`${api_base_url}/faucet/${this.faucet}`, {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'SessionId': this.sessionId
          }
        })

        let body = await res.body
        this.processingStopped();

        if (res.status == 200) {
          console.log(`Faucet ${this.faucet} created successfully`)
          this.response = "Faucet created successfully"
        } else {
          console.log("Faucet creation failed")
          this.response = "Faucet creation failed"
        }
      } catch (e) {
        console.log(e);
        this.processingStopped()
        this.response = "Faucet creation failed"
        return
      }
    },
    async generateAddress() {

      this.processingStarted("Generate Faucet addresses ...")
      let res;

      try {
        res = await fetch(`${api_base_url}/faucet/${this.faucet}/addresses`, {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'SessionId': this.sessionId
          },
          body: JSON.stringify({
            'noOfAccounts': this.noOfAccounts,
            'noOfAddresses': this.noOfAddresses
          })
        })

        let body = await res.json()
        this.processingStopped();

        if (res.status == 200) {
          console.log(`Faucet addresses ${this.faucet} created successfully`)
          this.response = body
        } else {
          console.log("Faucet addresses creation failed")
          this.response = "Faucet addresses creation failed"
        }
      } catch (e) {
        console.log(e);
        this.processingStopped()
        this.response = "Faucet addresses creation failed"
        return
      }
    },
    async getAddresses() {

      this.processingStarted("Get Faucet addresses ...")
      let res;

      try {
        res = await fetch(`${api_base_url}/faucet/${this.faucet}/addresses`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'SessionId': this.sessionId
          }
        })

        let body = await res.json()
        this.processingStopped();

        let formattedBody = JSON.stringify(body, null, 2)
        console.log(formattedBody)

        if (res.status == 200) {
          console.log(`Faucet addresses ${this.faucet} fetched successfully`)
          this.response = body
        } else {
          console.log("Faucet addresses fetch failed")
          this.response = "Faucet addresses fetch failed"
        }
      } catch (e) {
        console.log(e);
        this.processingStopped()
        this.response = "Faucet addresses fetch failed"
        return
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
