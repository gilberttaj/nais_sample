# NAIS Auth API - Local Development Guide

This guide will help you run the NAIS Auth API locally using SAM CLI with dummy authentication for development and testing.

## Prerequisites

### Required Software
- **Java 11** (OpenJDK recommended)
- **Maven 3.6+**
- **AWS SAM CLI**
- **Docker** (for SAM local runtime)

### Installation Commands

**Ubuntu/Debian:**
```bash
# Install Java 11
sudo apt update
sudo apt install openjdk-11-jdk maven

# Install SAM CLI
pip install aws-sam-cli

# Install Docker
sudo apt install docker.io
sudo usermod -aG docker $USER
```

**macOS:**
```bash
# Install Java 11 and Maven
brew install openjdk@11 maven

# Install SAM CLI
brew install aws-sam-cli

# Install Docker Desktop from https://www.docker.com/products/docker-desktop
```

**Windows:**
```powershell
# Install using Chocolatey
choco install openjdk11 maven awscli aws-sam-cli docker-desktop

# Or download manually:
# - Java 11: https://adoptium.net/
# - Maven: https://maven.apache.org/download.cgi
# - SAM CLI: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html
```

## Quick Start

### 1. Setup Environment
```bash
cd /path/to/nais-project/api

# Copy environment template
cp .env.example .env
```

The `.env` file is already configured for local development with these key settings:
```bash
ENVIRONMENT=local
LOCAL_MODE=true
LOCAL_DEV_EMAIL=dev@example.com
LOCAL_FRONTEND_URL=http://localhost:3000
```

### 2. Build and Run (Using Scripts)

**Option A - Using Bash Script (Git Bash/WSL/Linux/macOS):**
```bash
./dev.sh build     # Build the application
./dev.sh start     # Start local server at http://127.0.0.1:8080
```

**Option B - Using PowerShell Script (Windows):**
```powershell
.\dev.ps1 build     # Build the application
.\dev.ps1 start     # Start local server at http://127.0.0.1:8080
```

**Option C - Manual Commands:**
```bash
# Build the application
sam build --template-file template-local.yaml

# Start the local API server
sam local start-api --template-file template-local.yaml --env-vars env.json --port 8080
```

### 3. Verify Setup
Open your browser and test these endpoints:

- **Health Check**: http://127.0.0.1:8080/auth/health
- **Login**: http://127.0.0.1:8080/auth/google/login
- **Callback**: http://127.0.0.1:8080/auth/google/callback

## Development Scripts

### Available Commands

| Command | Bash | PowerShell | Description |
|---------|------|------------|-------------|
| Build | `./dev.sh build` | `.\dev.ps1 build` | Compile and build the application |
| Start | `./dev.sh start` | `.\dev.ps1 start` | Start the local API server |
| Restart | `./dev.sh restart` | `.\dev.ps1 restart` | Rebuild and restart (use after code changes) |
| Clean | `./dev.sh clean` | `.\dev.ps1 clean` | Remove build artifacts |
| Help | `./dev.sh help` | `.\dev.ps1 help` | Show available commands |

### Typical Development Workflow

1. **Initial Setup:**
   ```bash
   ./dev.sh build
   ./dev.sh start
   ```

2. **After Making Code Changes:**
   ```bash
   ./dev.sh restart  # Rebuilds and restarts automatically
   ```

3. **If Build Gets Corrupted:**
   ```bash
   ./dev.sh clean
   ./dev.sh build
   ./dev.sh start
   ```

## Local Authentication Flow

### How It Works

In local development mode, the API automatically detects the environment and uses dummy authentication instead of real Google OAuth:

1. **Detection**: API checks for `ENVIRONMENT=local` or `LOCAL_MODE=true`
2. **Dummy Tokens**: Generates valid JWT tokens with test data
3. **No External Dependencies**: No AWS/Cognito/Google services required

### API Endpoints

| Endpoint | Method | Description | Response |
|----------|--------|-------------|----------|
| `/auth/health` | GET | Health check with local mode indicator | JSON status |
| `/auth/google/login` | GET | Initiate authentication (returns dummy response) | JSON with `local_mode: true` |
| `/auth/google/callback` | GET | OAuth callback (returns dummy tokens) | JSON with tokens |
| `/auth/token/refresh` | POST | Refresh tokens (returns new dummy tokens) | JSON with new tokens |
| `/auth/logout` | POST | Logout (dummy response) | JSON success message |

### Example Token Response
```json
{
  "status": "success",
  "message": "Local development authentication successful",
  "email": "dev@example.com",
  "id_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "dummy-refresh-token-1703234567890",
  "expires_in": 3600,
  "token_type": "Bearer",
  "local_mode": true
}
```

## Configuration

### Environment Variables

The following environment variables control local development behavior:

| Variable | Default | Description |
|----------|---------|-------------|
| `ENVIRONMENT` | `local` | Set to `local` to enable dummy authentication |
| `LOCAL_MODE` | `true` | Explicit local mode toggle |
| `LOCAL_DEV_EMAIL` | `dev@example.com` | Email address for dummy tokens |
| `LOCAL_FRONTEND_URL` | `http://localhost:3000` | Frontend URL for redirects |

### Customization

To use a different email for testing:
```bash
# Edit env.json or set in template-local.yaml
LOCAL_DEV_EMAIL=john.doe@example.com
```

To change the frontend URL:
```bash
LOCAL_FRONTEND_URL=http://localhost:5173  # For Vite
```

## Frontend Integration

### Auto-Detection
The frontend automatically detects local development when:
- Running on `localhost` or `127.0.0.1`
- Common development ports (3000, 5173, 8080)

### Configuration
The frontend config (`/front/public/config.js`) automatically points to:
- **Local**: `http://127.0.0.1:8080` (your SAM API)
- **Production**: Your production API URL

### Authentication Flow
1. User clicks "Sign In" on frontend
2. Frontend detects local mode and calls local API
3. API returns dummy tokens directly (no redirects)
4. Frontend stores tokens and navigates to dashboard
5. Tokens can be used for subsequent API calls

## Troubleshooting

### Common Issues

**1. Docker/Image Pull Errors:**
```bash
# Skip image pull to use cached version
sam local start-api --skip-pull-image --template-file template-local.yaml --env-vars env.json --port 8080
```

**2. Java Version Mismatch:**
```bash
# Check Java version
java -version

# Should show Java 11. If not, set JAVA_HOME:
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

**3. Build Failures:**
```bash
# Clean and rebuild
./dev.sh clean
./dev.sh build
```

**4. Port Already in Use:**
```bash
# Use a different port
sam local start-api --template-file template-local.yaml --env-vars env.json --port 8081
```

**5. CORS Issues:**
- The API includes proper CORS headers for local development
- Frontend automatically detects local mode and handles accordingly

### Debug Logging

The API logs detailed information about:
- Local mode detection
- Environment variables
- Authentication flow
- Token generation

Check the console output when running `./dev.sh start` for debugging information.

### Resetting Environment

If you encounter persistent issues:
```bash
# Clean everything and start fresh
./dev.sh clean
rm -rf .aws-sam
docker system prune -f
./dev.sh build
./dev.sh start
```

## Production vs Local Differences

| Feature | Local Development | Production |
|---------|------------------|------------|
| Authentication | Dummy JWT tokens | Real Google OAuth |
| AWS Services | None required | Cognito, Secrets Manager, etc. |
| Email Validation | Bypassed | Enforced via workspace domains |
| Token Validation | Dummy signatures | Real JWT validation |
| CORS | Permissive for development | Restricted to specific origins |

## Next Steps

Once your local development is working:

1. **Test your REST API**: Use the generated tokens to authenticate requests
2. **Frontend Integration**: Ensure your frontend correctly handles local vs production modes
3. **API Development**: Build your actual business logic endpoints
4. **Production Deployment**: Use the main `template.yaml` for AWS deployment

## Support

If you encounter issues:
1. Check the console logs for error messages
2. Verify all prerequisites are installed correctly
3. Ensure Docker is running
4. Try the troubleshooting steps above

For more detailed AWS SAM documentation: https://docs.aws.amazon.com/serverless-application-model/