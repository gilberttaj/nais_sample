<template>
  <div class="w-full max-w-md">
    <GoogleSignInButton @click="handleGoogleSignIn" :disabled="isGoogleLoading">
      <template #icon v-if="isGoogleLoading">
        <LoadingSpinner :size="20" :dot-size="4" class="mr-2" />
      </template>
    </GoogleSignInButton>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import GoogleSignInButton from '@/components/molecules/GoogleSignInButton.vue' 
import LoadingSpinner from '@/components/atoms/LoadingSpinner.vue'
import axios from 'axios'


const isGoogleLoading = ref(false)

// Methods
const handleGoogleSignIn = async () => {
  // ls.set('access_token', '1234567890')
  // router.push('/')
  try {
    isGoogleLoading.value = true;
    const API_URL = import.meta.env.VITE_API_URL || 'https://91xl0mky4e-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev';
    const response = await axios.get(`${API_URL}/auth/google/login`);
    
    if (response.data && response.data.redirectUrl) {
      // Redirect the browser to Google login
      window.location.href = response.data.redirectUrl;
    }
  } catch (error) {
    console.error('Error initiating Google login:', error);
    isGoogleLoading.value = false;
  }
  // Note: We don't set isGoogleLoading to false on success because we're redirecting
}


</script>

<style scoped>
.mr-2 {
  margin-right: 8px;
}
</style>