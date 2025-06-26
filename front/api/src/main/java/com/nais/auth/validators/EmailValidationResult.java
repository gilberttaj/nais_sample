package com.nais.auth.validators;

/**
 * Result object for email validation operations
 */
public class EmailValidationResult {
    
    private final boolean isValid;
    private final String message;
    private final String email;
    
    private EmailValidationResult(boolean isValid, String message, String email) {
        this.isValid = isValid;
        this.message = message;
        this.email = email;
    }
    
    /**
     * Create a valid result
     */
    public static EmailValidationResult valid(String message) {
        return new EmailValidationResult(true, message, null);
    }
    
    /**
     * Create a valid result with email
     */
    public static EmailValidationResult valid(String message, String email) {
        return new EmailValidationResult(true, message, email);
    }
    
    /**
     * Create an invalid result
     */
    public static EmailValidationResult invalid(String message) {
        return new EmailValidationResult(false, message, null);
    }
    
    /**
     * Create an invalid result with email
     */
    public static EmailValidationResult invalid(String message, String email) {
        return new EmailValidationResult(false, message, email);
    }
    
    /**
     * Check if validation passed
     */
    public boolean isValid() {
        return isValid;
    }
    
    /**
     * Get validation message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the email that was validated (if provided)
     */
    public String getEmail() {
        return email;
    }
    
    @Override
    public String toString() {
        return String.format("EmailValidationResult{isValid=%s, message='%s', email='%s'}", 
                           isValid, message, email);
    }
}