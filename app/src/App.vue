<template>
  <nav-bar/>
  <aside-menu :menu="menu"/>
  <router-view/>
<!--  <footer-bar/>-->
</template>

<script>
// @ is an alias to /src
import { ref } from 'vue'
import { useStore } from 'vuex'
import { mdiDesktopMac, mdiTable, mdiHelpCircle, mdiGithub } from '@mdi/js'
import NavBar from '@/components/NavBar'
import AsideMenu from '@/components/AsideMenu'
import FooterBar from '@/components/FooterBar'

export default {
  name: 'Home',
  components: {
    FooterBar,
    AsideMenu,
    NavBar
  },
  setup () {
    const store = useStore()

    const menu = ref(
      [
        'Apps',
        [
          {
            to: '/multisig-minter',
            label: 'Multi-sig Minter',
            icon: mdiTable,
            updateMark: true
          },
        ],
        'About',
        [
          {
            href: 'https://bloxbean.com',
            label: 'About',
            icon: mdiHelpCircle
          },
          {
            href: 'https://github.com/bloxbean/bloxbean-playground',
            label: 'GitHub',
            icon: mdiGithub
          }
        ]
      ]
    )

    return {
      menu
    }
  }
}
</script>
