<template>
  <div class="w-full max-w-md">
    <GoogleSignInButton @click="handleGoogleSignIn" :disabled="isGoogleLoading">
      <template #icon v-if="isGoogleLoading">
        <LoadingSpinner :size="20" :dot-size="4" class="mr-2" />
      </template>
    </GoogleSignInButton>
    
    <!-- <Divider /> -->
    
    <!-- <form @submit.prevent="handleSubmit">
      <InputField
        id="email"
        label="Email"
        v-model="email"
        type="email"
        placeholder="Enter your email"
        :required="true"
        :error="emailError"
      />
      
      <InputField
        id="password"
        label="Password"
        v-model="password"
        :type="showPassword ? 'text' : 'password'"
        placeholder="Enter your password"
        :required="true"
        :error="passwordError"
        @toggle-password="togglePassword"
      >
        <template #icon>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" :class="{ 'text-primary': showPassword }" viewBox="0 0 20 20" fill="currentColor">
            <path v-if="showPassword" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" />
            <path v-if="showPassword" d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z" />
            <path v-if="!showPassword" d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
            <path v-if="!showPassword" d="M2.458 10C3.732 5.943 7.523 3 12 3c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
          </svg>
        </template>
      </InputField>
      
      <div class="flex items-center justify-between mb-6">
        <Checkbox id="remember-me" v-model="rememberMe">
          Keep me logged in
        </Checkbox>
        <a href="#" class="text-sm text-primary hover:text-primary-dark">
          Forgot password?
        </a>
      </div>
      
      <Button variant="primary" type="submit" :full-width="true">
        Sign In
      </Button>
      
      <p class="mt-4 text-center text-sm text-gray-600">
        Don't have an account?
        <router-link to="/signup" class="text-primary font-semibold hover:text-primary-dark">
          Sign Up
        </router-link>
      </p>
    </form> -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import InputField from '@/components/atoms/InputField.vue'
import Button from '@/components/atoms/Button.vue'
import Checkbox from '@/components/atoms/Checkbox.vue'
import GoogleSignInButton from '@/components/molecules/GoogleSignInButton.vue'
import Divider from '@/components/molecules/Divider.vue'
import LoadingSpinner from '@/components/atoms/LoadingSpinner.vue'
import axios from 'axios'
import ls from '@/utils/secureLS'
import { useRouter } from 'vue-router'

const router = useRouter()
// Reactive state
const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const emailError = ref('')
const passwordError = ref('')
const isGoogleLoading = ref(false)


  // Simple reactive store for auth state (in a real app, consider Pinia or Vuex)
  const authStore = reactive({
    user: null,
    idToken: null,
    isAuthenticated: false,
    isLoading: true,
    error: null,
    authApiResponse: null,
  });


  onMounted(async () => {
    authStore.isLoading = true;
    try {
      // Check authentication status from server
      const response = await axios.get('/auth/user', { 
        withCredentials: true 
      });
      
      if (response.data && response.data.isAuthenticated) {
        authStore.user = response.data.user;
        authStore.isAuthenticated = true;
        authStore.idToken = 'cookie-based'; // Token is in HTTP-only cookie
        
        // Domain validation already happened server-side during callback
        // No need to call Lambda here
      }
    } catch (error) {
      console.log('User not authenticated or session expired');
      authStore.user = null;
      authStore.idToken = null;
      authStore.isAuthenticated = false;
      // Don't set global error for this case, it's normal if not logged in
    } finally {
      authStore.isLoading = false;
    }
    
    // Check for auth success/error in URL params
    const urlParams = new URLSearchParams(window.location.search);
    const authResult = urlParams.get('auth');
    const authError = urlParams.get('error');
    const errorMessage = urlParams.get('message');
    
    if (authResult === 'success') {
      // Authentication successful, refresh user data
      try {
        const response = await axios.get('/auth/user', { 
          withCredentials: true 
        });
        if (response.data && response.data.isAuthenticated) {
          authStore.user = response.data.user;
          authStore.isAuthenticated = true;
          authStore.idToken = 'cookie-based';
        }
      } catch (error) {
        console.error('Error fetching user after auth success:', error);
      }
      
      // Clean up URL
      window.history.replaceState({}, document.title, window.location.pathname);
    } else if (authError) {
      // Handle different error types
      let displayError = 'Authentication failed';
      
      switch (authError) {
        case 'domain_not_allowed':
          displayError = errorMessage ? decodeURIComponent(errorMessage) : 'Access denied: Your email domain is not authorized';
          break;
        case 'oauth_error':
          displayError = 'OAuth authentication error occurred';
          break;
        case 'missing_parameters':
          displayError = 'Authentication request was incomplete';
          break;
        case 'invalid_state':
          displayError = 'Authentication session expired or invalid';
          break;
        case 'token_exchange_failed':
          displayError = 'Failed to complete authentication process';
          break;
        default:
          displayError = `Authentication failed: ${authError}`;
      }
      
      authStore.error = displayError;
      authStore.isAuthenticated = false;
      authStore.isLoading = false;
      
      // Clean up URL
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  });

const callAuthLambda = async (token) => {
  authStore.isLoading = true;
  authStore.authApiResponse = null;
  authStore.error = null;
  try {
    // Call your custom Lambda using axios instead of Amplify API
    const response = await axios.post('/api/auth', 
      { idToken: token }, 
      { 
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json',
        }
      }
    );

    console.log('Lambda response:', response.data);
    authStore.authApiResponse = response.data;

    if (response.data.isDomainAllowed === false) {
      authStore.error = response.data.message || 'Access denied by domain policy.';
      // Optionally: logout user if domain not allowed
      // await handleLogout();
    }
  } catch (error) {
    console.error('Error calling auth Lambda:', error);
    authStore.error = error.response?.data?.error || error.message || 'Failed to call auth API.';
    authStore.authApiResponse = { error: authStore.error };
    
    if (error.response?.status === 403) {
      authStore.error = "Access Denied: Your email domain is not authorized.";
      // Optionally sign out if domain is not authorized
    }
  } finally {
    authStore.isLoading = false;
  }
};

const handleLogout = async () => {
  try {
    await axios.post('/auth/logout', {}, { withCredentials: true });
    
    // Clear local auth state
    authStore.user = null;
    authStore.idToken = null;
    authStore.isAuthenticated = false;
    authStore.error = null;
    authStore.authApiResponse = null;
    
    // Redirect to login page or refresh
    router.push('/');
  } catch (error) {
    console.error('Error during logout:', error);
  }
};

// Methods
const handleGoogleSignIn = async () => {
  try {
    isGoogleLoading.value = true;
    authStore.isLoading = true;
    authStore.error = null;
    
    // Redirect to our server's OAuth login endpoint
    window.location.href = '/auth/login';
  } catch (error) {
    console.error('Error initiating Google sign-in:', error);
    authStore.error = error.message || 'Failed to initiate Google sign-in.';
    authStore.isLoading = false;
    isGoogleLoading.value = false;
  }
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const handleSubmit = () => {
  // Reset any previous errors
  emailError.value = ''
  passwordError.value = ''
  
  // Basic validation
  let isValid = true
  
  if (!email.value) {
    emailError.value = 'Email is required'
    isValid = false
  } else if (!/\S+@\S+\.\S+/.test(email.value)) {
    emailError.value = 'Please enter a valid email'
    isValid = false
  }
  
  if (!password.value) {
    passwordError.value = 'Password is required'
    isValid = false
  }
  
  if (isValid) {
    // Submit form logic would go here
    console.log('Form submitted', {
      email: email.value,
      password: password.value,
      rememberMe: rememberMe.value
    })
  }
}
</script>

<style scoped>
.mr-2 {
  margin-right: 8px;
}
</style>
