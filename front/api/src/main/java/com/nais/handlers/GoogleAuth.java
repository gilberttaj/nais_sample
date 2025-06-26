package com.nais.handlers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Google OAuth authentication handler
 * Focused on Google OAuth flow for workspace authentication
 */
public class GoogleAuth {
    private final AuthClient client;
    private final CloseableHttpClient httpClient;

    public GoogleAuth(AuthClient client) {
        this.client = client;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Generate Google OAuth authorization URL
     */
    public String getAuthorizationUrl() {
        try {
            String clientId = client.getConfig().getGoogleClientId();
            String redirectUri = client.getConfig().getGoogleRedirectUri();
            String scope = "openid email profile";
            String state = generateState(); // Optional: implement CSRF protection
            
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString()) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString()) +
                "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8.toString()) +
                "&response_type=code" +
                "&access_type=offline" +
                "&prompt=select_account";
            
            if (state != null) {
                authUrl += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8.toString());
            }
            
            client.getLambdaContext().getLogger().log("Generated Google OAuth authorization URL");
            
            return authUrl;
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Failed to generate authorization URL: " + e.getMessage());
            throw new RuntimeException("Failed to generate authorization URL", e);
        }
    }

    /**
     * Exchange Google OAuth authorization code for tokens
     */
    public TokenExchangeResult exchangeCodeForToken(String authorizationCode) {
        try {
            client.getLambdaContext().getLogger().log("Exchanging Google OAuth authorization code for tokens");
            
            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            String clientId = client.getConfig().getGoogleClientId();
            String clientSecret = getGoogleClientSecret();
            String redirectUri = client.getConfig().getGoogleRedirectUri();
            
            Map<String, String> params = new HashMap<>();
            params.put("code", authorizationCode);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("redirect_uri", redirectUri);
            params.put("grant_type", "authorization_code");
            
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formData.length() > 0) formData.append("&");
                formData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                formData.append("=");
                formData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            }
            
            HttpPost post = new HttpPost(tokenEndpoint);
            post.setEntity(new StringEntity(formData.toString(), ContentType.APPLICATION_FORM_URLENCODED));
            post.setHeader("Accept", "application/json");
            
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    client.getLambdaContext().getLogger().log("Google token exchange failed with status: " + 
                        response.getStatusLine().getStatusCode() + ", body: " + responseBody);
                    return TokenExchangeResult.failure("Google token exchange failed: HTTP " + 
                        response.getStatusLine().getStatusCode());
                }
                
                JsonNode tokenResponse = client.getObjectMapper().readTree(responseBody);
                
                if (tokenResponse.has("error")) {
                    String error = tokenResponse.get("error").asText();
                    String errorDescription = tokenResponse.has("error_description") ? 
                        tokenResponse.get("error_description").asText() : "";
                    client.getLambdaContext().getLogger().log("Google OAuth error: " + error + " - " + errorDescription);
                    return TokenExchangeResult.failure("Google OAuth error: " + error);
                }
                
                String idToken = tokenResponse.get("id_token").asText();
                String accessToken = tokenResponse.has("access_token") ? 
                    tokenResponse.get("access_token").asText() : null;
                
                client.getLambdaContext().getLogger().log("Successfully exchanged authorization code for tokens");
                
                return TokenExchangeResult.success(idToken, accessToken);
            }
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Token exchange failed: " + e.getMessage());
            return TokenExchangeResult.failure("Token exchange failed: " + e.getMessage());
        }
    }

    /**
     * Extract user information from Google ID token
     */
    public GoogleUserInfo getUserInfo(String idToken) {
        try {
            if (idToken == null || idToken.isEmpty()) {
                throw new IllegalArgumentException("ID token cannot be null or empty");
            }
            
            // Decode the JWT token (Google ID tokens are JWTs)
            DecodedJWT decodedJWT = JWT.decode(idToken);
            
            // Extract Google-specific claims
            String email = decodedJWT.getClaim("email").asString();
            String name = decodedJWT.getClaim("name").asString();
            String picture = decodedJWT.getClaim("picture").asString();
            String givenName = decodedJWT.getClaim("given_name").asString();
            String familyName = decodedJWT.getClaim("family_name").asString();
            Boolean emailVerified = decodedJWT.getClaim("email_verified").asBoolean();
            
            // Validate essential claims
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("No email found in Google ID token");
            }
            
            if (emailVerified != null && !emailVerified) {
                client.getLambdaContext().getLogger().log("Warning: Email not verified by Google for user: " + maskEmail(email));
            }
            
            // Construct full name if not present
            if (name == null && givenName != null && familyName != null) {
                name = givenName + " " + familyName;
            }
            
            client.getLambdaContext().getLogger().log("Successfully extracted user info for: " + maskEmail(email));
            
            return new GoogleUserInfo(email, name, picture, emailVerified);
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Failed to extract user info from Google ID token: " + e.getMessage());
            throw new RuntimeException("Failed to extract user info from Google ID token", e);
        }
    }

    /**
     * Verify Google ID token (optional additional verification)
     */
    public boolean verifyIdToken(String idToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(idToken);
            
            // Basic validation
            String issuer = decodedJWT.getIssuer();
            String audience = decodedJWT.getAudience().get(0);
            long exp = decodedJWT.getExpiresAt().getTime();
            long now = System.currentTimeMillis();
            
            // Check issuer
            if (!("https://accounts.google.com".equals(issuer) || "accounts.google.com".equals(issuer))) {
                client.getLambdaContext().getLogger().log("Invalid issuer in Google ID token: " + issuer);
                return false;
            }
            
            // Check audience (should match our Google client ID)
            String expectedClientId = client.getConfig().getGoogleClientId();
            if (!expectedClientId.equals(audience)) {
                client.getLambdaContext().getLogger().log("Invalid audience in Google ID token");
                return false;
            }
            
            // Check expiration
            if (now >= exp) {
                client.getLambdaContext().getLogger().log("Google ID token has expired");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Error verifying Google ID token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user info from Google using access token (alternative method)
     */
    public GoogleUserInfo getUserInfoFromAccessToken(String accessToken) {
        try {
            String userInfoEndpoint = "https://www.googleapis.com/oauth2/v2/userinfo";
            
            org.apache.http.client.methods.HttpGet get = new org.apache.http.client.methods.HttpGet(userInfoEndpoint);
            get.setHeader("Authorization", "Bearer " + accessToken);
            get.setHeader("Accept", "application/json");
            
            try (CloseableHttpResponse response = httpClient.execute(get)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Failed to get user info from Google: HTTP " + 
                        response.getStatusLine().getStatusCode());
                }
                
                JsonNode userInfo = client.getObjectMapper().readTree(responseBody);
                
                String email = userInfo.get("email").asText();
                String name = userInfo.get("name").asText();
                String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;
                Boolean emailVerified = userInfo.has("verified_email") ? 
                    userInfo.get("verified_email").asBoolean() : true;
                
                return new GoogleUserInfo(email, name, picture, emailVerified);
            }
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Failed to get user info from Google userinfo endpoint: " + e.getMessage());
            throw new RuntimeException("Failed to get user info from Google", e);
        }
    }

    /**
     * Generate state parameter for CSRF protection
     */
    private String generateState() {
        // Simple state generation - in production, use a more secure method
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Get Google client secret from secrets manager
     */
    private String getGoogleClientSecret() throws Exception {
        return client.cognito().getSecretValue("google_client_secret");
    }

    /**
     * Mask email for logging purposes
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            return email.substring(0, 2) + "***";
        }
        
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (localPart.length() <= 2) {
            return localPart + "***" + domain;
        }
        
        return localPart.substring(0, 2) + "***" + domain;
    }

    /**
     * Close HTTP client resources
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Failed to close HTTP client: " + e.getMessage());
        }
    }
}