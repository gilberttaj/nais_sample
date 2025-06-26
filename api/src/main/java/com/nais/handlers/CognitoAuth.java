package com.nais.handlers;

import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import com.fasterxml.jackson.databind.JsonNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * AWS Cognito authentication provider
 * Handles Cognito user authentication and management
 */
public class CognitoAuth {
    private final AuthClient client;

    public CognitoAuth(AuthClient client) {
        this.client = client;
    }

    /**
     * Sign in with username and password
     */
    public AuthResult signIn(String username, String password) {
        try {
            String clientSecret = getSecretValue("client_secret");
            
            InitiateAuthResponse response = client.getCognitoClient().initiateAuth(
                InitiateAuthRequest.builder()
                    .clientId(client.getConfig().getClientId())
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .authParameters(Map.of(
                        "USERNAME", username,
                        "PASSWORD", password,
                        "SECRET_HASH", calculateSecretHash(username, clientSecret)
                    ))
                    .build()
            );
            
            return new AuthResult(response.authenticationResult(), true, null);
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Sign in failed: " + e.getMessage());
            return new AuthResult(null, false, e.getMessage());
        }
    }

    /**
     * Sign in with Google ID token (main method for OAuth flow)
     */
    public AuthResult signInWithGoogle(String googleIdToken) {
        try {
            client.getLambdaContext().getLogger().log("Starting Google authentication with Cognito");
            
            String clientSecret = getSecretValue("client_secret");
            GoogleUserInfo userInfo = client.google().getUserInfo(googleIdToken);
            
            client.getLambdaContext().getLogger().log("Processing Cognito auth for: " + maskEmail(userInfo.getEmail()));
            
            try {
                // Try to authenticate existing user
                InitiateAuthResponse authResponse = client.getCognitoClient().initiateAuth(
                    InitiateAuthRequest.builder()
                        .clientId(client.getConfig().getClientId())
                        .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                        .authParameters(Map.of(
                            "USERNAME", userInfo.getEmail(),
                            "PASSWORD", googleIdToken,
                            "SECRET_HASH", calculateSecretHash(userInfo.getEmail(), clientSecret)
                        ))
                        .build()
                );
                
                client.getLambdaContext().getLogger().log("Existing user authenticated successfully");
                return new AuthResult(authResponse.authenticationResult(), true, null);
                
            } catch (UserNotFoundException | NotAuthorizedException e) {
                // User doesn't exist, create new user
                client.getLambdaContext().getLogger().log("User not found, creating new Cognito user");
                return createUserAndAuthenticate(userInfo, googleIdToken, clientSecret);
            }
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Google authentication failed: " + e.getMessage());
            return new AuthResult(null, false, "Google authentication failed: " + e.getMessage());
        }
    }

    /**
     * Refresh authentication tokens
     */
    public AuthResult refreshToken(String refreshToken, String username) {
        try {
            client.getLambdaContext().getLogger().log("Refreshing tokens for user: " + maskEmail(username));
            
            String clientSecret = getSecretValue("client_secret");
            
            InitiateAuthResponse response = client.getCognitoClient().initiateAuth(
                InitiateAuthRequest.builder()
                    .clientId(client.getConfig().getClientId())
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .authParameters(Map.of(
                        "REFRESH_TOKEN", refreshToken,
                        "SECRET_HASH", calculateSecretHash(username, clientSecret)
                    ))
                    .build()
            );
            
            client.getLambdaContext().getLogger().log("Token refresh successful");
            return new AuthResult(response.authenticationResult(), true, null);
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Token refresh failed: " + e.getMessage());
            return new AuthResult(null, false, "Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Sign out user globally
     */
    public SignOutResult signOut(String accessToken) {
        try {
            client.getLambdaContext().getLogger().log("Performing global sign out");
            
            client.getCognitoClient().globalSignOut(
                GlobalSignOutRequest.builder()
                    .accessToken(accessToken)
                    .build()
            );
            
            client.getLambdaContext().getLogger().log("Global sign out successful");
            return new SignOutResult(true, "Successfully signed out from all devices");
            
        } catch (Exception e) {
            client.getLambdaContext().getLogger().log("Sign out failed: " + e.getMessage());
            return new SignOutResult(false, "Sign out failed: " + e.getMessage());
        }
    }


    /**
     * Get secret value from AWS Secrets Manager
     */
    public String getSecretValue(String secretKey) throws Exception {
        GetSecretValueResponse response = client.getSecretsClient().getSecretValue(
            GetSecretValueRequest.builder()
                .secretId(client.getConfig().getSecretName())
                .build()
        );
        
        JsonNode secret = client.getObjectMapper().readTree(response.secretString());
        if (!secret.has(secretKey)) {
            throw new RuntimeException("Secret key '" + secretKey + "' not found in secrets manager");
        }
        return secret.get(secretKey).asText();
    }

    private String calculateSecretHash(String username, String clientSecret) throws Exception {
        String message = username + client.getConfig().getClientId();
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private AuthResult createUserAndAuthenticate(GoogleUserInfo userInfo, String googleIdToken, String clientSecret) throws Exception {
        try {
            // Create user in Cognito with Google profile information
            AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                .userPoolId(client.getConfig().getUserPoolId())
                .username(userInfo.getEmail())
                .userAttributes(
                    AttributeType.builder().name("email").value(userInfo.getEmail()).build(),
                    AttributeType.builder().name("name").value(userInfo.getName() != null ? userInfo.getName() : userInfo.getEmail()).build(),
                    AttributeType.builder().name("email_verified").value("true").build(),
                    AttributeType.builder().name("custom:auth_provider").value("google").build()
                )
                .temporaryPassword(generateTemporaryPassword())
                .messageAction(MessageActionType.SUPPRESS)
                .build();
                
            client.getCognitoClient().adminCreateUser(createUserRequest);
            client.getLambdaContext().getLogger().log("Created new Cognito user");
            
            // Set permanent password using Google ID token
            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(client.getConfig().getUserPoolId())
                .username(userInfo.getEmail())
                .password(googleIdToken)
                .permanent(true)
                .build();
                
            client.getCognitoClient().adminSetUserPassword(setPasswordRequest);
            client.getLambdaContext().getLogger().log("Set permanent password for new user");
            
            // Authenticate the newly created user
            InitiateAuthResponse authResponse = client.getCognitoClient().initiateAuth(
                InitiateAuthRequest.builder()
                    .clientId(client.getConfig().getClientId())
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .authParameters(Map.of(
                        "USERNAME", userInfo.getEmail(),
                        "PASSWORD", googleIdToken,
                        "SECRET_HASH", calculateSecretHash(userInfo.getEmail(), clientSecret)
                    ))
                    .build()
            );
            
            client.getLambdaContext().getLogger().log("New user authenticated successfully");
            return new AuthResult(authResponse.authenticationResult(), true, null);
            
        } catch (UsernameExistsException e) {
            // User was created between our check and now, try to authenticate again
            client.getLambdaContext().getLogger().log("User exists, attempting authentication");
            
            InitiateAuthResponse authResponse = client.getCognitoClient().initiateAuth(
                InitiateAuthRequest.builder()
                    .clientId(client.getConfig().getClientId())
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .authParameters(Map.of(
                        "USERNAME", userInfo.getEmail(),
                        "PASSWORD", googleIdToken,
                        "SECRET_HASH", calculateSecretHash(userInfo.getEmail(), clientSecret)
                    ))
                    .build()
            );
            
            return new AuthResult(authResponse.authenticationResult(), true, null);
        }
    }

    private String generateTemporaryPassword() {
        // Generate a more secure temporary password
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "GoogleAuth2024!" + timestamp.substring(timestamp.length() - 6);
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
}