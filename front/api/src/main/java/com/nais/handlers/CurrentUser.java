package com.nais.handlers;

/**
 * Represents the current authenticated user
 * Contains user information and authentication status
 */
public class CurrentUser {
    private final String username;
    private final boolean authenticated;
    private final String error;

    public CurrentUser(String username, boolean authenticated, String error) {
        this.username = username;
        this.authenticated = authenticated;
        this.error = error;
    }

    public String getUsername() { return username; }
    public boolean isAuthenticated() { return authenticated; }
    public String getError() { return error; }
}