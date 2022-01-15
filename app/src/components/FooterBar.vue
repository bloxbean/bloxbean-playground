<template>
  <footer v-show="isFooterBarVisible" class="bg-white py-2 px-6">
    <div class="container-fluid">
      <div class="level">
        <div class="level-left">
          <div class="level-item">
            <div class="footer-copyright">
              <b>&copy; {{ year }}, BloxBean.com</b> &mdash; Dashboard
            </div>
          </div>
        </div>
        <div class="level-right">
          <div class="level-item">
            <div class="logo">
              <a href="https://bloxbean.com">
                BloxBean
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
import dayjs from 'dayjs'
import { computed } from 'vue'
import { useStore } from 'vuex'

export default {
  name: 'FooterBar',
  setup () {
    const store = useStore()

    const year = computed(() => dayjs().year())

    const isFooterBarVisible = computed(() => !store.state.isFormScreen)

    return {
      year,
      isFooterBarVisible
    }
  }
}
</script>
