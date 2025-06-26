<!-- src/views/AuthValidation.vue -->
<template>
  <div class="auth-validation">
    <div v-if="loading" class="loading-container">
      <div class="spinner-container">
        <LoadingSpinner :size="35" />
        <div class="authenticating-text">Authenticating...</div>
      </div>
    </div>
    <div v-else-if="error" class="error-container">
      <h2>Authentication Error</h2>
      <p>{{ errorMessage }}</p>
      <button @click="goToLogin">Return to Login</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LoadingSpinner from '@/components/atoms/LoadingSpinner.vue'
import { useAuthStore } from '@/stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

// Function to parse URL query parameters
const getQueryParams = () => {
  const params = new URLSearchParams(window.location.search)
  const result = {}
  for (const [key, value] of params.entries()) {
    result[key] = value
  }
  return result
}

// Function to navigate to login page
const goToLogin = () => {
  authStore.clearAuth()
  router.push('/signin')
}

onMounted(() => {
  try {
    const params = getQueryParams()

    if (params.status === 'error') {
      error.value = true
      errorMessage.value = params.message || 'Authentication failed'
      loading.value = false
      return
    }

    if (params.status === 'success' && params.access_token) {
      // Store tokens using auth store
      authStore.storeAuthTokens({
        id_token: params.id_token,
        access_token: params.access_token,
        refresh_token: params.refresh_token,
        expires_in: params.expires_in,
        token_type: params.token_type
      })

      // Redirect to home page after successful authentication
      router.push('/')
    } else {
      error.value = true
      errorMessage.value = 'Invalid authentication response'
      loading.value = false
    }
  } catch (err) {
    console.error('Authentication validation error:', err)
    error.value = true
    errorMessage.value = 'Failed to process authentication response'
    loading.value = false
  }
})
</script>

<style scoped lang="postcss">
.auth-validation {
  @apply flex justify-center items-center min-h-screen text-center bg-gray-50;
}

.loading-container {
  @apply flex flex-col items-center justify-center;
}

.spinner-container {
  @apply flex flex-col items-center justify-center p-8 rounded-lg bg-white shadow-md w-[250px] h-[150px] mb-4;
}

.authenticating-text {
  @apply text-base text-gray-600 font-medium;
}

.error-container {
  @apply p-8 rounded-lg shadow-md bg-white max-w-md text-red-600;
}

button {
  @apply mt-6 py-3 px-6 bg-blue-500 text-white border-none rounded-md cursor-pointer font-medium transition-colors;
}

button:hover {
  @apply bg-blue-600;
}
</style>