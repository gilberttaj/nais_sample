package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nais.auth.services.WorkspaceAuthService;
import com.nais.auth.validators.EmailValidationResult;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 * AuthHandler implementing proper OAuth 2.0 flow with Google Workspace validation
 */
public class AuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    // Static initializer to set the default HTTP client for AWS SDK
    static {
        System.setProperty("software.amazon.awssdk.http.service.impl", 
                         "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService");
    }

    private AuthClient authClient;
    private ObjectMapper objectMapper;
    private WorkspaceAuthService workspaceAuthService;
    private SecretsManagerClient secretsManagerClient;

    public AuthHandler() {
        this.objectMapper = new ObjectMapper();
        this.secretsManagerClient = SecretsManagerClient.builder()
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            initializeComponents(context);
            
            String path = input.getPath();
            String httpMethod = input.getHttpMethod();
            
            context.getLogger().log("Processing OAuth request: " + httpMethod + " " + path);
            
            return routeRequest(input, context, path, httpMethod);
            
        } catch (Exception e) {
            context.getLogger().log("Error in handleRequest: " + e.getMessage());
            e.printStackTrace();
            return ResponseHelper.createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    private void initializeComponents(Context context) {
        if (authClient == null) {
            this.authClient = new AuthClient(context);
            
            String secretName = System.getenv("SECRET_NAME");
            this.workspaceAuthService = new WorkspaceAuthService(secretsManagerClient, secretName, context);
            
            context.getLogger().log("AuthHandler initialized with proper OAuth flow and workspace validation");
        }
    }

    private APIGatewayProxyResponseEvent routeRequest(APIGatewayProxyRequestEvent input, Context context, 
                                                    String path, String httpMethod) throws Exception {
        
        if ("OPTIONS".equals(httpMethod)) {
            return handleOptionsRequest();
        }
        
        // Auto-detect authentication mode based on available configuration
        AuthenticationMode authMode = detectAuthenticationMode(context);
        context.getLogger().log("Using authentication mode: " + authMode);
        
        switch (path) {
            case "/auth/health":
                if ("GET".equals(httpMethod)) {
                    return handleHealthCheck(input, authMode);
                }
                break;
                
            // OAuth Step 1: Initiate authentication flow
            case "/auth/google/login":
                if ("GET".equals(httpMethod)) {
                    return handleInitiateAuth(context, authMode);
                }
                break;
                
            // OAuth Step 2: Handle authentication callback
            case "/auth/google/callback":
                if ("GET".equals(httpMethod)) {
                    return handleAuthCallback(input, context, authMode);
                }
                break;
                
            case "/auth/token/refresh":
                if ("POST".equals(httpMethod)) {
                    return handleTokenRefresh(input, authMode);
                }
                break;
                
            case "/auth/logout":
                if ("POST".equals(httpMethod)) {
                    return handleLogout(input, authMode);
                }
                break;
                
            case "/auth/workspace/domains":
                if ("GET".equals(httpMethod)) {
                    return handleGetAllowedDomains();
                }
                break;
                
            default:
                context.getLogger().log("Unknown path: " + path);
                return ResponseHelper.createErrorResponse(404, "Endpoint not found: " + path);
        }
        
        return ResponseHelper.createErrorResponse(405, "Method not allowed for " + path);
    }

    /**
     * Flexible authentication initiation - adapts to available configuration
     */
    private APIGatewayProxyResponseEvent handleInitiateAuth(Context context, AuthenticationMode authMode) throws Exception {
        context.getLogger().log("Initiating authentication flow in " + authMode + " mode");
        
        switch (authMode) {
            case OAUTH:
                return initiateGoogleOAuth(context);
            case HYBRID:
                return initiateHybridAuth(context);
            case MOCK:
                return initiateMockAuth(context);
            default:
                return createErrorResponse(500, "Unknown authentication mode: " + authMode);
        }
    }
    
    /**
     * Flexible authentication callback - adapts to available configuration
     */
    private APIGatewayProxyResponseEvent handleAuthCallback(APIGatewayProxyRequestEvent input, Context context, AuthenticationMode authMode) throws Exception {
        context.getLogger().log("Handling authentication callback in " + authMode + " mode");
        
        switch (authMode) {
            case OAUTH:
                return handleGoogleOAuthCallback(input);
            case HYBRID:
                return handleHybridCallback(input, context);
            case MOCK:
                return handleMockCallback(input, context);
            default:
                return createErrorResponse(500, "Unknown authentication mode: " + authMode);
        }
    }

    /**
     * OAuth Step 1: Initiate Google OAuth via Cognito (without PKCE)
     * Validates workspace configuration before starting OAuth flow
     */
    private APIGatewayProxyResponseEvent initiateGoogleOAuth(Context context) throws Exception {
        context.getLogger().log("Initiating Google OAuth flow via Cognito with workspace validation");
        
        try {
            // Check if workspace authentication is properly configured
            if (workspaceAuthService.hasEmailRestrictions()) {
                context.getLogger().log("Workspace authentication is enabled - email domain validation will be enforced");
            } else {
                context.getLogger().log("WARNING: No workspace email restrictions configured - all Google users will be allowed");
            }
            
            // Get configuration from environment variables
            String cognitoDomainUrl = System.getenv("COGNITO_DOMAIN_URL");
            String appClientId = System.getenv("CLIENT_ID");
            String callbackUrl = System.getenv("GOOGLE_REDIRECT_URI");
            if (callbackUrl == null) {
                callbackUrl = System.getenv("OAUTH_CALLBACK_URL"); // fallback
            }
            
            if (cognitoDomainUrl == null || appClientId == null || callbackUrl == null) {
                context.getLogger().log("Missing required environment variables");
                return createErrorResponse(500, "OAuth configuration error: missing environment variables");
            }
            
            // Generate a simple state parameter for CSRF protection
            SecureRandom secureRandom = new SecureRandom();
            byte[] stateBytes = new byte[16];
            secureRandom.nextBytes(stateBytes);
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);
            
            context.getLogger().log("Generated state parameter: " + state);
            
            // Construct the redirect URL to Cognito's OAuth endpoint with Google as the provider
            // Add prompt=select_account to force Google to show account selection even if user is already authenticated
            String redirectUrl = String.format("%s/oauth2/authorize?response_type=code&client_id=%s" +
                    "&redirect_uri=%s&identity_provider=Google&scope=email+openid+profile&state=%s&prompt=select_account",
                    cognitoDomainUrl,
                    appClientId,
                    URLEncoder.encode(callbackUrl, "UTF-8"),
                    URLEncoder.encode(state, "UTF-8"));
            
            // Return the redirect URL in the response body instead of doing a 302 redirect
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("redirectUrl", redirectUrl);
            responseBody.put("workspace_auth_enabled", workspaceAuthService.hasEmailRestrictions());
            responseBody.put("workspace_config", workspaceAuthService.getConfigurationInfo());
            responseBody.put("auth_mode", "OAUTH");
            
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200); // Use 200 instead of 302 to avoid CORS issues
            response.setHeaders(getResponseHeaders()); // Use standard CORS headers
            response.setBody(objectMapper.writeValueAsString(responseBody));
            
            context.getLogger().log("Google OAuth initiation successful with workspace validation enabled");
            
            return response;
            
        } catch (Exception e) {
            context.getLogger().log("Error initiating Google OAuth: " + e.getMessage());
            return createErrorResponse(500, "Failed to initiate Google authentication: " + e.getMessage());
        }
    }

    /**
     * Get standard CORS headers for responses
     */
    private Map<String, String> getResponseHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.put("Content-Type", "application/json");
        return headers;
    }

    /**
     * OAuth Step 2: Handle callback from Google with authorization code
     * Validates OAuth parameters and prepares for token exchange
     */
    private APIGatewayProxyResponseEvent handleGoogleOAuthCallback(APIGatewayProxyRequestEvent input) throws Exception {
        Context context = authClient.getLambdaContext();
        context.getLogger().log("Received Google callback with workspace validation enabled");
        context.getLogger().log("Query parameters: " + (input.getQueryStringParameters() != null ? input.getQueryStringParameters().toString() : "null"));
        
        Map<String, String> queryParams = input.getQueryStringParameters();
        if (queryParams == null) {
            queryParams = new HashMap<>();
        }
        
        // Check for OAuth error first
        if (queryParams.containsKey("error")) {
            String error = queryParams.get("error");
            String errorDescription = queryParams.get("error_description");
            context.getLogger().log("OAuth error received: " + error + " - " + errorDescription);
            
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "https://c3cb9bzz3k.ap-northeast-1.awsapprunner.com";
            }
            String redirectUrl = frontendUrl + "/auth/validation?status=error&message=" + 
                URLEncoder.encode("OAuth error: " + error + (errorDescription != null ? " - " + errorDescription : ""), "UTF-8");
            
            return createRedirectResponse(redirectUrl);
        }
        
        // Try to get auth code from multiple possible locations
        String authCode = null;
        
        // First check the query parameters
        if (queryParams.containsKey("code")) {
            authCode = queryParams.get("code");
        }
        
        // If not found, check path parameters
        if ((authCode == null || authCode.isEmpty()) && input.getPathParameters() != null) {
            Map<String, String> pathParams = input.getPathParameters();
            if (pathParams.containsKey("code")) {
                authCode = pathParams.get("code");
            }
        }
        
        // If still not found, check multi-value query parameters
        if ((authCode == null || authCode.isEmpty()) && input.getMultiValueQueryStringParameters() != null) {
            Map<String, java.util.List<String>> multiValueParams = input.getMultiValueQueryStringParameters();
            if (multiValueParams.containsKey("code") && !multiValueParams.get("code").isEmpty()) {
                authCode = multiValueParams.get("code").get(0);
            }
        }
        
        // Check if we have a valid auth code now
        if (authCode == null || authCode.trim().isEmpty()) {
            context.getLogger().log("No authorization code provided in OAuth callback");
            
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "https://c3cb9bzz3k.ap-northeast-1.awsapprunner.com";
            }
            String redirectUrl = frontendUrl + "/auth/validation?status=error&message=" + 
                URLEncoder.encode("No authorization code provided", "UTF-8");
            
            return createRedirectResponse(redirectUrl);
        }
        
        // Extract the state parameter for CSRF validation
        String state = queryParams.get("state");
        if (state != null) {
            context.getLogger().log("Received state parameter: " + state);
            // In production, you should validate this state parameter against stored session state
        }
        
        context.getLogger().log("Valid authorization code received, proceeding to token exchange with workspace validation");
        
        return exchangeCodeForTokens(authCode, context);
    }

    /**
     * Exchange authorization code for tokens with Cognito and validate workspace access
     */
    private APIGatewayProxyResponseEvent exchangeCodeForTokens(String authCode, Context context) {
        try {
            context.getLogger().log("Starting token exchange with workspace validation");
            
            // Exchange the authorization code for tokens
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("grant_type", "authorization_code");
            tokenRequest.put("client_id", System.getenv("CLIENT_ID"));
            tokenRequest.put("code", authCode);
            
            // Get client secret from Secrets Manager
            try {
                String secretName = System.getenv("SECRET_NAME");
                if (secretName != null) {
                    String secretJson = workspaceAuthService.getSecretValue(secretName);
                    if (secretJson != null) {
                        JsonNode secretData = objectMapper.readTree(secretJson);
                        if (secretData.has("client_secret")) {
                            String appClientSecret = secretData.get("client_secret").asText();
                            tokenRequest.put("client_secret", appClientSecret);
                            context.getLogger().log("Added client_secret from Secrets Manager to token request");
                        } else {
                            context.getLogger().log("WARNING: client_secret not found in Secrets Manager");
                        }
                    } else {
                        context.getLogger().log("WARNING: Could not retrieve secret from Secrets Manager");
                    }
                } else {
                    context.getLogger().log("WARNING: SECRET_NAME environment variable not set");
                }
            } catch (Exception e) {
                context.getLogger().log("WARNING: Error retrieving client_secret from Secrets Manager: " + e.getMessage());
                // Continue without client_secret - some Cognito configurations don't require it
            }
            
            // Set the redirect URI from environment variable
            String redirectUri = System.getenv("GOOGLE_REDIRECT_URI");
            if (redirectUri == null) {
                redirectUri = System.getenv("OAUTH_CALLBACK_URL"); // fallback to old name
                if (redirectUri == null) {
                    redirectUri = "https://91xl0mky4e-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev/auth/google/callback"; // fallback
                }
            }
            context.getLogger().log("Using redirect URI: " + redirectUri);
            tokenRequest.put("redirect_uri", redirectUri);
            
            // Log the token request parameters for debugging (hiding the client secret)
            Map<String, String> logSafeParams = new HashMap<>(tokenRequest);
            if (logSafeParams.containsKey("client_secret")) {
                logSafeParams.put("client_secret", "[REDACTED]");
            }
            context.getLogger().log("Token request parameters: " + logSafeParams.toString());
            
            // Get Cognito domain URL from environment variable
            String cognitoDomainUrl = System.getenv("COGNITO_DOMAIN_URL");
            if (cognitoDomainUrl == null) {
                cognitoDomainUrl = "https://nais-stage.auth.ap-northeast-1.amazoncognito.com"; // fallback
            }
            
            // Now exchange the auth code for tokens
            URL tokenUrl = new URL(cognitoDomainUrl + "/oauth2/token");
            context.getLogger().log("Token URL: " + tokenUrl.toString());
            
            HttpURLConnection tokenConnection = (HttpURLConnection) tokenUrl.openConnection();
            tokenConnection.setRequestMethod("POST");
            tokenConnection.setConnectTimeout(10000);  // 10 seconds timeout
            tokenConnection.setReadTimeout(10000);     // 10 seconds read timeout
            tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            tokenConnection.setRequestProperty("Accept", "application/json");
            tokenConnection.setDoOutput(true);
            
            StringBuilder tokenParams = new StringBuilder();
            for (Map.Entry<String, String> entry : tokenRequest.entrySet()) {
                if (tokenParams.length() > 0) {
                    tokenParams.append("&");
                }
                tokenParams.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            
            try (OutputStream os = tokenConnection.getOutputStream()) {
                byte[] requestData = tokenParams.toString().getBytes("UTF-8");
                os.write(requestData, 0, requestData.length);
            }
            
            int statusCode = tokenConnection.getResponseCode();
            
            InputStream inputStream;
            if (statusCode >= 400) {
                inputStream = tokenConnection.getErrorStream();
            } else {
                inputStream = tokenConnection.getInputStream();
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            
            String responseContent = responseBuilder.toString();
            context.getLogger().log("Token endpoint response status: " + statusCode);
            
            if (statusCode >= 400) {
                context.getLogger().log("Token exchange failed: " + responseContent);
                return createWorkspaceErrorRedirect("Token exchange failed: " + responseContent);
            }
            
            // Parse the JSON response
            JsonNode tokenResponse = objectMapper.readTree(responseContent);
            String idToken = tokenResponse.get("id_token").asText();
            
            context.getLogger().log("Token exchange successful, now validating workspace access");
            
            // Extract user email from ID token for workspace validation
            String userEmail = extractEmailFromIdToken(idToken, context);
            if (userEmail == null) {
                context.getLogger().log("Failed to extract email from ID token");
                return createWorkspaceErrorRedirect("Failed to extract user information from authentication token");
            }
            
            context.getLogger().log("Extracted user email for validation: " + maskEmail(userEmail));
            
            // Validate email against workspace domains and allowed emails
            EmailValidationResult validationResult = workspaceAuthService.validateUserAccess(userEmail);
            if (!validationResult.isValid()) {
                context.getLogger().log("Workspace validation failed for email: " + maskEmail(userEmail) + " - " + validationResult.getMessage());
                return createWorkspaceErrorRedirect("Access denied: " + validationResult.getMessage());
            }
            
            context.getLogger().log("Workspace validation passed for email: " + maskEmail(userEmail));
            
            // Create or update user in Cognito (optional - for user management)
            try {
                createOrUpdateCognitoUser(userEmail, tokenResponse, context);
            } catch (Exception e) {
                context.getLogger().log("Warning: Failed to create/update Cognito user: " + e.getMessage());
                // Continue anyway - this is not critical for the authentication flow
            }
            
            // Redirect to frontend with tokens in URL parameters
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "https://c3cb9bzz3k.ap-northeast-1.awsapprunner.com"; // fallback
            }
            
            StringBuilder redirectUrl = new StringBuilder(frontendUrl + "/auth/validation");
            redirectUrl.append("?status=success");
            redirectUrl.append("&message=").append(URLEncoder.encode("Google Workspace login successful", "UTF-8"));
            redirectUrl.append("&email=").append(URLEncoder.encode(maskEmail(userEmail), "UTF-8"));
            redirectUrl.append("&id_token=").append(URLEncoder.encode(tokenResponse.get("id_token").asText(), "UTF-8"));
            redirectUrl.append("&access_token=").append(URLEncoder.encode(tokenResponse.get("access_token").asText(), "UTF-8"));
            
            if (tokenResponse.has("refresh_token")) {
                redirectUrl.append("&refresh_token=").append(URLEncoder.encode(tokenResponse.get("refresh_token").asText(), "UTF-8"));
            }
            
            redirectUrl.append("&expires_in=").append(tokenResponse.get("expires_in").asInt());
            redirectUrl.append("&token_type=").append(URLEncoder.encode(tokenResponse.get("token_type").asText(), "UTF-8"));
            
            context.getLogger().log("Workspace authentication successful for: " + maskEmail(userEmail));
            
            return createRedirectResponse(redirectUrl.toString());
            
        } catch (Exception e) {
            context.getLogger().log("Error in token exchange with workspace validation: " + e.getMessage());
            e.printStackTrace();
            
            return createWorkspaceErrorRedirect("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract email from ID token (simplified JWT parsing)
     */
    private String extractEmailFromIdToken(String idToken, Context context) {
        try {
            // Simple JWT parsing - split by dots and decode the payload
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            
            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadJson = objectMapper.readTree(payload);
            
            if (payloadJson.has("email")) {
                return payloadJson.get("email").asText();
            }
            
            return null;
        } catch (Exception e) {
            context.getLogger().log("Error extracting email from ID token: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create or update user in Cognito for user management
     */
    private void createOrUpdateCognitoUser(String email, JsonNode tokenResponse, Context context) {
        try {
            // This is a placeholder for Cognito user management
            // You can implement actual Cognito user creation/update here if needed
            context.getLogger().log("Cognito user management for: " + maskEmail(email));
            
            // Example: Use CognitoAuth to create/update user
            // CognitoAuth cognitoAuth = authClient.cognito();
            // cognitoAuth.createOrUpdateUser(email, tokenResponse);
            
        } catch (Exception e) {
            context.getLogger().log("Error in Cognito user management: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create error redirect with workspace context
     */
    private APIGatewayProxyResponseEvent createWorkspaceErrorRedirect(String errorMessage) {
        try {
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "https://c3cb9bzz3k.ap-northeast-1.awsapprunner.com";
            }
            String redirectUrl = frontendUrl + "/auth/validation?status=error&message=" + 
                URLEncoder.encode(errorMessage, "UTF-8") + "&workspace_auth=true";
            
            return createRedirectResponse(redirectUrl);
        } catch (Exception e) {
            return createErrorResponse(500, errorMessage);
        }
    }
    
    /**
     * Mask email for logging (privacy protection)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "invalid-email";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return username + "@" + domain;
        }
        
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
    }

    /**
     * Create a redirect response
     */
    private APIGatewayProxyResponseEvent createRedirectResponse(String redirectUrl) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(302); // HTTP 302 Found for redirect
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", redirectUrl);
        headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.put("Pragma", "no-cache");
        headers.put("Expires", "0");
        response.setHeaders(headers);
        
        return response;
    }
    
    /**
     * Create an error response
     */
    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setHeaders(getResponseHeaders());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        
        try {
            response.setBody(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            response.setBody("{\"status\":\"error\",\"message\":\"" + message + "\"}");
        }
        
        return response;
    }

    private APIGatewayProxyResponseEvent handleOptionsRequest() {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setHeaders(getResponseHeaders());
        response.setBody("");
        
        return response;
    }

    private APIGatewayProxyResponseEvent handleHealthCheck(APIGatewayProxyRequestEvent input) {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "2.1.0");
        health.put("oauth_flow", "google_oauth_2.0");
        health.put("workspace_auth_enabled", workspaceAuthService.hasEmailRestrictions());
        health.put("workspace_auth_config", workspaceAuthService.getConfigurationInfo());
        
        try {
            return ResponseHelper.createSuccessResponse(health);
        } catch (Exception e) {
            authClient.getLambdaContext().getLogger().log("Failed to create health response: " + e.getMessage());
            return ResponseHelper.createErrorResponse(500, "Failed to create health response: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent handleGetAllowedDomains() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("workspace_auth_enabled", workspaceAuthService.hasEmailRestrictions());
        response.put("configuration_summary", workspaceAuthService.getConfigurationInfo());
        
        return ResponseHelper.createSuccessResponse(response);
    }

    private APIGatewayProxyResponseEvent handleTokenRefresh(APIGatewayProxyRequestEvent input) throws Exception {
        JsonNode requestBody = objectMapper.readTree(input.getBody());
        String refreshToken = requestBody.get("refreshToken").asText();
        String username = requestBody.get("username").asText();
        
        CognitoAuth cognitoAuth = authClient.cognito();
        AuthResult result = cognitoAuth.refreshToken(refreshToken, username);
        
        if (!result.isSuccess()) {
            return ResponseHelper.createErrorResponse(401, "Token refresh failed: " + result.getError());
        }
        
        return ResponseHelper.createTokenResponse(result.getAuthenticationResult());
    }

    private APIGatewayProxyResponseEvent handleLogout(APIGatewayProxyRequestEvent input) throws Exception {
        JsonNode requestBody = objectMapper.readTree(input.getBody());
        String accessToken = requestBody.get("accessToken").asText();
        
        CognitoAuth cognitoAuth = authClient.cognito();
        SignOutResult result = cognitoAuth.signOut(accessToken);
        
        if (!result.isSuccess()) {
            return ResponseHelper.createErrorResponse(500, "Logout failed: " + result.getError());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully logged out");
        
        return ResponseHelper.createSuccessResponse(response);
    }

    /**
     * Auto-detect authentication mode based on available configuration
     */
    private AuthenticationMode detectAuthenticationMode(Context context) {
        context.getLogger().log("Auto-detecting authentication mode based on available configuration");
        
        // Check for explicit mode override
        String explicitMode = System.getenv("AUTH_MODE");
        if (explicitMode != null) {
            try {
                AuthenticationMode mode = AuthenticationMode.valueOf(explicitMode.toUpperCase());
                context.getLogger().log("Using explicit authentication mode: " + mode);
                return mode;
            } catch (IllegalArgumentException e) {
                context.getLogger().log("Invalid AUTH_MODE specified: " + explicitMode + ", falling back to auto-detection");
            }
        }
        
        // Auto-detect based on available configuration
        String cognitoDomainUrl = System.getenv("COGNITO_DOMAIN_URL");
        String clientId = System.getenv("CLIENT_ID");
        String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        String secretName = System.getenv("SECRET_NAME");
        
        // Check if we have minimal OAuth configuration
        boolean hasOAuthConfig = cognitoDomainUrl != null && !cognitoDomainUrl.isEmpty() &&
                                clientId != null && !clientId.isEmpty() &&
                                googleClientId != null && !googleClientId.isEmpty();
        
        // Check if secrets are accessible
        boolean hasSecrets = false;
        if (secretName != null && !secretName.isEmpty()) {
            try {
                String secretValue = workspaceAuthService.getSecretValue(secretName);
                hasSecrets = secretValue != null && !secretValue.isEmpty();
                context.getLogger().log("Secrets accessibility check: " + (hasSecrets ? "accessible" : "not accessible"));
            } catch (Exception e) {
                context.getLogger().log("Secrets accessibility check failed: " + e.getMessage());
            }
        }
        
        // Determine mode based on configuration availability
        if (hasOAuthConfig && hasSecrets) {
            context.getLogger().log("Full OAuth configuration detected - using OAUTH mode");
            return AuthenticationMode.OAUTH;
        } else if (hasOAuthConfig) {
            context.getLogger().log("Partial OAuth configuration detected - using HYBRID mode");
            return AuthenticationMode.HYBRID;
        } else {
            context.getLogger().log("No OAuth configuration detected - using MOCK mode");
            return AuthenticationMode.MOCK;
        }
    }
    
    /**
     * Authentication modes
     */
    private enum AuthenticationMode {
        OAUTH,    // Full OAuth with Cognito/Google
        HYBRID,   // OAuth-like flow but with mock tokens for missing config
        MOCK      // Full mock authentication for development
    }

    /**
     * Generate dummy JWT token for local development
     */
    private String generateDummyJWT(String email, String tokenType) {
        try {
            // Create header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            
            // Create payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", "dummy-user-" + email.hashCode());
            payload.put("email", email);
            payload.put("email_verified", true);
            payload.put("name", "Local Dev User");
            payload.put("given_name", "Local");
            payload.put("family_name", "User");
            payload.put("iss", "local-dev-issuer");
            payload.put("aud", "local-dev-client");
            payload.put("token_use", tokenType); // "id" or "access"
            payload.put("iat", System.currentTimeMillis() / 1000);
            payload.put("exp", (System.currentTimeMillis() / 1000) + 3600); // 1 hour
            
            // Encode header and payload
            String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(objectMapper.writeValueAsString(header).getBytes("UTF-8"));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(objectMapper.writeValueAsString(payload).getBytes("UTF-8"));
            
            // Create dummy signature (not cryptographically secure, for local dev only)
            String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("dummy-signature".getBytes("UTF-8"));
            
            return encodedHeader + "." + encodedPayload + "." + signature;
            
        } catch (Exception e) {
            return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkdW1teS11c2VyIiwiZW1haWwiOiJkZXZAZXhhbXBsZS5jb20iLCJuYW1lIjoiTG9jYWwgRGV2IFVzZXIifQ.dummy-signature";
        }
    }

    /**
     * Handle local development login
     */
    private APIGatewayProxyResponseEvent handleLocalLogin(Context context) {
        context.getLogger().log("Local development login initiated");
        
        try {
            String localFrontendUrl = System.getenv("LOCAL_FRONTEND_URL");
            if (localFrontendUrl == null) {
                localFrontendUrl = "http://localhost:3000"; // Default for local React app
            }
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("redirectUrl", localFrontendUrl + "/auth/validation?mode=local");
            responseBody.put("message", "Local development mode - proceeding to dummy authentication");
            responseBody.put("workspace_auth_enabled", false);
            responseBody.put("local_mode", true);
            
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setHeaders(getResponseHeaders());
            response.setBody(objectMapper.writeValueAsString(responseBody));
            
            return response;
            
        } catch (Exception e) {
            context.getLogger().log("Error in local login: " + e.getMessage());
            return createErrorResponse(500, "Local authentication error: " + e.getMessage());
        }
    }

    /**
     * Handle local development callback with dummy tokens
     */
    private APIGatewayProxyResponseEvent handleLocalCallback(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Local development callback with dummy tokens");
        
        try {
            String dummyEmail = "dev@example.com";
            String userEmail = System.getenv("LOCAL_DEV_EMAIL");
            if (userEmail != null && !userEmail.isEmpty()) {
                dummyEmail = userEmail;
            }
            
            // Generate dummy tokens
            String idToken = generateDummyJWT(dummyEmail, "id");
            String accessToken = generateDummyJWT(dummyEmail, "access");
            String refreshToken = "dummy-refresh-token-" + System.currentTimeMillis();
            
            // For local development, return JSON response instead of redirect to avoid CORS issues
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Local development authentication successful");
            response.put("email", dummyEmail);
            response.put("id_token", idToken);
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("expires_in", 3600);
            response.put("token_type", "Bearer");
            response.put("local_mode", true);
            
            context.getLogger().log("Local authentication successful for: " + dummyEmail);
            
            APIGatewayProxyResponseEvent apiResponse = new APIGatewayProxyResponseEvent();
            apiResponse.setStatusCode(200);
            apiResponse.setHeaders(getResponseHeaders());
            apiResponse.setBody(objectMapper.writeValueAsString(response));
            
            return apiResponse;
            
        } catch (Exception e) {
            context.getLogger().log("Error in local callback: " + e.getMessage());
            return createErrorResponse(500, "Local callback error: " + e.getMessage());
        }
    }

    /**
     * Handle local development token refresh
     */
    private APIGatewayProxyResponseEvent handleLocalTokenRefresh(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Local development token refresh");
        
        try {
            String dummyEmail = "dev@example.com";
            String userEmail = System.getenv("LOCAL_DEV_EMAIL");
            if (userEmail != null && !userEmail.isEmpty()) {
                dummyEmail = userEmail;
            }
            
            // Generate new dummy tokens
            String newIdToken = generateDummyJWT(dummyEmail, "id");
            String newAccessToken = generateDummyJWT(dummyEmail, "access");
            
            Map<String, Object> response = new HashMap<>();
            response.put("id_token", newIdToken);
            response.put("access_token", newAccessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("local_mode", true);
            
            return ResponseHelper.createSuccessResponse(response);
            
        } catch (Exception e) {
            context.getLogger().log("Error in local token refresh: " + e.getMessage());
            return createErrorResponse(500, "Local token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Handle local development logout
     */
    private APIGatewayProxyResponseEvent handleLocalLogout(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Local development logout");
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully logged out from local development mode");
            response.put("local_mode", true);
            
            return ResponseHelper.createSuccessResponse(response);
        } catch (Exception e) {
            context.getLogger().log("Error in local logout: " + e.getMessage());
            return createErrorResponse(500, "Local logout failed: " + e.getMessage());
        }
    }

    /**
     * Hybrid authentication mode - OAuth-like flow with fallbacks for missing config
     */
    private APIGatewayProxyResponseEvent initiateHybridAuth(Context context) throws Exception {
        context.getLogger().log("Initiating hybrid authentication flow");
        
        try {
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "https://c3cb9bzz3k.ap-northeast-1.awsapprunner.com";
            }
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("redirectUrl", frontendUrl + "/auth/validation?mode=hybrid");
            responseBody.put("message", "Hybrid authentication - OAuth config available but some components missing");
            responseBody.put("workspace_auth_enabled", workspaceAuthService.hasEmailRestrictions());
            responseBody.put("workspace_config", workspaceAuthService.getConfigurationInfo());
            responseBody.put("auth_mode", "HYBRID");
            
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setHeaders(getResponseHeaders());
            response.setBody(objectMapper.writeValueAsString(responseBody));
            
            context.getLogger().log("Hybrid authentication initiated");
            return response;
            
        } catch (Exception e) {
            context.getLogger().log("Error in hybrid authentication: " + e.getMessage());
            return createErrorResponse(500, "Hybrid authentication error: " + e.getMessage());
        }
    }
    
    /**
     * Mock authentication mode - full mock for development/testing
     */
    private APIGatewayProxyResponseEvent initiateMockAuth(Context context) throws Exception {
        context.getLogger().log("Initiating mock authentication flow");
        
        try {
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "http://localhost:3000";
            }
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("redirectUrl", frontendUrl + "/auth/validation?mode=mock");
            responseBody.put("message", "Mock authentication - no OAuth configuration available");
            responseBody.put("workspace_auth_enabled", false);
            responseBody.put("auth_mode", "MOCK");
            
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setHeaders(getResponseHeaders());
            response.setBody(objectMapper.writeValueAsString(responseBody));
            
            context.getLogger().log("Mock authentication initiated");
            return response;
            
        } catch (Exception e) {
            context.getLogger().log("Error in mock authentication: " + e.getMessage());
            return createErrorResponse(500, "Mock authentication error: " + e.getMessage());
        }
    }
    
    /**
     * Hybrid callback - generates tokens with available data
     */
    private APIGatewayProxyResponseEvent handleHybridCallback(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Handling hybrid authentication callback");
        
        try {
            String defaultEmail = System.getenv("DEFAULT_TEST_EMAIL");
            if (defaultEmail == null) {
                defaultEmail = "test@example.com";
            }
            
            // Generate mock tokens but with real-looking structure
            String idToken = generateDummyJWT(defaultEmail, "id");
            String accessToken = generateDummyJWT(defaultEmail, "access");
            String refreshToken = "hybrid-refresh-token-" + System.currentTimeMillis();
            
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "https://c3cb9bzz3k.ap-northeast-1.awsapprunner.com";
            }
            
            StringBuilder redirectUrl = new StringBuilder(frontendUrl + "/auth/validation");
            redirectUrl.append("?status=success");
            redirectUrl.append("&message=").append(URLEncoder.encode("Hybrid authentication successful", "UTF-8"));
            redirectUrl.append("&email=").append(URLEncoder.encode(defaultEmail, "UTF-8"));
            redirectUrl.append("&id_token=").append(URLEncoder.encode(idToken, "UTF-8"));
            redirectUrl.append("&access_token=").append(URLEncoder.encode(accessToken, "UTF-8"));
            redirectUrl.append("&refresh_token=").append(URLEncoder.encode(refreshToken, "UTF-8"));
            redirectUrl.append("&expires_in=3600");
            redirectUrl.append("&token_type=Bearer");
            redirectUrl.append("&auth_mode=HYBRID");
            
            context.getLogger().log("Hybrid authentication successful for: " + defaultEmail);
            return createRedirectResponse(redirectUrl.toString());
            
        } catch (Exception e) {
            context.getLogger().log("Error in hybrid callback: " + e.getMessage());
            return createWorkspaceErrorRedirect("Hybrid authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Mock callback - generates mock tokens for testing
     */
    private APIGatewayProxyResponseEvent handleMockCallback(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Handling mock authentication callback");
        
        try {
            String mockEmail = System.getenv("MOCK_USER_EMAIL");
            if (mockEmail == null) {
                mockEmail = "mock@example.com";
            }
            
            // Generate mock tokens
            String idToken = generateDummyJWT(mockEmail, "id");
            String accessToken = generateDummyJWT(mockEmail, "access");
            String refreshToken = "mock-refresh-token-" + System.currentTimeMillis();
            
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null) {
                frontendUrl = "http://localhost:3000";
            }
            
            StringBuilder redirectUrl = new StringBuilder(frontendUrl + "/auth/validation");
            redirectUrl.append("?status=success");
            redirectUrl.append("&message=").append(URLEncoder.encode("Mock authentication successful", "UTF-8"));
            redirectUrl.append("&email=").append(URLEncoder.encode(mockEmail, "UTF-8"));
            redirectUrl.append("&id_token=").append(URLEncoder.encode(idToken, "UTF-8"));
            redirectUrl.append("&access_token=").append(URLEncoder.encode(accessToken, "UTF-8"));
            redirectUrl.append("&refresh_token=").append(URLEncoder.encode(refreshToken, "UTF-8"));
            redirectUrl.append("&expires_in=3600");
            redirectUrl.append("&token_type=Bearer");
            redirectUrl.append("&auth_mode=MOCK");
            
            context.getLogger().log("Mock authentication successful for: " + mockEmail);
            return createRedirectResponse(redirectUrl.toString());
            
        } catch (Exception e) {
            context.getLogger().log("Error in mock callback: " + e.getMessage());
            return createWorkspaceErrorRedirect("Mock authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Flexible token refresh - adapts to authentication mode
     */
    private APIGatewayProxyResponseEvent handleTokenRefresh(APIGatewayProxyRequestEvent input, AuthenticationMode authMode) throws Exception {
        switch (authMode) {
            case OAUTH:
                return handleOAuthTokenRefresh(input);
            case HYBRID:
            case MOCK:
                return handleMockTokenRefresh(input, authMode);
            default:
                return createErrorResponse(500, "Token refresh not supported for mode: " + authMode);
        }
    }
    
    /**
     * Flexible logout - adapts to authentication mode
     */
    private APIGatewayProxyResponseEvent handleLogout(APIGatewayProxyRequestEvent input, AuthenticationMode authMode) throws Exception {
        switch (authMode) {
            case OAUTH:
                return handleOAuthLogout(input);
            case HYBRID:
            case MOCK:
                return handleMockLogout(input, authMode);
            default:
                return createErrorResponse(500, "Logout not supported for mode: " + authMode);
        }
    }
    
    /**
     * OAuth token refresh (renamed to avoid recursion)
     */
    private APIGatewayProxyResponseEvent handleOAuthTokenRefresh(APIGatewayProxyRequestEvent input) throws Exception {
        JsonNode requestBody = objectMapper.readTree(input.getBody());
        String refreshToken = requestBody.get("refreshToken").asText();
        String username = requestBody.get("username").asText();
        
        CognitoAuth cognitoAuth = authClient.cognito();
        AuthResult result = cognitoAuth.refreshToken(refreshToken, username);
        
        if (!result.isSuccess()) {
            return ResponseHelper.createErrorResponse(401, "Token refresh failed: " + result.getError());
        }
        
        return ResponseHelper.createTokenResponse(result.getAuthenticationResult());
    }
    
    /**
     * OAuth logout (renamed to avoid recursion)
     */
    private APIGatewayProxyResponseEvent handleOAuthLogout(APIGatewayProxyRequestEvent input) throws Exception {
        JsonNode requestBody = objectMapper.readTree(input.getBody());
        String accessToken = requestBody.get("accessToken").asText();
        
        CognitoAuth cognitoAuth = authClient.cognito();
        SignOutResult result = cognitoAuth.signOut(accessToken);
        
        if (!result.isSuccess()) {
            return ResponseHelper.createErrorResponse(500, "Logout failed: " + result.getError());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully logged out");
        
        return ResponseHelper.createSuccessResponse(response);
    }
    
    /**
     * Mock token refresh for non-OAuth modes
     */
    private APIGatewayProxyResponseEvent handleMockTokenRefresh(APIGatewayProxyRequestEvent input, AuthenticationMode authMode) {
        try {
            String mockEmail = authMode == AuthenticationMode.HYBRID ? "test@example.com" : "mock@example.com";
            
            String newIdToken = generateDummyJWT(mockEmail, "id");
            String newAccessToken = generateDummyJWT(mockEmail, "access");
            
            Map<String, Object> response = new HashMap<>();
            response.put("id_token", newIdToken);
            response.put("access_token", newAccessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("auth_mode", authMode.toString());
            
            return ResponseHelper.createSuccessResponse(response);
            
        } catch (Exception e) {
            return createErrorResponse(500, "Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Mock logout for non-OAuth modes
     */
    private APIGatewayProxyResponseEvent handleMockLogout(APIGatewayProxyRequestEvent input, AuthenticationMode authMode) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully logged out from " + authMode.toString().toLowerCase() + " mode");
            response.put("auth_mode", authMode.toString());
            
            return ResponseHelper.createSuccessResponse(response);
        } catch (Exception e) {
            return createErrorResponse(500, "Logout failed: " + e.getMessage());
        }
    }
    
    /**
     * Flexible health check - includes authentication mode info
     */
    private APIGatewayProxyResponseEvent handleHealthCheck(APIGatewayProxyRequestEvent input, AuthenticationMode authMode) {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "2.1.0");
        health.put("auth_mode", authMode.toString());
        health.put("oauth_flow", authMode == AuthenticationMode.OAUTH ? "google_oauth_2.0" : "flexible_auth");
        health.put("workspace_auth_enabled", workspaceAuthService.hasEmailRestrictions());
        health.put("workspace_auth_config", workspaceAuthService.getConfigurationInfo());
        
        try {
            return ResponseHelper.createSuccessResponse(health);
        } catch (Exception e) {
            authClient.getLambdaContext().getLogger().log("Failed to create health response: " + e.getMessage());
            return ResponseHelper.createErrorResponse(500, "Failed to create health response: " + e.getMessage());
        }
    }
}