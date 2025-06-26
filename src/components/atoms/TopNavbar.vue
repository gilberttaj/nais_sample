<template>
  <header class="bg-white shadow-sm border-b border-gray-200">
    <div class="flex items-center justify-between px-6 py-4">
      <!-- Search Bar -->
      <div class="flex-1 max-w-md">
        <div class="relative">
          <input
            :value="modelValue"
            @input="updateValue"
            type="text"
            placeholder="Search..."
            class="w-full pl-4 pr-12 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
            @keyup.enter="emitSearch"
          />
          <Button
            @click="emitSearch"
            class="absolute right-2 top-1/2 transform -translate-y-1/2 bg-blue-600 text-white p-1.5 rounded hover:bg-blue-700 transition-colors"
          >
            <SearchIcon class="w-4 h-4" />
          </Button>
        </div>
      </div>

      <!-- User Actions -->
      <div class="flex items-center space-x-4">
        <Button variant=text class="p-2 text-gray-400 hover:text-gray-600 transition-colors">
          <BellIcon class="w-5 h-5" />
        </Button>
        <Button
          @click="$emit('logout')"
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
import { SearchIcon, BellIcon } from 'lucide-vue-next'
import Button from '../atoms/Button.vue'

const props = defineProps({
  modelValue: String
})

const emits = defineEmits(['update:modelValue', 'search', 'logout'])

function emitSearch() {
  emits('search')
}

const updateValue = (event) => {
  emit('update:modelValue', event.target.value)
}
</script>
