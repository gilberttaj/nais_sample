// Configuration utility - detects environment and uses appropriate env var method
const isAppRunner = () => {
  // Check if we're running in a Node.js runtime (like App Runner with 'serve')
  // Key indicators: process exists, we're not in Vite dev mode, or we're in production
  const hasProcess = typeof process !== 'undefined' && process.env;
  const isViteDev = typeof window !== 'undefined' && import.meta?.env?.DEV === true;
  const isProduction = hasProcess && process.env.NODE_ENV === 'production';
  
  // Use process.env if:
  // 1. We're in production (App Runner sets NODE_ENV=production)
  // 2. We have process.env but are NOT in Vite dev mode
  // 3. We're running with 'serve' (PORT is set by App Runner)
  return hasProcess && (isProduction || !isViteDev || !!process.env.PORT);
}

const getEnvVar = (key) => {
  if (isAppRunner()) {
    // App Runner: use process.env
    console.log(`App Runner mode: accessing process.env.${key}`);
    return process.env[key];
  } else {
    // Local/Vite: use import.meta.env
    const viteKey = key.startsWith('VITE_') ? key : `VITE_${key}`;
    console.log(`Local mode: accessing import.meta.env.${viteKey}`);
    return import.meta.env[viteKey];
  }
}

export const getApiUrl = () => {
  // Return exactly what's in the environment variable, no fallbacks
  const apiUrl = getEnvVar('VITE_API_URL');
  
  if (!apiUrl) {
    const envMethod = isAppRunner() ? 'process.env.VITE_API_URL' : 'import.meta.env.VITE_API_URL';
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
    const envMethod = isAppRunner() ? 'process.env.VITE_APP_ENV' : 'import.meta.env.VITE_APP_ENV';
    console.error(`VITE_APP_ENV environment variable is not set! (Using ${envMethod})`);
    throw new Error(`Environment not configured. Please set VITE_APP_ENV environment variable. (Environment: ${isAppRunner() ? 'App Runner' : 'Local'})`);
  }
  
  console.log(`Environment detected: ${environment} (${isAppRunner() ? 'App Runner' : 'Local'} mode)`);
  return environment;
}

export const getConfig = (key, fallback = null) => {
  const value = getEnvVar(key);
  
  if (!value && fallback === null) {
    const envMethod = isAppRunner() ? `process.env.${key}` : `import.meta.env.VITE_${key}`;
    console.error(`${key} environment variable is not set! (Using ${envMethod})`);
  }
  
  return value || fallback;
}

// For backward compatibility - can be removed if not used elsewhere
export const getSecureKey = () => {
  const key = getEnvVar('VITE_SECURE_LS_KEY');
  
  if (!key) {
    const envMethod = isAppRunner() ? 'process.env.VITE_SECURE_LS_KEY' : 'import.meta.env.VITE_SECURE_LS_KEY';
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