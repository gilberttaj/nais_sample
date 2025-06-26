package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * Authentication configuration loaded from environment variables
 * Matches the environment variables defined in SAM template
 */
public class AuthConfig {
    private final String userPoolId;
    private final String clientId;
    private final String secretName;
    private final String awsRegion;
    private final String googleClientId;
    private final String googleRedirectUri;
    private final boolean workspaceAuthEnabled;
    private final String stage;
    private final String allowedEmailDomains;
    private final String allowedEmails;
    private final String authLibraryVersion;

    public AuthConfig() {
        this(null);
    }

    public AuthConfig(Context lambdaContext) {
        // Core authentication settings (matches SAM template)
        this.userPoolId = System.getenv("USER_POOL_ID");
        this.clientId = System.getenv("CLIENT_ID");
        this.secretName = System.getenv("SECRET_NAME");
        
        // AWS_REGION is automatically available in Lambda
        this.awsRegion = System.getenv("AWS_REGION");
        
        this.googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        this.googleRedirectUri = System.getenv("GOOGLE_REDIRECT_URI");
        
        // Workspace authentication settings (matches SAM template)
        this.workspaceAuthEnabled = "true".equalsIgnoreCase(System.getenv("WORKSPACE_AUTH_ENABLED"));
        this.allowedEmailDomains = System.getenv("ALLOWED_EMAIL_DOMAINS");
        this.allowedEmails = System.getenv("ALLOWED_EMAILS");
        
        // Library configuration (matches SAM template)
        this.authLibraryVersion = System.getenv("AUTH_LIBRARY_VERSION");
        this.stage = System.getenv("STAGE");
        
        // Log for debugging (use Lambda logger if available)
        logConfiguration(lambdaContext);
    }

    private void logConfiguration(Context lambdaContext) {
        String debugInfo = buildDebugInfo();
        
        if (lambdaContext != null) {
            lambdaContext.getLogger().log(debugInfo);
        } else {
            System.out.println(debugInfo);
        }
    }

    private String buildDebugInfo() {
        StringBuilder debug = new StringBuilder();
        debug.append("=== AuthConfig Debug (SAM Template Aligned) ===\n");
        debug.append("USER_POOL_ID: ").append(maskSensitive(userPoolId)).append("\n");
        debug.append("CLIENT_ID: ").append(maskSensitive(clientId)).append("\n");
        debug.append("SECRET_NAME: ").append(secretName).append("\n");
        debug.append("AWS_REGION: ").append(awsRegion).append("\n");
        debug.append("GOOGLE_CLIENT_ID: ").append(maskSensitive(googleClientId)).append("\n");
        debug.append("GOOGLE_REDIRECT_URI: ").append(googleRedirectUri).append("\n");
        debug.append("WORKSPACE_AUTH_ENABLED: ").append(workspaceAuthEnabled).append("\n");
        debug.append("ALLOWED_EMAIL_DOMAINS: ").append(allowedEmailDomains).append("\n");
        debug.append("ALLOWED_EMAILS: ").append(maskSensitive(allowedEmails)).append("\n");
        debug.append("AUTH_LIBRARY_VERSION: ").append(authLibraryVersion).append("\n");
        debug.append("STAGE: ").append(stage).append("\n");
        debug.append("CONFIG_VALID: ").append(isValid()).append("\n");
        
        if (!isValid()) {
            debug.append("MISSING_VARS: ").append(getMissingVariables()).append("\n");
        }
        
        debug.append("================================================");
        return debug.toString();
    }

    // Core getters (required for authentication)
    public String getUserPoolId() { 
        return userPoolId != null ? userPoolId : "NOT_SET"; 
    }
    
    public String getClientId() { 
        return clientId != null ? clientId : "NOT_SET"; 
    }
    
    public String getSecretName() { 
        return secretName != null ? secretName : "NOT_SET"; 
    }
    
    public String getAwsRegion() { 
        return awsRegion != null ? awsRegion : "NOT_SET"; 
    }
    
    public String getGoogleClientId() { 
        return googleClientId != null ? googleClientId : "NOT_SET"; 
    }
    
    public String getGoogleRedirectUri() { 
        return googleRedirectUri != null ? googleRedirectUri : "NOT_SET"; 
    }

    // Workspace authentication getters (matches SAM template)
    public boolean isWorkspaceAuthEnabled() {
        return workspaceAuthEnabled;
    }

    public String getAllowedEmailDomains() {
        return allowedEmailDomains != null ? allowedEmailDomains : "";
    }

    public String getAllowedEmails() {
        return allowedEmails != null ? allowedEmails : "";
    }

    // Library configuration getters
    public String getAuthLibraryVersion() {
        return authLibraryVersion != null ? authLibraryVersion : "2.1.0";
    }

    public String getStage() {
        return stage != null ? stage : "dev";
    }

    // Validation methods
    public boolean isValid() {
        return userPoolId != null && 
               clientId != null && 
               secretName != null && 
               awsRegion != null && 
               googleClientId != null && 
               googleRedirectUri != null;
    }

    public String getMissingVariables() {
        StringBuilder missing = new StringBuilder();
        
        if (userPoolId == null) missing.append("USER_POOL_ID ");
        if (clientId == null) missing.append("CLIENT_ID ");
        if (secretName == null) missing.append("SECRET_NAME ");
        if (awsRegion == null) missing.append("AWS_REGION ");
        if (googleClientId == null) missing.append("GOOGLE_CLIENT_ID ");
        if (googleRedirectUri == null) missing.append("GOOGLE_REDIRECT_URI ");
        
        return missing.toString().trim();
    }

    /**
     * Validate configuration and throw exception if invalid
     */
    public void validateOrThrow() {
        if (!isValid()) {
            throw new IllegalStateException("Invalid AuthConfig - Missing environment variables: " + getMissingVariables());
        }
    }

    /**
     * Get configuration summary for logging
     */
    public String getConfigurationSummary() {
        return String.format("AuthConfig{region=%s, stage=%s, version=%s, workspaceAuth=%s, valid=%s}", 
                           awsRegion, stage, authLibraryVersion, workspaceAuthEnabled, isValid());
    }

    /**
     * Mask sensitive values for logging
     */
    private String maskSensitive(String value) {
        if (value == null) {
            return "NOT_SET";
        }
        if (value.length() <= 8) {
            return value.substring(0, Math.min(2, value.length())) + "***";
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }

    /**
     * Check if running in production environment
     */
    public boolean isProduction() {
        return "prod".equalsIgnoreCase(stage);
    }

    /**
     * Check if running in development environment
     */
    public boolean isDevelopment() {
        return "dev".equalsIgnoreCase(stage);
    }

    /**
     * Check if workspace authentication has domain restrictions configured
     */
    public boolean hasWorkspaceRestrictions() {
        return workspaceAuthEnabled && 
               (allowedEmailDomains != null && !allowedEmailDomains.trim().isEmpty() ||
                allowedEmails != null && !allowedEmails.trim().isEmpty());
    }

    /**
     * Get parsed allowed email domains as array
     */
    public String[] getAllowedEmailDomainsArray() {
        if (allowedEmailDomains == null || allowedEmailDomains.trim().isEmpty()) {
            return new String[0];
        }
        return allowedEmailDomains.split(",");
    }

    /**
     * Get parsed allowed emails as array
     */
    public String[] getAllowedEmailsArray() {
        if (allowedEmails == null || allowedEmails.trim().isEmpty()) {
            return new String[0];
        }
        return allowedEmails.split(",");
    }
}