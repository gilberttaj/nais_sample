package com.nais.handlers;

/**
 * Result object for OAuth URL generation
 * Contains the generated auth URL, success status, and error information
 */
public class AuthUrlResult {
    private final String authUrl;
    private final boolean success;
    private final String error;

    public AuthUrlResult(String authUrl, boolean success, String error) {
        this.authUrl = authUrl;
        this.success = success;
        this.error = error;
    }

    public String getAuthUrl() { return authUrl; }
    public boolean isSuccess() { return success; }
    public String getError() { return error; }
}