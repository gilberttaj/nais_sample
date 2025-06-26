package com.nais.handlers;

/**
 * Result object for Cognito authentication operations
 * Matches the constructor pattern used in CognitoAuth
 */
public class AuthResult {
    
    private final Object authenticationResult;
    private final boolean success;
    private final String error;
    
    /**
     * Constructor matching the pattern used in CognitoAuth
     * @param authenticationResult The Cognito authentication result object
     * @param success Whether the authentication was successful
     * @param error The error message if authentication failed
     */
    public AuthResult(Object authenticationResult, boolean success, String error) {
        this.authenticationResult = authenticationResult;
        this.success = success;
        this.error = error;
    }
    
    /**
     * Create a successful authentication result
     */
    public static AuthResult success(Object authenticationResult) {
        return new AuthResult(authenticationResult, true, null);
    }
    
    /**
     * Create a failed authentication result
     */
    public static AuthResult failure(String error) {
        return new AuthResult(null, false, error);
    }
    
    /**
     * Check if authentication was successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get the authentication result object (contains tokens)
     */
    public Object getAuthenticationResult() {
        return authenticationResult;
    }
    
    /**
     * Get the error message if authentication failed
     */
    public String getError() {
        return error;
    }
    
    /**
     * Check if we have a valid authentication result
     */
    public boolean hasAuthenticationResult() {
        return authenticationResult != null;
    }
    
    @Override
    public String toString() {
        return String.format("AuthResult{success=%s, hasResult=%s, error='%s'}", 
                           success, hasAuthenticationResult(), error);
    }
}