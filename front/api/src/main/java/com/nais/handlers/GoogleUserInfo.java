package com.nais.handlers;

/**
 * Google user information extracted from OAuth ID token
 */
public class GoogleUserInfo {
    
    private final String email;
    private final String name;
    private final String picture;
    private final Boolean emailVerified;
    
    public GoogleUserInfo(String email, String name, String picture) {
        this(email, name, picture, null);
    }
    
    public GoogleUserInfo(String email, String name, String picture, Boolean emailVerified) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.emailVerified = emailVerified;
    }
    
    /**
     * Get the user's email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Get the user's full name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the user's profile picture URL
     */
    public String getPicture() {
        return picture;
    }
    
    /**
     * Check if the email is verified by Google
     */
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    /**
     * Check if email verification status is available
     */
    public boolean hasEmailVerificationInfo() {
        return emailVerified != null;
    }
    
    /**
     * Check if email is verified (defaults to true if verification info is not available)
     */
    public boolean isEmailVerified() {
        return emailVerified == null || emailVerified;
    }
    
    /**
     * Extract domain from email address
     */
    public String getEmailDomain() {
        if (email == null) {
            return null;
        }
        
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return null;
        }
        
        return email.substring(atIndex + 1).toLowerCase();
    }
    
    /**
     * Check if user has a profile picture
     */
    public boolean hasPicture() {
        return picture != null && !picture.isEmpty();
    }
    
    /**
     * Get masked email for logging purposes
     */
    public String getMaskedEmail() {
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
    
    /**
     * Validate that essential user info is present
     */
    public boolean isValid() {
        return email != null && !email.isEmpty() && email.contains("@");
    }
    
    @Override
    public String toString() {
        return String.format("GoogleUserInfo{email='%s', name='%s', emailVerified=%s, hasPicture=%s}", 
                           getMaskedEmail(), name, emailVerified, hasPicture());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GoogleUserInfo that = (GoogleUserInfo) obj;
        return email != null ? email.equals(that.email) : that.email == null;
    }
    
    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}