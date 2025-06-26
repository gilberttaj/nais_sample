<template>
  <div class="w-full max-w-md">
    <GoogleSignInButton @click="handleGoogleSignIn" :disabled="authStore.isLoading">
      <template #icon v-if="authStore.isLoading">
        <LoadingSpinner :size="20" :dot-size="4" class="mr-2" />
      </template>
    </GoogleSignInButton>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import GoogleSignInButton from '@/components/molecules/GoogleSignInButton.vue'
import LoadingSpinner from '@/components/atoms/LoadingSpinner.vue'
import { useAuthStore } from '@/stores/auth.js'


const router = useRouter()
const authStore = useAuthStore()

// Handle Google sign-in
const handleGoogleSignIn = async () => {
  try {
    await authStore.signInWithGoogle(router)
  } catch (error) {
    console.error('Sign-in error:', error)
    // You can add user-friendly error handling here
    // For example, show a toast notification or set an error state
  }
}

</script>

<style scoped>
.mr-2 {
  margin-right: 8px;
}
</style>
