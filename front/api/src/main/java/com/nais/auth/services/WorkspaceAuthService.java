package com.nais.auth.services;

import com.amazonaws.services.lambda.runtime.Context;
import com.nais.auth.validators.EmailValidator;
import com.nais.auth.validators.EmailValidationResult;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Service for handling Google Workspace authentication with email domain restrictions
 */
public class WorkspaceAuthService {
    
    private final EmailValidator emailValidator;
    private final Context lambdaContext;
    
    public WorkspaceAuthService(SecretsManagerClient secretsManagerClient, String secretName, Context lambdaContext) {
        this.emailValidator = new EmailValidator(secretsManagerClient, secretName);
        this.lambdaContext = lambdaContext;
        
        // Log configuration on initialization
        lambdaContext.getLogger().log("WorkspaceAuthService initialized: " + emailValidator.getConfigurationSummary());
    }
    
    /**
     * Validates if a user email is allowed to authenticate
     * 
     * @param email The user's email address from Google OAuth
     * @return EmailValidationResult indicating if authentication should proceed
     */
    public EmailValidationResult validateUserAccess(String email) {
        lambdaContext.getLogger().log("Validating user access for email: " + maskEmail(email));
        
        EmailValidationResult result = emailValidator.validateEmail(email);
        
        if (result.isValid()) {
            lambdaContext.getLogger().log("Email validation passed: " + result.getMessage());
        } else {
            lambdaContext.getLogger().log("Email validation failed: " + result.getMessage());
        }
        
        return result;
    }
    
    /**
     * Check if the authentication service has any email restrictions configured
     */
    public boolean hasEmailRestrictions() {
        return emailValidator.hasRestrictions();
    }
    
    /**
     * Get configuration information for debugging/logging
     */
    public String getConfigurationInfo() {
        return emailValidator.getConfigurationSummary();
    }
    
    /**
     * Get the raw secret value from AWS Secrets Manager
     * This delegates to the EmailValidator's secret management
     */
    public String getSecretValue(String secretName) throws Exception {
        return emailValidator.getSecretValue(secretName);
    }
    
    /**
     * Mask email for logging purposes (show only first 2 chars and domain)
     * Example: john.doe@company.com -> jo***@company.com
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
}