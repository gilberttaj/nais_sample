<template>
  <button 
    :class="[
      'transition duration-200 flex items-center justify-center',
      variant === 'primary' ? 'btn-primary' : '',
      variant === 'outline' ? 'border border-primary text-primary hover:bg-primary hover:text-white font-semibold py-2 px-4 rounded-md' : '',
      variant === 'text' ? 'text-primary hover:text-primary-dark font-semibold' : '',
      fullWidth ? 'w-full' : '',
      disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'
    ]"
    :disabled="disabled"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'outline', 'text'].includes(value)
  },
  fullWidth: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

defineEmits(['click']);
</script>
