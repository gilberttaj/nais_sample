import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import { getApiUrl, getEnvironment } from '@/utils/config.js'
import ls from '@/utils/secureLS'

export const useAuthStore = defineStore('auth', () => {
  // State
  const isLoading = ref(false)
  const user = ref(null)
  const tokens = ref({
    idToken: null,
    accessToken: null,
    refreshToken: null,
    tokenType: null,
    expirationTime: null
  })

  // Getters
  const isAuthenticated = computed(() => {
    return !!tokens.value.accessToken && 
           tokens.value.expirationTime && 
           Date.now() < tokens.value.expirationTime
  })

  const isLocalMode = computed(() => {
    const environment = getEnvironment()
    const apiUrl = getApiUrl()
    return environment === 'development' || 
           apiUrl.includes('localhost') || 
           apiUrl.includes('127.0.0.1')
  })

  // Actions
  const initializeAuth = () => {
    // Load tokens from secure localStorage on app start
    try {
      const idToken = ls.get('id_token')
      const accessToken = ls.get('access_token')
      const refreshToken = ls.get('refresh_token')
      const tokenType = ls.get('token_type')
      const expirationTime = ls.get('token_expiration')

      if (accessToken && expirationTime) {
        tokens.value = {
          idToken,
          accessToken,
          refreshToken,
          tokenType,
          expirationTime: parseInt(expirationTime)
        }

        // Parse user info from ID token if available
        if (idToken) {
          user.value = parseUserFromToken(idToken)
        }
      }
    } catch (error) {
      console.error('Error initializing auth:', error)
      clearAuth()
    }
  }

  const signInWithGoogle = async (router = null) => {
    try {
      isLoading.value = true
      const apiUrl = getApiUrl()
      
      console.log(`Initiating Google sign-in - API URL: ${apiUrl}, Environment: ${getEnvironment()}, Local Mode: ${isLocalMode.value}`)
      
      const response = await axios.get(`${apiUrl}/auth/google/login`)
      
      if (response.data && response.data.redirectUrl) {
        if (isLocalMode.value && response.data.local_mode) {
          // Local development mode - simulate OAuth callback directly
          console.log('Local development mode detected - simulating OAuth callback')
          await handleLocalCallback(router)
        } else {
          // Production mode - redirect to Google login
          window.location.href = response.data.redirectUrl
        }
      }
    } catch (error) {
      console.error('Error initiating Google login:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const handleLocalCallback = async (router = null) => {
    try {
      const apiUrl = getApiUrl()
      
      // Simulate the OAuth callback with dummy parameters
      const callbackResponse = await axios.get(`${apiUrl}/auth/google/callback?local=true`)
      
      if (callbackResponse.data && callbackResponse.data.status === 'success') {
        const data = callbackResponse.data
        
        console.log('Local authentication successful!', {
          email: data.email,
          message: data.message,
          localMode: data.local_mode,
          tokenType: data.token_type,
          expiresIn: data.expires_in,
          // Don't log actual tokens for security
          hasIdToken: !!data.id_token,
          hasAccessToken: !!data.access_token,
          hasRefreshToken: !!data.refresh_token
        })
        
        // Store tokens and redirect to AuthValidation for consistent flow
        const params = new URLSearchParams({
          status: 'success',
          message: data.message,
          email: data.email,
          id_token: data.id_token,
          access_token: data.access_token,
          refresh_token: data.refresh_token,
          expires_in: data.expires_in.toString(),
          token_type: data.token_type,
          local_mode: 'true'
        })
        
        // Use router to navigate to AuthValidation or fallback to window.location
        if (router) {
          router.push(`/auth/validation?${params.toString()}`)
        } else {
          window.location.href = `/auth/validation?${params.toString()}`
        }
      } else {
        throw new Error('Authentication failed: ' + (callbackResponse.data?.message || 'Unknown error'))
      }
    } catch (error) {
      console.error('Error in local callback:', error)
      throw error
    }
  }

  const storeAuthTokens = (tokenData) => {
    try {
      // Store in secure localStorage
      ls.set('id_token', tokenData.id_token)
      ls.set('access_token', tokenData.access_token)
      
      if (tokenData.refresh_token) {
        ls.set('refresh_token', tokenData.refresh_token)
      }
      
      ls.set('token_type', tokenData.token_type)
      
      // Store expiration time as timestamp
      const expiresIn = parseInt(tokenData.expires_in)
      const expirationTime = Date.now() + expiresIn * 1000
      ls.set('token_expiration', expirationTime.toString())
      
      // Update store state
      tokens.value = {
        idToken: tokenData.id_token,
        accessToken: tokenData.access_token,
        refreshToken: tokenData.refresh_token,
        tokenType: tokenData.token_type,
        expirationTime
      }
      
      // Parse user info from ID token
      if (tokenData.id_token) {
        user.value = parseUserFromToken(tokenData.id_token)
      }
      
      console.log('Auth tokens stored successfully')
    } catch (error) {
      console.error('Error storing auth tokens:', error)
      throw error
    }
  }

  const refreshTokens = async () => {
    try {
      if (!tokens.value.refreshToken) {
        throw new Error('No refresh token available')
      }

      const apiUrl = getApiUrl()
      const response = await axios.post(`${apiUrl}/auth/token/refresh`, {
        refreshToken: tokens.value.refreshToken,
        username: user.value?.email
      })

      if (response.data && response.data.id_token) {
        storeAuthTokens({
          id_token: response.data.id_token,
          access_token: response.data.access_token,
          refresh_token: tokens.value.refreshToken, // Keep existing refresh token
          token_type: response.data.token_type,
          expires_in: response.data.expires_in
        })
        
        return true
      }
      
      return false
    } catch (error) {
      console.error('Error refreshing tokens:', error)
      clearAuth()
      return false
    }
  }

  const signOut = async () => {
    try {
      isLoading.value = true
      
      if (tokens.value.accessToken && !isLocalMode.value) {
        // Call logout endpoint for production
        const apiUrl = getApiUrl()
        await axios.post(`${apiUrl}/auth/logout`, {
          accessToken: tokens.value.accessToken
        })
      }
    } catch (error) {
      console.error('Error during logout:', error)
    } finally {
      clearAuth()
      isLoading.value = false
    }
  }

  const clearAuth = () => {
    // Clear localStorage
    ls.remove('id_token')
    ls.remove('access_token')
    ls.remove('refresh_token')
    ls.remove('token_type')
    ls.remove('token_expiration')
    
    // Clear store state
    tokens.value = {
      idToken: null,
      accessToken: null,
      refreshToken: null,
      tokenType: null,
      expirationTime: null
    }
    user.value = null
    
    console.log('Auth cleared')
  }

  const parseUserFromToken = (idToken) => {
    try {
      // Simple JWT parsing - split by dots and decode the payload
      const parts = idToken.split('.')
      if (parts.length < 2) {
        return null
      }
      
      // Decode the payload (second part)
      const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
      const payloadJson = JSON.parse(payload)
      
      return {
        email: payloadJson.email,
        name: payloadJson.name || payloadJson.given_name + ' ' + payloadJson.family_name,
        given_name: payloadJson.given_name,
        family_name: payloadJson.family_name,
        email_verified: payloadJson.email_verified
      }
    } catch (error) {
      console.error('Error parsing user from token:', error)
      return null
    }
  }

  const getAuthHeaders = () => {
    if (!tokens.value.accessToken) {
      return {}
    }
    
    return {
      'Authorization': `${tokens.value.tokenType || 'Bearer'} ${tokens.value.accessToken}`
    }
  }

  // Auto-refresh tokens before they expire
  const setupTokenRefresh = () => {
    const checkTokenExpiration = () => {
      if (tokens.value.expirationTime && tokens.value.refreshToken) {
        const timeUntilExpiry = tokens.value.expirationTime - Date.now()
        const refreshThreshold = 5 * 60 * 1000 // 5 minutes before expiry
        
        if (timeUntilExpiry < refreshThreshold && timeUntilExpiry > 0) {
          refreshTokens()
        }
      }
    }
    
    // Check every minute
    setInterval(checkTokenExpiration, 60000)
  }

  return {
    // State
    isLoading,
    user,
    tokens,
    
    // Getters
    isAuthenticated,
    isLocalMode,
    
    // Actions
    initializeAuth,
    signInWithGoogle,
    storeAuthTokens,
    refreshTokens,
    signOut,
    clearAuth,
    getAuthHeaders,
    setupTokenRefresh
  }
})