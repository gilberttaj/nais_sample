# AWS App Runner Configuration for Vue.js Frontend

## Problem with Build-time Environment Variables

The original code used `import.meta.env.VITE_API_URL`, which is a **build-time** environment variable. This doesn't work with AWS App Runner because:

- Vite bundles environment variables during build
- AWS App Runner sets environment variables at runtime
- The `VITE_API_URL` isn't available during the App Runner build process

## Solution: Runtime Configuration

We've implemented a runtime configuration approach:

### 1. Runtime Config File (`public/config.js`)
```javascript
window.appConfig = {
  apiUrl: 'your-api-url-here'
}
```

### 2. Utility Function (`src/utils/config.js`)
```javascript
export const getApiUrl = () => {
  return window.appConfig?.apiUrl || 
         import.meta.env.VITE_API_URL || 
         'fallback-url'
}
```

### 3. Usage in Components
```javascript
import { getApiUrl } from '@/utils/config.js'
const API_URL = getApiUrl();
```

## AWS App Runner Setup

### Method 1: Using apprunner.yaml (Recommended)

1. **Place `apprunner.yaml` in your repository root**
2. **Set environment variables in App Runner console:**
   - `API_URL`: Your actual API endpoint
   - `NODE_ENV`: production
   - `AWS_REGION`: your-region
   - `SECURE_LS_KEY`: Strong encryption key for SecureLS (32+ characters)

3. **The runtime script will automatically configure the app**

### Method 2: Manual Environment Variables

In AWS App Runner console, add these environment variables:

```
API_URL=https://your-api-gateway-url.amazonaws.com/prod
NODE_ENV=production
AWS_REGION=ap-northeast-1
SECURE_LS_KEY=your-strong-encryption-key-minimum-32-characters-long
```

### Method 3: Build Commands Override

If you can't use apprunner.yaml, override build commands in App Runner:

**Build command:**
```bash
npm ci && npm run build && chmod +x scripts/configure-runtime.sh
```

**Start command:**
```bash
./scripts/configure-runtime.sh && PORT=8080 npm start
```

## How It Works

1. **Build Phase:** App builds normally with fallback URLs
2. **Runtime Phase:** Script runs `configure-runtime.sh` 
3. **Script Updates:** `public/config.js` with actual environment variables
4. **App Loads:** Uses runtime configuration instead of build-time variables

## Benefits

✅ **Environment-specific URLs** without rebuilding  
✅ **Secure secrets** not baked into bundles  
✅ **Easy deployment** across environments  
✅ **No code changes** needed for different environments  

## Local Development

For local development, you can still use `.env` files:

```bash
# .env.local
VITE_API_URL=http://localhost:3000/api
VITE_SECURE_LS_KEY=development-encryption-key-32-chars
```

The utility function will use Vite env vars in development and runtime config in production.

## Testing the Configuration

After deployment, check the browser console:
```javascript
console.log(window.appConfig)
// Should show your runtime configuration
```