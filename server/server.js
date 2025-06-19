import express from 'express';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import crypto from 'crypto';
import axios from 'axios';
import cookieParser from 'cookie-parser';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 8080;

// Cognito configuration
const COGNITO_DOMAIN = 'nais-stage.auth.ap-northeast-1.amazoncognito.com';
const COGNITO_URL = `https://${COGNITO_DOMAIN}`;
const CLIENT_ID = '443r5ntohpimv3fao1i6p7sdkp';
const SCOPES = 'phone email openid profile aws.cognito.signin.user.admin';

// Enable CORS
app.use(cors({
  origin: true,
  credentials: true
}));

// Parse JSON bodies
app.use(express.json());

// Parse cookies
app.use(cookieParser());

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

// Store for PKCE state and code verifiers (in production, use Redis or database)
const pkceStore = new Map();

// Helper function to generate PKCE parameters
function generatePKCE() {
  const codeVerifier = crypto.randomBytes(32).toString('base64url');
  const codeChallenge = crypto.createHash('sha256').update(codeVerifier).digest('base64url');
  return { codeVerifier, codeChallenge };
}

// Helper function to generate state parameter
function generateState() {
  return crypto.randomBytes(32).toString('base64url');
}

// OAuth login endpoint - redirects to Cognito
app.get('/auth/login', (req, res) => {
  try {
    const { codeVerifier, codeChallenge } = generatePKCE();
    const state = generateState();
    
    // Store PKCE parameters for later verification
    pkceStore.set(state, { codeVerifier, timestamp: Date.now() });
    
    // Clean up old entries (older than 10 minutes)
    for (const [key, value] of pkceStore.entries()) {
      if (Date.now() - value.timestamp > 10 * 60 * 1000) {
        pkceStore.delete(key);
      }
    }
    
    // Build the redirect URI based on the current request
    const protocol = req.get('x-forwarded-proto') || req.protocol;
    const host = req.get('host');
    const redirectUri = `${protocol}://${host}/auth/callback`;
    
    // Build Cognito authorization URL
    const authUrl = new URL(`${COGNITO_URL}/oauth2/authorize`);
    authUrl.searchParams.set('redirect_uri', redirectUri);
    authUrl.searchParams.set('response_type', 'code');
    authUrl.searchParams.set('client_id', CLIENT_ID);
    authUrl.searchParams.set('identity_provider', 'Google');
    authUrl.searchParams.set('scope', SCOPES);
    authUrl.searchParams.set('state', state);
    authUrl.searchParams.set('code_challenge', codeChallenge);
    authUrl.searchParams.set('code_challenge_method', 'S256');
    
    console.log('Redirecting to Cognito:', authUrl.toString());
    
    // Redirect to Cognito
    res.redirect(authUrl.toString());
  } catch (error) {
    console.error('Error in /auth/login:', error);
    res.status(500).json({ error: 'Failed to initiate login' });
  }
});

// OAuth callback endpoint - handles the code exchange
app.get('/auth/callback', async (req, res) => {
  try {
    const { code, state, error } = req.query;
    
    if (error) {
      console.error('OAuth error:', error);
      return res.redirect('/?error=oauth_error');
    }
    
    if (!code || !state) {
      console.error('Missing code or state parameter');
      return res.redirect('/?error=missing_parameters');
    }
    
    // Retrieve and validate PKCE parameters
    const pkceData = pkceStore.get(state);
    if (!pkceData) {
      console.error('Invalid or expired state parameter');
      return res.redirect('/?error=invalid_state');
    }
    
    // Clean up the used state
    pkceStore.delete(state);
    
    // Build the redirect URI
    const protocol = req.get('x-forwarded-proto') || req.protocol;
    const host = req.get('host');
    const redirectUri = `${protocol}://${host}/auth/callback`;
    
    // Exchange code for tokens
    const tokenUrl = `${COGNITO_URL}/oauth2/token`;
    const tokenData = {
      grant_type: 'authorization_code',
      client_id: CLIENT_ID,
      code: code,
      redirect_uri: redirectUri,
      code_verifier: pkceData.codeVerifier
    };
    
    console.log('Exchanging code for tokens...');
    
    const tokenResponse = await axios.post(tokenUrl, new URLSearchParams(tokenData), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    });
    
    const { access_token, id_token, refresh_token, expires_in } = tokenResponse.data;
    
    // Validate domain using the same logic as your Lambda
    const domainValidation = await validateEmailDomain(id_token);
    if (!domainValidation.isAllowed) {
      console.log('Domain validation failed:', domainValidation.message);
      return res.redirect(`/?error=domain_not_allowed&message=${encodeURIComponent(domainValidation.message)}`);
    }
    
    // Set secure HTTP-only cookies
    const cookieOptions = {
      httpOnly: true,
      secure: req.get('x-forwarded-proto') === 'https' || req.protocol === 'https',
      sameSite: 'lax',
      path: '/'
    };
    
    // Set access token cookie (expires based on token expiry)
    res.cookie('access_token', access_token, {
      ...cookieOptions,
      maxAge: (expires_in || 3600) * 1000
    });
    
    // Set ID token cookie
    res.cookie('id_token', id_token, {
      ...cookieOptions,
      maxAge: (expires_in || 3600) * 1000
    });
    
    // Set refresh token cookie (longer expiry)
    if (refresh_token) {
      res.cookie('refresh_token', refresh_token, {
        ...cookieOptions,
        maxAge: 30 * 24 * 60 * 60 * 1000 // 30 days
      });
    }
    
    console.log('Tokens set as cookies, redirecting to app...');
    
    // Redirect to the main app
    res.redirect('/?auth=success');
    
  } catch (error) {
    console.error('Error in /auth/callback:', error);
    res.redirect('/?error=token_exchange_failed');
  }
});

// Domain validation function (same logic as your Lambda)
async function validateEmailDomain(idToken) {
  try {
    // Get allowed domains from environment variable
    const allowedDomainsEnv = process.env.ALLOWED_EMAIL_DOMAINS;
    if (!allowedDomainsEnv) {
      console.error('ALLOWED_EMAIL_DOMAINS environment variable not set');
      return { isAllowed: false, message: 'Configuration error: Allowed email domains not set.' };
    }
    
    const allowedDomains = allowedDomainsEnv.split(',').map(d => d.trim().toLowerCase()).filter(d => d);
    
    // Decode JWT token (basic decode without verification for domain check)
    const tokenParts = idToken.split('.');
    if (tokenParts.length !== 3) {
      return { isAllowed: false, message: 'Invalid token format' };
    }
    
    const payload = JSON.parse(Buffer.from(tokenParts[1], 'base64url').toString());
    
    const email = payload.email;
    const emailVerified = payload.email_verified;
    
    if (!email) {
      return { isAllowed: false, message: 'Email not found in token' };
    }
    
    if (!emailVerified) {
      console.warn(`Email not verified for: ${email}`);
      // You can decide whether to allow unverified emails
      // return { isAllowed: false, message: 'Email not verified' };
    }
    
    const emailDomain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
    
    if (allowedDomains.includes(emailDomain)) {
      console.log(`Domain validation successful for: ${emailDomain}`);
      return { isAllowed: true, message: 'Email domain allowed' };
    } else {
      console.log(`Domain validation failed for: ${emailDomain}. Allowed domains: ${allowedDomains.join(', ')}`);
      return { isAllowed: false, message: `Access denied. Email domain '${emailDomain}' not authorized.` };
    }
    
  } catch (error) {
    console.error('Error validating email domain:', error);
    return { isAllowed: false, message: 'Error validating email domain' };
  }
}

// Proxy endpoint for Lambda API (for backward compatibility)
app.post('/api/auth', async (req, res) => {
  try {
    const idToken = req.cookies?.id_token;
    
    if (!idToken) {
      return res.status(401).json({ error: 'No authentication token found' });
    }
    
    // Call your existing Lambda function
    const lambdaResponse = await axios.post(
      'https://koc3wch4f0.execute-api.ap-northeast-1.amazonaws.com/Prod/auth',
      { idToken: idToken },
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    
    res.json(lambdaResponse.data);
    
  } catch (error) {
    console.error('Error calling Lambda:', error);
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({ error: 'Failed to call authentication service' });
    }
  }
});

// Logout endpoint
app.post('/auth/logout', (req, res) => {
  try {
    // Clear all auth cookies
    const cookieOptions = {
      httpOnly: true,
      secure: req.get('x-forwarded-proto') === 'https' || req.protocol === 'https',
      sameSite: 'lax',
      path: '/'
    };
    
    res.clearCookie('access_token', cookieOptions);
    res.clearCookie('id_token', cookieOptions);
    res.clearCookie('refresh_token', cookieOptions);
    
    res.json({ success: true, message: 'Logged out successfully' });
  } catch (error) {
    console.error('Error in /auth/logout:', error);
    res.status(500).json({ error: 'Failed to logout' });
  }
});

// Get current user info from cookies
app.get('/auth/user', (req, res) => {
  try {
    const idToken = req.cookies?.id_token;
    const accessToken = req.cookies?.access_token;
    
    if (!idToken || !accessToken) {
      return res.status(401).json({ error: 'Not authenticated' });
    }
    
    // Decode ID token to get user info (basic validation)
    const tokenParts = idToken.split('.');
    if (tokenParts.length !== 3) {
      return res.status(401).json({ error: 'Invalid token format' });
    }
    
    const payload = JSON.parse(Buffer.from(tokenParts[1], 'base64url').toString());
    
    // Basic token expiry check
    if (payload.exp && payload.exp < Math.floor(Date.now() / 1000)) {
      return res.status(401).json({ error: 'Token expired' });
    }
    
    res.json({
      user: {
        sub: payload.sub,
        email: payload.email,
        name: payload.name,
        picture: payload.picture,
        email_verified: payload.email_verified
      },
      isAuthenticated: true
    });
    
  } catch (error) {
    console.error('Error in /auth/user:', error);
    res.status(500).json({ error: 'Failed to get user info' });
  }
});

// Serve static Vue.js files
app.use(express.static(path.join(__dirname, '../dist')));

// Handle Vue.js routing (SPA fallback)
app.get('*', (req, res) => {
  // Don't serve index.html for API routes or auth routes
  if (req.path.startsWith('/api') || 
      req.path.startsWith('/health') ||
      req.path.startsWith('/auth')) {
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
  console.log(`OAuth endpoints available at /auth/login and /auth/callback`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully');
  process.exit(0);
});
