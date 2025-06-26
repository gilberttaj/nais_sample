<template>
  <div class="mb-4">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700">
      {{ label }}
    </label>
    <div class="relative">
      <select
        :id="id"
        :value="modelValue"
        @change="$emit('update:modelValue', $event.target.value)"
        :disabled="disabled"
        :required="required"
        class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
      >
        <option v-if="placeholder" disabled value="">{{ placeholder }}</option>
        <option
          v-for="option in normalizedOptions"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: [String, Number],
  label: String,
  placeholder: String,
  disabled: Boolean,
  required: Boolean,
  options: {
    type: Array,
    required: true,
    // Accepts: ['PDF', 'MPDF'] or [{ value: 'PDF', label: 'PDF' }]
  },
  id: {
    type: String,
    default: () => `select-${Math.random().toString(36).slice(2)}`,
  },
});

const emit = defineEmits(['update:modelValue']);

// Normalize options to consistent object format
const normalizedOptions = computed(() =>
  props.options.map(opt =>
    typeof opt === 'string'
      ? { value: opt, label: opt }
      : { value: opt.value, label: opt.label }
  )
);
</script>
