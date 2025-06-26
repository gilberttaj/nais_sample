package com.nais.handlers;

/**
 * Result object for sign out operations
 * Matches the constructor pattern used in CognitoAuth
 */
public class SignOutResult {
    
    private final boolean success;
    private final String error;
    private final String message;
    
    /**
     * Constructor matching the pattern used in CognitoAuth
     * @param success Whether the sign out was successful
     * @param error The error message if sign out failed (or success message if successful)
     */
    public SignOutResult(boolean success, String error) {
        this.success = success;
        if (success) {
            this.message = error; // In success case, "error" parameter contains the success message
            this.error = null;
        } else {
            this.error = error;
            this.message = null;
        }
    }
    
    /**
     * Create a successful sign out result
     */
    public static SignOutResult success(String message) {
        return new SignOutResult(true, message);
    }
    
    /**
     * Create a successful sign out result with default message
     */
    public static SignOutResult success() {
        return new SignOutResult(true, "Successfully signed out");
    }
    
    /**
     * Create a failed sign out result
     */
    public static SignOutResult failure(String error) {
        return new SignOutResult(false, error);
    }
    
    /**
     * Check if sign out was successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get the success message
     */
    public String getMessage() {
        return success ? message : null;
    }
    
    /**
     * Get the error message if sign out failed
     */
    public String getError() {
        return success ? null : error;
    }
    
    @Override
    public String toString() {
        return String.format("SignOutResult{success=%s, message='%s', error='%s'}", 
                           success, getMessage(), getError());
    }
}