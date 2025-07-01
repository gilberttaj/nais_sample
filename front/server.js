import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = process.env.PORT || 8080;

// Serve static files from dist directory
app.use(express.static(path.join(__dirname, 'dist')));

// API endpoint to provide runtime configuration
app.get('/api/config', (req, res) => {
  const config = {
    apiUrl: process.env.VITE_API_URL || 'http://localhost:3000/api',
    secureLsKey: process.env.VITE_SECURE_LS_KEY || 'development-encryption-key-32-chars-minimum',
    appName: process.env.VITE_APP_NAME || 'NAIS Application tEST',
    appVersion: process.env.VITE_APP_VERSION || '1.0.0',
    environment: process.env.VITE_APP_ENV || 'production'
  };
  
  res.json(config);
});

// Handle client-side routing - serve index.html for all non-API routes
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'));
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
  console.log('Environment variables loaded:');
  console.log('- API_URL:', process.env.VITE_API_URL ? 'Set' : 'Not set');
  console.log('- SECURE_LS_KEY:', process.env.VITE_SECURE_LS_KEY ? 'Set' : 'Not set');
  console.log('- APP_ENV:', process.env.VITE_APP_ENV || 'Not set');
});