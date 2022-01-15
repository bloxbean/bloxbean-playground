<template>
  <nav-bar/>
  <aside-menu :menu="menu"/>
  <router-view/>
  <footer-bar/>
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

    store.commit('user', {
      name: 'John Doe',
      email: 'john@example.com',
      avatar: 'https://avatars.dicebear.com/v2/gridy/John-Doe.svg'
    })

    const menu = ref(
      [
        'General',
        [
          {
            to: '/',
            icon: mdiDesktopMac,
            label: 'Dashboard'
          }
        ],
        'Apps',
        [
          {
            to: '/minter',
            label: 'Minter',
            icon: mdiTable,
            updateMark: true
          },
          {
            to: '/tables',
            label: 'Tables',
            icon: mdiTable,
            updateMark: true
          }
        ],
        'About',
        [
          {
            href: 'https://bloxbean.com',
            label: 'About',
            icon: mdiHelpCircle
          },
          {
            href: 'https://github.com/bloxbean',
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
