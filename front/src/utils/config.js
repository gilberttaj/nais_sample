// Configuration utility - uses .env.local for development, AWS Secrets Manager for production
let configCache = null;

const isProduction = () => {
  return import.meta.env.PROD || import.meta.env.VITE_APP_ENV === 'production';
};

const fetchConfigFromSecrets = async () => {
  try {
    const { SecretsManagerClient, GetSecretValueCommand } = await import('@aws-sdk/client-secrets-manager');
    
    const client = new SecretsManagerClient({ 
      region: 'ap-northeast-1',
    });
    
    const command = new GetSecretValueCommand({
      SecretId: 'nais-frontend-config'
    });
    
    const response = await client.send(command);
    const config = JSON.parse(response.SecretString);
    
    console.log('✅ Configuration loaded from AWS Secrets Manager');
    return config;
  } catch (error) {
    console.error('❌ Failed to fetch configuration from Secrets Manager:', error);
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
  
  if (isProduction()) {
    console.log('🔧 Loading configuration from AWS Secrets Manager (Production)');
    configCache = await fetchConfigFromSecrets();
  } else {
    console.log('🔧 Loading configuration from .env.local (Development)');
    configCache = getConfigFromEnv();
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