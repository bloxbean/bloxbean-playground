<template>
  <section class="p-6 border-b border-gray-100">
    <div class="level">
      <div class="level-left">
      </div>
      <div class="level-right">
        <div class="level-item">

        </div>
      </div>
    </div>
  </section>
</template>

<script>
import Icon from '@/components/Icon'
import { mdiCreditCard } from '@mdi/js'

export default {
  name: 'TitleBar',
  components: { Icon },
  props: {
    titleStack: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return ({

    });
  },
  computed: {

  },
  methods: {

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
