package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;

/**
 * Amplify-like authentication client for AWS Lambda
 * Provides a simplified interface for authentication operations
 */
public class AuthClient {
    private final CognitoIdentityProviderClient cognitoClient;
    private final SecretsManagerClient secretsClient;
    private final ObjectMapper objectMapper;
    private final AuthConfig config;
    private final Context lambdaContext;

    public AuthClient(Context context) {
        this.lambdaContext = context;
        this.config = new AuthConfig();
        
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(config.getAwsRegion()))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
                
        this.secretsClient = SecretsManagerClient.builder()
                .region(Region.of(config.getAwsRegion()))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
                
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Configure authentication with Google OAuth
     */
    public GoogleAuth google() {
        return new GoogleAuth(this);
    }

    /**
     * Configure Cognito authentication
     */
    public CognitoAuth cognito() {
        return new CognitoAuth(this);
    }


    // Package-private getters for internal use
    CognitoIdentityProviderClient getCognitoClient() { 
        return cognitoClient; 
    }
    
    SecretsManagerClient getSecretsClient() { 
        return secretsClient; 
    }
    
    ObjectMapper getObjectMapper() { 
        return objectMapper; 
    }
    
    AuthConfig getConfig() { 
        return config; 
    }
    
    Context getLambdaContext() { 
        return lambdaContext; 
    }
}