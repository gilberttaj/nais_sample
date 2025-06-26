package com.nais.handlers;

/**
 * Result object for Google OAuth token exchange operations
 */
public class TokenExchangeResult {
    
    private final String idToken;
    private final String accessToken;
    private final boolean success;
    private final String error;
    
    private TokenExchangeResult(String idToken, String accessToken, boolean success, String error) {
        this.idToken = idToken;
        this.accessToken = accessToken;
        this.success = success;
        this.error = error;
    }
    
    /**
     * Create a successful result with ID token only
     */
    public static TokenExchangeResult success(String idToken) {
        return new TokenExchangeResult(idToken, null, true, null);
    }
    
    /**
     * Create a successful result with both ID and access tokens
     */
    public static TokenExchangeResult success(String idToken, String accessToken) {
        return new TokenExchangeResult(idToken, accessToken, true, null);
    }
    
    /**
     * Create a failure result
     */
    public static TokenExchangeResult failure(String error) {
        return new TokenExchangeResult(null, null, false, error);
    }
    
    /**
     * Check if the token exchange was successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get the ID token (JWT containing user info)
     */
    public String getIdToken() {
        return idToken;
    }
    
    /**
     * Get the access token (for API calls)
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Get the error message if exchange failed
     */
    public String getError() {
        return error;
    }
    
    /**
     * Check if we have a valid ID token
     */
    public boolean hasIdToken() {
        return idToken != null && !idToken.isEmpty();
    }
    
    /**
     * Check if we have a valid access token
     */
    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("TokenExchangeResult{success=%s, hasIdToken=%s, hasAccessToken=%s, error='%s'}", 
                           success, hasIdToken(), hasAccessToken(), error);
    }
}