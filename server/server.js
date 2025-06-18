import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 8080;

// Cognito configuration
const COGNITO_DOMAIN = 'nais-stage.auth.ap-northeast-1.amazoncognito.com';
const COGNITO_URL = `https://${COGNITO_DOMAIN}`;

// Enable CORS
app.use(cors({
  origin: true,
  credentials: true
}));

// Health check for AppRunner
app.get('/health', (req, res) => {
  res.status(200).send('healthy');
});

// API health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Logging middleware
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
  next();
});

// Cognito proxy routes
const cognitoProxy = createProxyMiddleware({
  target: COGNITO_URL,
  changeOrigin: true,
  secure: true,
  followRedirects: true,
  
  onProxyRes: (proxyRes, req, res) => {
    // Handle redirects
    if (proxyRes.headers.location) {
      proxyRes.headers.location = proxyRes.headers.location
        .replace(COGNITO_URL, `https://${req.get('host')}`)
        .replace(COGNITO_DOMAIN, req.get('host'));
    }
    
    // Handle cookies
    if (proxyRes.headers['set-cookie']) {
      proxyRes.headers['set-cookie'] = proxyRes.headers['set-cookie'].map(cookie => {
        return cookie.replace(/Domain=[^;]+;?/gi, '');
      });
    }
  },
  
  onProxyReq: (proxyReq, req, res) => {
    proxyReq.setHeader('Host', COGNITO_DOMAIN);
    proxyReq.setHeader('X-Forwarded-For', req.ip);
    proxyReq.setHeader('X-Forwarded-Proto', req.protocol);
    proxyReq.setHeader('X-Forwarded-Host', req.get('host'));
  },
  
  onError: (err, req, res) => {
    console.error('Cognito Proxy Error:', err);
    res.status(500).send('Unable to connect to authentication service');
  }
});

// Apply Cognito proxy to auth routes
app.use('/oauth2', cognitoProxy);
app.use('/login', cognitoProxy);
app.use('/logout', cognitoProxy);
app.use('/saml2', cognitoProxy);
app.use('/confirmSignUp', cognitoProxy);
app.use('/forgotPassword', cognitoProxy);

// Serve static Vue.js files
app.use(express.static(path.join(__dirname, '../dist')));

// Handle Vue.js routing (SPA fallback)
app.get('*', (req, res) => {
  // Don't serve index.html for API routes or auth routes
  if (req.path.startsWith('/api') || 
      req.path.startsWith('/health') ||
      req.path.startsWith('/oauth2') ||
      req.path.startsWith('/login') ||
      req.path.startsWith('/logout')) {
    return res.status(404).send('Not Found');
  }
  
  res.sendFile(path.join(__dirname, '../dist/index.html'));
});

// Error handling
app.use((error, req, res, next) => {
  console.error('Server Error:', error);
  res.status(500).send('Internal Server Error');
});

// Start server
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`Serving Vue.js app from: ${path.join(__dirname, '../dist')}`);
  console.log(`Cognito proxy target: ${COGNITO_URL}`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully');
  process.exit(0);
});
