// Configuration utility - detects environment and uses appropriate env var method
const isAppRunner = () => {
  // In App Runner (production), import.meta.env won't exist or will be different
  // In Vite dev mode, import.meta.env.DEV will be true
  const isViteDev = import.meta?.env?.DEV === true;
  
  // If not in Vite dev mode, assume we're in App Runner/production
  return !isViteDev;
}

const getEnvVar = (key) => {
  const viteKey = key.startsWith('VITE_') ? key : `VITE_${key}`;
  
  if (isAppRunner()) {
    // In App Runner (production), use process.env
    const processKey = viteKey.replace('VITE_', ''); // Remove VITE_ prefix for process.env
    console.log(`Accessing process.env.${processKey} (App Runner mode)`);
    return typeof process !== 'undefined' ? process.env[processKey] : undefined;
  } else {
    // In development (Vite), use import.meta.env
    console.log(`Accessing import.meta.env.${viteKey} (Local mode)`);
    return import.meta.env[viteKey];
  }
}

export const getApiUrl = () => {
  // Return exactly what's in the environment variable, no fallbacks
  const apiUrl = getEnvVar('VITE_API_URL');
  
  if (!apiUrl) {
    const envMethod = isAppRunner() ? 'process.env.API_URL' : 'import.meta.env.VITE_API_URL';
    const envVarName = isAppRunner() ? 'API_URL' : 'VITE_API_URL';
    console.error(`${envVarName} environment variable is not set! (Using ${envMethod})`);
    throw new Error(`API URL not configured. Please set ${envVarName} environment variable. (Environment: ${isAppRunner() ? 'App Runner' : 'Local'})`);
  }
  
  console.log(`API URL detected: ${apiUrl} (${isAppRunner() ? 'App Runner' : 'Local'} mode)`);
  return apiUrl;
}

export const getEnvironment = () => {
  // Return exactly what's in the environment variable, no fallbacks
  const environment = getEnvVar('VITE_APP_ENV');
  
  if (!environment) {
    const envMethod = isAppRunner() ? 'process.env.APP_ENV' : 'import.meta.env.VITE_APP_ENV';
    const envVarName = isAppRunner() ? 'APP_ENV' : 'VITE_APP_ENV';
    console.error(`${envVarName} environment variable is not set! (Using ${envMethod})`);
    throw new Error(`Environment not configured. Please set ${envVarName} environment variable. (Environment: ${isAppRunner() ? 'App Runner' : 'Local'})`);
  }
  
  console.log(`Environment detected: ${environment} (${isAppRunner() ? 'App Runner' : 'Local'} mode)`);
  return environment;
}

export const getConfig = (key, fallback = null) => {
  const value = getEnvVar(key);
  
  if (!value && fallback === null) {
    const viteKey = key.startsWith('VITE_') ? key : `VITE_${key}`;
    const processKey = viteKey.replace('VITE_', '');
    const envMethod = isAppRunner() ? `process.env.${processKey}` : `import.meta.env.${viteKey}`;
    const envVarName = isAppRunner() ? processKey : viteKey;
    console.error(`${envVarName} environment variable is not set! (Using ${envMethod})`);
  }
  
  return value || fallback;
}

// For backward compatibility - can be removed if not used elsewhere
export const getSecureKey = () => {
  const key = getEnvVar('VITE_SECURE_LS_KEY');
  
  if (!key) {
    const envMethod = isAppRunner() ? 'process.env.SECURE_LS_KEY' : 'import.meta.env.VITE_SECURE_LS_KEY';
    const envVarName = isAppRunner() ? 'SECURE_LS_KEY' : 'VITE_SECURE_LS_KEY';
    console.error(`${envVarName} environment variable is not set! (Using ${envMethod})`);
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