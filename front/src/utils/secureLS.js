import SecureLS from "secure-ls";
import { getConfig } from './config.js';

// Create the configuration object
const config = {
  encodingType: "aes",
  isCompression: true,
  encryptionSecret: getConfig('SECURE_LS_KEY', 'default-secure-key-change-in-production'),
};

// Initialize SecureLS with the configuration
const ls = new SecureLS(config);

export default ls;