// Configuration utility - simplified for build-time environment variables
// All environment variables are now injected at build time (GitHub Actions or local build)
const getEnvVar = (key) => {
  const viteKey = key.startsWith('VITE_') ? key : `VITE_${key}`;
  return import.meta.env[viteKey];
}

export const getApiUrl = () => {
  const apiUrl = getEnvVar('VITE_API_URL');
  
  if (!apiUrl) {
    console.error('VITE_API_URL environment variable is not set!');
    throw new Error('API URL not configured. Please set VITE_API_URL environment variable.');
  }
  
  console.log(`API URL: ${apiUrl}`);
  return apiUrl;
}

export const getEnvironment = () => {
  const environment = getEnvVar('VITE_APP_ENV') || 'development';
  console.log(`Environment: ${environment}`);
  return environment;
}

export const getConfig = (key, fallback = null) => {
  const value = getEnvVar(key);
  return value || fallback;
}

export const getSecureKey = () => {
  const key = getEnvVar('VITE_SECURE_LS_KEY');
  
  if (!key) {
    console.error('VITE_SECURE_LS_KEY environment variable is not set!');
    return 'development-encryption-key-32-chars-minimum'; // Fallback for development
  }
  
  return key;
}

// Debug function to show current configuration
export const debugEnvironment = () => {
  console.log('Configuration Debug:', {
    apiUrl: getEnvVar('VITE_API_URL'),
    environment: getEnvVar('VITE_APP_ENV'),
    appName: getEnvVar('VITE_APP_NAME'),
    appVersion: getEnvVar('VITE_APP_VERSION'),
    hasSecureKey: !!getEnvVar('VITE_SECURE_LS_KEY'),
    isViteDev: import.meta.env?.DEV
  });
}