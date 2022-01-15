<template>
  <title-bar :title-stack="titleStack" />
  <hero-bar>
    NFT Minter
<!--    <template #right>-->
<!--      <router-link to="/" class="button light">-->
<!--        Dashboard-->
<!--      </router-link>-->
<!--    </template>-->
  </hero-bar>
  <main-section>
    <tiles>
      <card-component title="Edit Profile" :icon="mdiAccountCircle" class="tile is-child">
        <form @submit.prevent="submit">
          <file-picker label="Image / File to upload" />
          <divider/>
          <field label="Name" help="Required. Name">
            <control :icon-left="mdiAccount">
              <input v-model="nftName" name="name" class="input" required />
            </control>
          </field>
          <field label="Image" help="Required. Image">
            <control :icon-left="mdiMail" :icon-right="mdiCheck">
              <input v-model="nftImage" name="image" class="input" required />
            </control>
          </field>
          <field label="description" help="Description">
            <control :icon-left="mdiMail" :icon-right="mdiCheck">
              <input v-model="description" name="description" class="textarea" required />
            </control>
          </field>
          <field label="otherProperties" help="Other Properties">
            <control :icon-left="mdiMail" :icon-right="mdiCheck">
              <input v-model="otherProperties" name="otherProperties" class="textarea" required />
            </control>
          </field>

          <divider/>
          <field>
            <control>
              <button type="submit" class="button blue">
                Click To Mint
              </button>
            </control>
          </field>
        </form>
      </card-component>
      <card-component title="Preview" :icon="mdiAccount" class="tile is-child">
        Image Preview
<!--        <user-avatar class="w-48 h-48 mx-auto" />-->
<!--        <divider/>-->
<!--        <field label="Name">-->
<!--          <control>-->
<!--            <input class="input" :value="userName" readonly />-->
<!--          </control>-->
<!--        </field>-->
<!--        <divider/>-->
<!--        <field label="E-mail">-->
<!--          <input :value="userEmail" class="input" readonly />-->
<!--        </field>-->
      </card-component>
    </tiles>

  </main-section>
</template>

<script>
import { ref } from 'vue'
import { mdiMonitorCellphone, mdiAccountMultiple, mdiTableBorder, mdiTableOff } from '@mdi/js'
import MainSection from '@/components/MainSection'
// import Notification from '@/components/Notification'
// import ClientsTable from '@/components/ClientsTable'
import CardComponent from '@/components/CardComponent'
import TitleBar from '@/components/TitleBar'
import HeroBar from '@/components/HeroBar'
import Tiles from '@/components/Tiles'
import FilePicker from '@/components/FilePicker'
import Divider from '@/components/Divider'
import Field from '@/components/Field'
import Control from '@/components/Control'

export default {
  name: 'Tables',
  components: {
    MainSection,
    HeroBar,
    Tiles,
    TitleBar,
    CardComponent,
    FilePicker,
    Divider,
    Field,
    Control
    // ClientsTable,
    // Notification
  },
  setup () {
    const titleStack = ref(['App', 'Minter'])

    return {
      titleStack,
      mdiMonitorCellphone,
      mdiAccountMultiple,
      mdiTableBorder,
      mdiTableOff
    }
  }
}
</script>
