import { createPinia } from 'pinia'

export const pinia = createPinia()

// Export stores
export { useAuthStore } from './auth.js'