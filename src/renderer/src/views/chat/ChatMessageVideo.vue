<template>
  <div class="image-panel" v-if="data.status == 1">
    <template v-if="data.status == 1">
      <ShowLocalImage :fileId="data.messageId" :showPlay="true" partType="chat" :fileType="data.fileType">
      </ShowLocalImage>
    </template>
  </div>
  <ChatMessageFile v-else :data="data"></ChatMessageFile>
</template>

<script setup>
import ChatMessageFile from './ChatMessageFile.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useGlobalInfoStore } from '@/stores/GlobalInfoStore'
const globalInfoStore = useGlobalInfoStore()

const props = defineProps({
  data: {
    type: Object,
    default: {}
  }
})
</script>

<style lang="scss" scoped>
</style>
