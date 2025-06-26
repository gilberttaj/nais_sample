// Runtime configuration utility
export const getApiUrl = () => {
  // Priority: runtime config > environment variable > fallback
  return window.appConfig?.apiUrl || 
         import.meta.env.VITE_API_URL || 
         'https://91xl0mky4e-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev'
}

export const getConfig = (key, fallback = null) => {
  return window.appConfig?.[key] || import.meta.env[`VITE_${key.toUpperCase()}`] || fallback
}