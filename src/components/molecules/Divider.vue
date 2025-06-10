<template>
  <div class="flex items-center my-4">
    <div class="flex-grow border-t border-gray-300"></div>
    <div class="px-4 text-gray-500 text-sm">{{ text }}</div>
    <div class="flex-grow border-t border-gray-300"></div>
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: 'Or'
  }
})
</script>
