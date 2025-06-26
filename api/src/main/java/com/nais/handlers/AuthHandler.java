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
        
        switch (path) {
            case "/auth/health":
                if ("GET".equals(httpMethod)) {
                    return handleHealthCheck(input);
                }
                break;
                
            // OAuth Step 1: Initiate OAuth flow
            case "/auth/google/login":
                if ("GET".equals(httpMethod)) {
                    return initiateGoogleOAuth(context);
                }
                break;
                
            // OAuth Step 2: Handle OAuth callback from Google
            case "/auth/google/callback":
                if ("GET".equals(httpMethod)) {
                    return handleGoogleOAuthCallback(input);
                }
                break;
                
            case "/auth/token/refresh":
                if ("POST".equals(httpMethod)) {
                    return handleTokenRefresh(input);
                }
                break;
                
            case "/auth/logout":
                if ("POST".equals(httpMethod)) {
                    return handleLogout(input);
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
                    redirectUri = "https://u7ipl8kq14.execute-api.ap-northeast-1.amazonaws.com/dev/auth/google/callback"; // fallback
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
}