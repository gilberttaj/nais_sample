<template>
  <div class="auth-validation-container">
    <div class="spinner"></div>
    <h1>Validating session...</h1>
    <p>Please wait, we're securely signing you in.</p>
    <p v-if="error" class="error-message">
      An error occurred: {{ error }}. Redirecting to sign-in page...
    </p>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Hub } from 'aws-amplify/utils';
import { getCurrentUser } from 'aws-amplify/auth';

const router = useRouter();
const error = ref(null);

// This function will be called when the Hub detects an event
const listener = async (data) => {
  switch (data.payload.event) {
    case 'signInWithRedirect':
      // This event fires when the redirect process starts. We just wait.
      console.log('AuthValidation: signInWithRedirect event started.');
      break;
    case 'signInWithRedirect_failure':
      // Handle any failure during the redirect process
      console.error('AuthValidation: signInWithRedirect failed:', data.payload.data);
      error.value = 'Login failed. Please try again.';
      setTimeout(() => router.push('/signin'), 3000);
      break;
    case 'signedIn':
       // This is the old event name for v5, keeping it for robustness
    case 'signIn':
      // This event fires when the user is successfully signed in.
      console.log('AuthValidation: User signed in successfully!');
      // Now that we're signed in, redirect to the homepage.
      router.push('/');
      break;
  }
};

// When the component is first created, start listening for auth events.
onMounted(() => {
  console.log('AuthValidation component mounted. Listening for auth events.');
  // The 'auth' channel is where all Amplify Auth events are published.
  const unsubscribe = Hub.listen('auth', listener);

  // Also, check if the user is already signed in, in case the event was missed.
  // This can happen on a page refresh.
  getCurrentUser()
    .then(user => {
      if (user) {
        console.log('AuthValidation: User is already signed in, redirecting.');
        router.push('/');
      }
    })
    .catch(() => {
      // No user signed in, which is expected on this page. Do nothing.
      console.log('AuthValidation: No active user session found. Waiting for redirect to complete.');
    });

  // When the component is destroyed, stop listening to prevent memory leaks.
  onUnmounted(() => {
    console.log('AuthValidation component unmounted. Unsubscribing from auth events.');
    unsubscribe();
  });
});
</script>

<style scoped>
.auth-validation-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80vh;
  text-align: center;
  font-family: sans-serif;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #09f;
  animation: spin 1s ease infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

h1 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

p {
  color: #666;
}

.error-message {
  margin-top: 1rem;
  color: #d9534f;
  font-weight: bold;
}
</style>