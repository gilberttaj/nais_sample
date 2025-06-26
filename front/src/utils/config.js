// Configuration utility using Vite environment variables
export const getApiUrl = () => {
  // Return exactly what's in the environment variable, no fallbacks
  const apiUrl = import.meta.env.VITE_API_URL;
  
  if (!apiUrl) {
    console.error('VITE_API_URL environment variable is not set!');
    throw new Error('API URL not configured. Please set VITE_API_URL environment variable.');
  }
  
  return apiUrl;
}

export const getEnvironment = () => {
  // Return exactly what's in the environment variable, no fallbacks
  const environment = import.meta.env.VITE_APP_ENV;
  
  if (!environment) {
    console.error('VITE_APP_ENV environment variable is not set!');
    throw new Error('Environment not configured. Please set VITE_APP_ENV environment variable.');
  }
  
  return environment;
}

export const getConfig = (key, fallback = null) => {
  const envKey = `VITE_${key.toUpperCase()}`;
  const value = import.meta.env[envKey];
  
  if (!value && fallback === null) {
    console.error(`${envKey} environment variable is not set!`);
  }
  
  return value || fallback;
}

// For backward compatibility - can be removed if not used elsewhere
export const getSecureKey = () => {
  const key = import.meta.env.VITE_SECURE_LS_KEY;
  
  if (!key) {
    console.error('VITE_SECURE_LS_KEY environment variable is not set!');
    return 'test-encryption-key-32-characters-long'; // This one can have a fallback for security
  }
  
  return key;
}