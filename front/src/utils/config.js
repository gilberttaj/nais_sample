// Configuration utility - fetches from Express API endpoint
let configCache = null;

const isDevelopment = () => {
  return import.meta.env.DEV || import.meta.env.VITE_APP_ENV === 'development';
};

const fetchConfigFromAPI = async () => {
  try {
    const response = await fetch('/api/config');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const config = await response.json();
    console.log('✅ Configuration loaded from Express API');
    return config;
  } catch (error) {
    console.error('❌ Failed to fetch configuration from API:', error);
    throw error;
  }
};

const getConfigFromEnv = () => {
  return {
    apiUrl: import.meta.env.VITE_API_URL || 'http://localhost:3000/api',
    secureLsKey: import.meta.env.VITE_SECURE_LS_KEY || 'development-encryption-key-32-chars-minimum',
    appName: import.meta.env.VITE_APP_NAME || 'NAIS Application',
    appVersion: import.meta.env.VITE_APP_VERSION || '1.0.0',
    environment: import.meta.env.VITE_APP_ENV || 'development'
  };
};

const getConfig = async () => {
  if (configCache) return configCache;
  
  if (isDevelopment()) {
    console.log('🔧 Loading configuration from .env.local (Development)');
    configCache = getConfigFromEnv();
  } else {
    console.log('🔧 Loading configuration from Express API (Production)');
    configCache = await fetchConfigFromAPI();
  }
  
  return configCache;
};

export const getApiUrl = async () => {
  const config = await getConfig();
  return config.apiUrl;
};

export const getEnvironment = async () => {
  const config = await getConfig();
  return config.environment;
};

export const getSecureKey = async () => {
  const config = await getConfig();
  return config.secureLsKey;
};

export const getConfigValue = async (key, fallback = null) => {
  const config = await getConfig();
  return config[key] || fallback;
};

// Debug function to show current configuration
export const debugEnvironment = async () => {
  const config = await getConfig();
  console.log('Configuration Debug:', {
    ...config,
    secureLsKey: config.secureLsKey ? '***' : 'not set',
    source: isProduction() ? 'AWS Secrets Manager' : '.env.local',
    isProduction: isProduction(),
    isViteDev: import.meta.env?.DEV
  });
};