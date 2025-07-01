<template>
  <div class="mb-4">
    <label v-if="label" :for="id" class="form-label ml-1 mb-2">
      <i :class="icon"></i>
      {{ label }} <span v-if="required" class="ml-2 text-red-500">*</span>
    </label>
    <div class="relative w-full">
      <label class="form-input flex items-center w-full cursor-pointer">
        <input
          type="file"
          class="hidden"
          @change="handleFileChange"
          :accept="accept"
        />
        <span class="w-full truncate">
          {{ modelValue || 'ファイルが選択されていません' }}
        </span>
      </label>
      <p v-if="error" class="mt-1 text-sm text-red-500">{{ error }}</p>
    </div>
  </div>
</template>


<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: File | String,
  accept: {
    type: String,
    default: '',
  },
  label: String,
  required: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: '',
  },
  icon: {
    type: String,
    default: ''
  },
})
const emit = defineEmits(['update:modelValue'])

function handleFileChange(event) {
  const file = event.target.files[0]
  if (file) {
    fileName.value = file.name
    emit('update:modelValue', file)
  } else {
    fileName.value = ''
    emit('update:modelValue', null)
  }
}
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

  .form-input, .form-select {
    width: 100%;
    padding: 12px 16px;
    font-size: 14px;
    color: #111827;
    background-color: #ffffff;
    border: 2px solid #e5e7eb;
    border-radius: 10px;
    transition: all 0.2s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .form-input:focus, .form-select:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    background-color: #ffffff;
    }

    .form-input:hover, .form-select:hover {
    border-color: #d1d5db;
    }
</style>
 