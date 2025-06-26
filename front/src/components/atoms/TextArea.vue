<template>
  <div>
    <label v-if="label" :for="id" class="block text-sm mb-1">{{ label }}</label>
    <div class="relative">
        <textarea
        :id="id"
        :value="innerValue"
        @input="onInput"
        :placeholder="placeholder"
        :rows="rows"
        :class="[
            'w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary',
            error ? 'border-red-500' : 'border-gray-300'
        ]"
        ></textarea>
    </div>
    <p v-if="error" class="text-red-500 text-sm mt-1">{{ error }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  label: String,
  placeholder: String,
  rows: {
    type: Number,
    default: 3,
  },
  id: {
    type: String,
    default: () => `textarea-${Math.random().toString(36).substr(2, 9)}`,
  },
  error: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['update:modelValue']);

const innerValue = computed(() => props.modelValue);
const onInput = (event) => {
  emit('update:modelValue', event.target.value);
};
</script>

<style scoped>
</style>