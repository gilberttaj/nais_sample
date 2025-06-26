#!/bin/bash

# Runtime configuration script for AWS App Runner
# This script updates the config.js file with environment variables at runtime

# Find the config file in the build output
CONFIG_FILE="./dist/config.js"
if [ ! -f "$CONFIG_FILE" ]; then
    CONFIG_FILE="./public/config.js"
fi
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Creating config.js in dist directory"
    mkdir -p ./dist
    CONFIG_FILE="./dist/config.js"
fi

# Create the runtime configuration
cat > "$CONFIG_FILE" << EOF
// Runtime configuration - Updated by AWS App Runner
window.appConfig = {
  apiUrl: '${API_URL:-https://91xl0mky4e-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev}',
  environment: '${NODE_ENV:-production}',
  region: '${AWS_REGION:-ap-northeast-1}',
  secureLS_Key: '${SECURE_LS_KEY:-default-secure-key-change-in-production}'
}
EOF

echo "Runtime configuration updated:"
cat "$CONFIG_FILE"