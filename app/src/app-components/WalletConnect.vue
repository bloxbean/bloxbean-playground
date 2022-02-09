<template>
  <section class="p-6 border-b border-gray-100">
    <div class="level">
      <div class="level-left">
      </div>
      <div class="level-right">
        <div class="level-item">
          <div v-if="!connected" class="buttons is-right">
            <a href="#" target="_blank" class="button small cyan"  @click.prevent="initConnection('nami')">
              <icon :path="mdiCreditCard" class="mr-3" />
              <span>Connect To Nami</span>
            </a>
            <a href="#" target="_blank" class="button small amber"  @click.prevent="initConnection('flint')">
              <icon :path="mdiCreditCard" class="mr-3" />
              <span>Connect To Flint</span>
            </a>
            <a href="#" target="_blank" class="button small violet"  @click.prevent="initConnection('ccvault')">
              <icon :path="mdiCreditCard" class="mr-3" />
              <span>Connect To ccvault</span>
            </a>
          </div>
          <div v-else>
            <span class="text-zinc-500 mr-2" :title="address">{{ shortAddress }}</span>
            <a href="#" target="_blank" class=""  @click.prevent="disconnect()">
              <span class="text-red-500">x</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import Icon from '@/components/Icon'
import { mdiCreditCard } from '@mdi/js'

export default {
  name: 'WalletConnect',
  components: { Icon },
  data() {
    return ({

    });
  },
  computed: {
    connected() {
      return this.$store.state.connected
    },
    address() {
      return this.$store.state.address
    },
    shortAddress() {
      if (this.$store.state.address) {
        let _address = this.$store.state.address
        let first = _address.substr(0,20);
        let end = _address.slice(-5);
        return first + "..." + end
      }
    }
  },
  methods: {
    async initConnection(walletType){
      this.$store.dispatch('doConnect', walletType)
    },
    async disconnect() {
      this.$store.commit('disconnect')
    }
  },
  setup () {
   return {
      mdiCreditCard
    }
  }
}
</script>

<style scoped>
li.title-stack-item:not(:last-child):after {
  content: '/';
  @apply inline-block pl-3;
}

li.title-stack-item:last-child {
  @apply pr-0 font-black text-black;
}
</style>
