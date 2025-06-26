<template>
  <div v-if="show" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-lg max-w-sm w-full p-6">
      <h2 class="text-lg font-semibold mb-2">{{ title }}</h2>
      <p class="text-sm text-gray-700 mb-4">{{ message }}</p>
      <div class="flex justify-end space-x-2">
        <Button variant="outline" :onClick="closeDialog">
          キャンセル
        </Button>
        <Button variant="danger" 
          class="bg-red-500 text-white hover:bg-red-600 px-4 py-2.5 text-sm font-medium text-white shadow-theme-xs rounded-md" 
          :onClick="confirm"
        >
          デリート
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Button from '../atoms/Button.vue'

const props = defineProps(['show', 'title', 'message'])
const emit = defineEmits(['close', 'confirm'])

const closeDialog = () => emit('close')
const confirm = () => emit('confirm')
</script>
<style scoped></style>