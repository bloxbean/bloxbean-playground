import {createStore} from 'vuex'
import Wallet from "../util/wallet";

export default createStore({
  state: {
    wallet: new Wallet(),
    address: null,
    connected: false,
    sessionId: null,
    role: null,
    // /* Aside */
    isAsideMobileExpanded: false
  },
  mutations: {
    setAddress(state, add) {
      state.address = add
      state.connected = true
    },
    disconnect(state) {
      state.address = null
      state.connected = false
      state.sessionId = null
      state.role = null
      state.wallet.reset()
    },
    setSession(state, session) {
      state.sessionId = session.sessionId
      state.role = session.role
    },
    /* A fit-them-all commit */
    basic(state, payload) {
      state[payload.key] = payload.value
    },
  },
  actions: {
    async doConnect({commit, state}, walletType) {
      await state.wallet.connect(walletType)
      const address = await state.wallet.getUseAddress()
      commit('setAddress', address)
    },
    asideMobileToggle({commit, state}, payload = null) {
      const isShow = payload !== null ? payload : !state.isAsideMobileExpanded

      document.getElementById('app').classList[isShow ? 'add' : 'remove']('ml-60')

      document.documentElement.classList[isShow ? 'add' : 'remove']('m-clipped')

      commit('basic', {
        key: 'isAsideMobileExpanded',
        value: isShow
      })
    },
  },
  getters: {
    getAddress: state => {
      return state.address
    }
  },
  modules: {}
})
