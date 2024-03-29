<template>
  <nav
    v-show="isNavBarVisible"
    class="top-0 left-0 right-0 fixed flex bg-white h-14 border-b border-gray-100 z-30 w-screen transition-all md:pl-60 md:w-auto md:items-stretch"
    :class="{'ml-60':isAsideMobileExpanded}"
  >
    <div class="flex-1 items-stretch flex h-14">
      <nav-bar-item class="items-center flex md:hidden" @click.prevent="menuToggleMobile">
        <icon :path="menuToggleMobileIcon" size="24" />
      </nav-bar-item>
    </div>
    <div class="flex-none items-stretch flex h-14 md:hidden">
      <nav-bar-item class="items-center flex" @click.prevent="menuNavBarToggle">
        <icon :path="menuNavBarToggleIcon" size="24" />
      </nav-bar-item>
    </div>
    <div class="absolute w-screen top-14 left-0 bg-white border-b border-gray-100 overflow-auto shadow max-height-screen-menu md:items-stretch md:flex md:flex-grow md:static md:border-b-0 md:overflow-visible md:shadow-none" :class="{ 'hidden': !isMenuNavBarActive, 'block': isMenuNavBarActive }">
      <div class="md:flex md:items-stretch md:justify-end md:ml-auto">
        <nav-bar-item href="https://bloxbean.com" has-divider is-desktop-icon-only>
          <nav-bar-item-label :icon="mdiHelpCircleOutline" label="About" is-desktop-icon-only />
        </nav-bar-item>
        <nav-bar-item href="https://github.com/bloxbean/bloxbean-playground" has-divider is-desktop-icon-only>
          <nav-bar-item-label :icon="mdiGithub" label="GitHub" is-desktop-icon-only />
        </nav-bar-item>
<!--        <nav-bar-item is-desktop-icon-only>-->
<!--          <nav-bar-item-label :icon="mdiLogout" label="Log out" is-desktop-icon-only />-->
<!--        </nav-bar-item>-->
      </div>
    </div>
  </nav>
</template>

<script>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import {
  mdiForwardburger,
  mdiBackburger,
  mdiClose,
  mdiDotsVertical,
  mdiMenu,
  mdiClockOutline,
  mdiCloud,
  mdiCrop,
  mdiAccount,
  mdiCogOutline,
  mdiEmail,
  mdiLogout,
  mdiHelpCircleOutline,
  mdiGithub
} from '@mdi/js'
import NavBarItem from '@/components/NavBarItem'
import NavBarItemLabel from '@/components/NavBarItemLabel'
import Icon from '@/components/Icon'

export default {
  name: 'NavBar',
  components: {
    NavBarItem,
    NavBarItemLabel,
    Icon
  },
  setup () {
    const store = useStore()

    const isNavBarVisible = computed(() => !store.state.isFormScreen)

    const isAsideMobileExpanded = computed(() => store.state.isAsideMobileExpanded)

    const userName = computed(() => store.state.userName)

    const menuToggleMobileIcon = computed(() => isAsideMobileExpanded.value ? mdiBackburger : mdiForwardburger)

    const menuToggleMobile = () => store.dispatch('asideMobileToggle')

    const isMenuNavBarActive = ref(false)

    const menuNavBarToggleIcon = computed(() => isMenuNavBarActive.value ? mdiClose : mdiDotsVertical)

    const menuNavBarToggle = () => {
      isMenuNavBarActive.value = !isMenuNavBarActive.value
    }

    return {
      isNavBarVisible,
      isAsideMobileExpanded,
      userName,
      menuToggleMobileIcon,
      menuToggleMobile,
      isMenuNavBarActive,
      menuNavBarToggleIcon,
      menuNavBarToggle,
      mdiMenu,
      mdiClockOutline,
      mdiCloud,
      mdiCrop,
      mdiAccount,
      mdiCogOutline,
      mdiEmail,
      mdiLogout,
      mdiHelpCircleOutline,
      mdiGithub
    }
  }
}
</script>
