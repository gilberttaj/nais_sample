<template>
  <div class="mb-5">
    <label v-if="label" :for="id" class="form-label ml-1 flex items-center gap-1 text-sm font-medium text-gray-700 mb-1">
      <i :class="icon"></i>
      {{ label }} <span v-if="required" class="ml-2 text-red-500">*</span>
    </label>
    <div class="relative">
      <select
        :id="id"
        :value="modelValue"
        @change="$emit('update:modelValue', $event.target.value)"
        :disabled="disabled"
        :required="required"
        class="w-full py-3 px-4 text-sm text-gray-900 bg-white border-2 border-gray-300 rounded-[10px] shadow-md transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-200 focus:border-indigo-500 hover:border-gray-400"
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
  icon: {
    type: String,
    default: ''
  },
});

const emit = defineEmits(['update:modelValue']);

const normalizedOptions = computed(() =>
  (props.options ?? []).map(opt =>
    typeof opt === 'string'
      ? { value: opt, label: opt }
      : { value: opt?.value, label: opt?.label }
  )
);
</script>
<style scoped>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
  @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

  * {
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }

  .form-label {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    margin-bottom: 8px;
  }

  .form-label i {
    margin-right: 8px;
    color: #6b7280;
    width: 16px;
  }
</style>
