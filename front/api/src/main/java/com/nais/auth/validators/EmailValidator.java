package com.nais.auth.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Email domain validator for Google Workspace authentication
 * Validates that users can only authenticate if they belong to approved email domains
 */
public class EmailValidator {
    
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    private final Pattern emailPattern;
    private final Set<String> allowedDomains;
    private final Set<String> allowedEmails;
    private final ObjectMapper objectMapper;
    private final SecretsManagerClient secretsManagerClient;
    private final String secretName;
    
    public EmailValidator(SecretsManagerClient secretsManagerClient, String secretName) {
        this.emailPattern = Pattern.compile(EMAIL_PATTERN);
        this.allowedDomains = new HashSet<>();
        this.allowedEmails = new HashSet<>();
        this.objectMapper = new ObjectMapper();
        this.secretsManagerClient = secretsManagerClient;
        this.secretName = secretName;
        
        // Load configuration from AWS Secrets Manager
        loadEmailConfiguration();
    }
    
    /**
     * Load email configuration from AWS Secrets Manager
     */
    private void loadEmailConfiguration() {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
                
            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();
            
            JsonNode secretJson = objectMapper.readTree(secretString);
            
            // Load allowed domains
            if (secretJson.has("allowed_domains")) {
                JsonNode domainsNode = secretJson.get("allowed_domains");
                if (domainsNode.isArray()) {
                    // Handle JSON array format
                    for (JsonNode domainNode : domainsNode) {
                        String domain = domainNode.asText().trim().toLowerCase();
                        if (!domain.isEmpty()) {
                            allowedDomains.add(domain);
                        }
                    }
                } else if (domainsNode.isTextual()) {
                    // Handle comma-separated string format
                    String domainsString = domainsNode.asText();
                    if (!domainsString.trim().isEmpty()) {
                        String[] domains = domainsString.split(",");
                        for (String domain : domains) {
                            String cleanDomain = domain.trim().toLowerCase();
                            if (!cleanDomain.isEmpty()) {
                                allowedDomains.add(cleanDomain);
                            }
                        }
                    }
                }
            }
            
            // Load allowed individual emails
            if (secretJson.has("allowed_emails")) {
                JsonNode emailsNode = secretJson.get("allowed_emails");
                if (emailsNode.isArray()) {
                    // Handle JSON array format
                    for (JsonNode emailNode : emailsNode) {
                        String email = emailNode.asText().trim().toLowerCase();
                        if (!email.isEmpty()) {
                            allowedEmails.add(email);
                        }
                    }
                } else if (emailsNode.isTextual()) {
                    // Handle comma-separated string format
                    String emailsString = emailsNode.asText();
                    if (!emailsString.trim().isEmpty()) {
                        String[] emails = emailsString.split(",");
                        for (String email : emails) {
                            String cleanEmail = email.trim().toLowerCase();
                            if (!cleanEmail.isEmpty()) {
                                allowedEmails.add(cleanEmail);
                            }
                        }
                    }
                }
            }
            
            // Fallback to environment variables if secrets don't contain the config
            if (allowedDomains.isEmpty() && allowedEmails.isEmpty()) {
                loadFromEnvironmentVariables();
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load email configuration from secrets: " + e.getMessage());
            // Fallback to environment variables
            loadFromEnvironmentVariables();
        }
    }
    
    /**
     * Fallback method to load configuration from environment variables
     */
    private void loadFromEnvironmentVariables() {
        String domainsEnv = System.getenv("ALLOWED_EMAIL_DOMAINS");
        String emailsEnv = System.getenv("ALLOWED_EMAILS");
        
        if (domainsEnv != null && !domainsEnv.trim().isEmpty()) {
            String[] domains = domainsEnv.split(",");
            for (String domain : domains) {
                String cleanDomain = domain.trim().toLowerCase();
                if (!cleanDomain.isEmpty()) {
                    allowedDomains.add(cleanDomain);
                }
            }
        }
        
        if (emailsEnv != null && !emailsEnv.trim().isEmpty()) {
            String[] emails = emailsEnv.split(",");
            for (String email : emails) {
                String cleanEmail = email.trim().toLowerCase();
                if (!cleanEmail.isEmpty()) {
                    allowedEmails.add(cleanEmail);
                }
            }
        }
        
        // Default fallback domains if nothing is configured
        if (allowedDomains.isEmpty() && allowedEmails.isEmpty()) {
            System.out.println("No email restrictions configured. Adding default fallback domains.");
            allowedDomains.addAll(Arrays.asList(
                "gleamorb.com",
                "company.com" // Replace with your actual domain
            ));
        }
    }
    
    /**
     * Validates if the given email is allowed to authenticate
     * 
     * @param email The email address to validate
     * @return EmailValidationResult containing validation status and details
     */
    public EmailValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return EmailValidationResult.invalid("Email cannot be null or empty");
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        
        // Basic email format validation
        if (!emailPattern.matcher(normalizedEmail).matches()) {
            return EmailValidationResult.invalid("Invalid email format");
        }
        
        // Extract domain from email
        String domain = extractDomain(normalizedEmail);
        if (domain == null) {
            return EmailValidationResult.invalid("Could not extract domain from email");
        }
        
        boolean emailInAllowedList = allowedEmails.contains(normalizedEmail);
        boolean domainInAllowedList = allowedDomains.contains(domain);
        
        // If both allowed emails and allowed domains are configured, BOTH must pass
        if (!allowedEmails.isEmpty() && !allowedDomains.isEmpty()) {
            if (emailInAllowedList && domainInAllowedList) {
                return EmailValidationResult.valid("Email and domain both allowed");
            } else if (!emailInAllowedList && !domainInAllowedList) {
                return EmailValidationResult.invalid(
                    String.format("Email '%s' is not in allowed emails list and domain '%s' is not in allowed domains list", 
                                normalizedEmail, domain)
                );
            } else if (!emailInAllowedList) {
                return EmailValidationResult.invalid(
                    String.format("Email '%s' is not in the allowed emails list", normalizedEmail)
                );
            } else {
                return EmailValidationResult.invalid(
                    String.format("Email domain '%s' is not in the allowed domains list", domain)
                );
            }
        }
        
        // If only allowed emails are configured, check email allowlist
        if (!allowedEmails.isEmpty() && allowedDomains.isEmpty()) {
            if (emailInAllowedList) {
                return EmailValidationResult.valid("Email explicitly allowed");
            } else {
                return EmailValidationResult.invalid(
                    String.format("Email '%s' is not in the allowed emails list", normalizedEmail)
                );
            }
        }
        
        // If only allowed domains are configured, check domain allowlist
        if (allowedEmails.isEmpty() && !allowedDomains.isEmpty()) {
            if (domainInAllowedList) {
                return EmailValidationResult.valid("Email domain is allowed");
            } else {
                return EmailValidationResult.invalid(
                    String.format("Email domain '%s' is not in the allowed domains list", domain)
                );
            }
        }
        
        // If neither list is configured, allow all emails (fallback behavior)
        return EmailValidationResult.valid("No email restrictions configured");
    }
    
    /**
     * Extract domain from email address
     */
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return null;
        }
        return email.substring(atIndex + 1);
    }
    
    /**
     * Get the list of allowed domains for logging/debugging
     */
    public Set<String> getAllowedDomains() {
        return new HashSet<>(allowedDomains);
    }
    
    /**
     * Get the list of allowed emails for logging/debugging
     */
    public Set<String> getAllowedEmails() {
        return new HashSet<>(allowedEmails);
    }
    
    /**
     * Check if any email restrictions are configured
     */
    public boolean hasRestrictions() {
        return !allowedDomains.isEmpty() || !allowedEmails.isEmpty();
    }
    
    /**
     * Get configuration summary for logging
     */
    public String getConfigurationSummary() {
        return String.format(
            "EmailValidator configured with %d allowed domains and %d allowed emails",
            allowedDomains.size(),
            allowedEmails.size()
        );
    }
    
    /**
     * Get the raw secret value from AWS Secrets Manager
     * This allows access to other secrets stored in the same secret
     */
    public String getSecretValue(String secretId) throws Exception {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId(secretId)
            .build();
            
        GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
        return response.secretString();
    }
}