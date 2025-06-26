// Configuration utility - detects environment and uses appropriate env var method
const isAppRunner = () => {
  // In App Runner (production), import.meta.env won't exist or will be different
  // In Vite dev mode, import.meta.env.DEV will be true
  const isViteDev = import.meta?.env?.DEV === true;
  
  // If not in Vite dev mode, assume we're in App Runner/production
  return !isViteDev;
}

const getEnvVar = (key) => {
  // Always use import.meta.env (Vite injects env vars at build time)
  const viteKey = key.startsWith('VITE_') ? key : `VITE_${key}`;
  console.log(`Accessing import.meta.env.${viteKey} (${isAppRunner() ? 'App Runner' : 'Local'} mode)`);
  return import.meta.env[viteKey];
}

export const getApiUrl = () => {
  // Return exactly what's in the environment variable, no fallbacks
  const apiUrl = getEnvVar('VITE_API_URL');
  
  if (!apiUrl) {
    const envMethod = 'import.meta.env.VITE_API_URL';
    console.error(`VITE_API_URL environment variable is not set! (Using ${envMethod})`);
    throw new Error(`API URL not configured. Please set VITE_API_URL environment variable. (Environment: ${isAppRunner() ? 'App Runner' : 'Local'})`);
  }
  
  console.log(`API URL detected: ${apiUrl} (${isAppRunner() ? 'App Runner' : 'Local'} mode)`);
  return apiUrl;
}

export const getEnvironment = () => {
  // Return exactly what's in the environment variable, no fallbacks
  const environment = getEnvVar('VITE_APP_ENV');
  
  if (!environment) {
    const envMethod = 'import.meta.env.VITE_APP_ENV';
    console.error(`VITE_APP_ENV environment variable is not set! (Using ${envMethod})`);
    throw new Error(`Environment not configured. Please set VITE_APP_ENV environment variable. (Environment: ${isAppRunner() ? 'App Runner' : 'Local'})`);
  }
  
  console.log(`Environment detected: ${environment} (${isAppRunner() ? 'App Runner' : 'Local'} mode)`);
  return environment;
}

export const getConfig = (key, fallback = null) => {
  const value = getEnvVar(key);
  
  if (!value && fallback === null) {
    const envMethod = `import.meta.env.VITE_${key}`;
    console.error(`${key} environment variable is not set! (Using ${envMethod})`);
  }
  
  return value || fallback;
}

// For backward compatibility - can be removed if not used elsewhere
export const getSecureKey = () => {
  const key = getEnvVar('VITE_SECURE_LS_KEY');
  
  if (!key) {
    const envMethod = 'import.meta.env.VITE_SECURE_LS_KEY';
    console.error(`VITE_SECURE_LS_KEY environment variable is not set! (Using ${envMethod})`);
    return 'test-encryption-key-32-characters-long'; // This one can have a fallback for security
  }
  
  return key;
}

// Debug function to show current environment detection
export const debugEnvironment = () => {
  console.log('Environment Detection Debug:', {
    isAppRunner: isAppRunner(),
    hasProcess: typeof process !== 'undefined',
    hasProcessEnv: typeof process !== 'undefined' && !!process.env,
    hasImportMeta: !!import.meta,
    hasImportMetaEnv: !!import.meta.env,
    isViteDev: import.meta.env?.DEV,
    nodeEnv: typeof process !== 'undefined' ? process.env.NODE_ENV : 'undefined'
  });
}