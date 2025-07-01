<template>
  <div class="mb-4">
    <label v-if="label" :for="id" class="form-label ml-2">
      <i :class="icon"></i>
      {{ label }} <span v-if="required" class="ml-2 text-red-500">*</span>
    </label>

    <div class="relative">
      <input
        :id="id"
        :type="type"
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :placeholder="placeholder"
        :required="required"
        :class="[
          'w-full py-3 px-4 text-sm text-gray-900 bg-white border-2 border-gray-200 rounded-lg shadow-sm transition-all',
          'hover:border-gray-300',
          error ? 'border-red-500 focus:ring-red-100 focus:border-red-500' : 'focus:border-indigo-500 focus:ring-indigo-100',
          'focus:outline-none focus:ring-4'
        ]"
      />

      <div v-if="type === 'password'" class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer">
        <span @click="$emit('toggle-password')" class="text-gray-500">
          <slot name="icon"></slot>
        </span>
      </div>
    </div>

    <p v-if="error" class="mt-1 text-sm text-red-500">{{ error }}</p>
  </div>
</template>

<script setup>
defineProps({
  id: {
    type: String,
    required: true
  },
  label: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
});

defineEmits(['update:modelValue', 'toggle-password']);
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
