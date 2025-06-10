<template>
  <div class="auth-callback">
    <p>Processing your Google sign-in...</p>
    <!-- Optional loading spinner -->
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

onMounted(async () => {
  // Get the authorization code from URL query parameters
  const code = new URLSearchParams(window.location.search).get('code');
  
  if (code) {
    try {
      // Call your Lambda endpoint to exchange code for tokens
      const response = await axios.get(`https://71yru8o4n5.execute-api.ap-northeast-1.amazonaws.com/Prod/auth/google/callback?code=${code}`);
      
      if (response.data && response.data.access_token) {
        // Store tokens in localStorage or your auth store
        localStorage.setItem('accessToken', response.data.access_token);
        localStorage.setItem('idToken', response.data.id_token);
        localStorage.setItem('refreshToken', response.data.refresh_token);
        
        // Redirect to authenticated area of your app
        router.push('/dashboard'); // or wherever you want users to go after login
      } else {
        // Handle error case
        console.error('Invalid token response', response.data);
        router.push('/login?error=invalid_response');
      }
    } catch (error) {
      console.error('Error processing callback:', error);
      router.push('/login?error=auth_failed');
    }
  } else {
    // No code found in the URL
    console.error('No authorization code received');
    router.push('/login?error=no_code');
  }
});
</script>