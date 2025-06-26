<template>
  <header class="bg-white shadow-sm border-b border-gray-200">
    <div class="flex items-center justify-end px-6 py-4">

      <!-- User Actions -->
      <div class="flex items-center space-x-4 ">
        <Button
          @click="$router.push('/signin')"
          variant="text"
          class="text-sm text-blue-600 hover:text-blue-800 transition-colors bg-transparent"
        >
          ログアウト
        </Button>
      </div>
    </div>
  </header>
</template>

<script setup>
import Button from '../atoms/Button.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  modelValue: String
})
</script>
