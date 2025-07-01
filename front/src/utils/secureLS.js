import SecureLS from "secure-ls";

// For development, use environment variable or fallback
// For production, this will be replaced by runtime configuration
const getEncryptionKey = () => {
  // Check if we're in development mode
  if (import.meta.env.DEV || import.meta.env.VITE_APP_ENV === 'development') {
    return import.meta.env.VITE_SECURE_LS_KEY || 'development-encryption-key-32-chars-minimum';
  }
  
  // For production, this will need to be injected at runtime
  // For now, use a fallback (this should be replaced by proper runtime config)
  return import.meta.env.VITE_SECURE_LS_KEY || 'production-key-must-be-32-chars-min';
};

// Create the configuration object
const config = {
  encodingType: "aes",
  isCompression: true,
  encryptionSecret: getEncryptionKey(),
};

// Initialize SecureLS with the configuration
const ls = new SecureLS(config);

export default ls;