<!-- src/views/AuthValidation.vue -->
<template>
    <div class="auth-validation">
      <div v-if="loading" class="loading">
        <p>Processing authentication...</p>
      </div>
      <div v-else-if="error" class="error">
        <h2>Authentication Error</h2>
        <p>{{ errorMessage }}</p>
        <button @click="goToLogin">Return to Login</button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  
  const router = useRouter()
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
  
  // Function to store auth tokens in localStorage
  const storeAuthTokens = (tokens) => {
    localStorage.setItem('id_token', tokens.id_token)
    localStorage.setItem('access_token', tokens.access_token)
    
    if (tokens.refresh_token) {
      localStorage.setItem('refresh_token', tokens.refresh_token)
    }
    
    localStorage.setItem('token_type', tokens.token_type)
    
    // Store expiration time as timestamp
    const expiresIn = parseInt(tokens.expires_in)
    const expirationTime = Date.now() + expiresIn * 1000
    localStorage.setItem('token_expiration', expirationTime.toString())
  }
  
  // Function to navigate to login page
  const goToLogin = () => {
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
        // Store tokens in localStorage
        storeAuthTokens({
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
  
  <style scoped>
  .auth-validation {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    text-align: center;
  }
  
  .loading, .error {
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    background-color: white;
    max-width: 400px;
  }
  
  .error {
    color: #d32f2f;
  }
  
  button {
    margin-top: 1rem;
    padding: 0.5rem 1rem;
    background-color: #1976d2;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  
  button:hover {
    background-color: #1565c0;
  }
  </style>