package com.example.demo.model;

public class AuthResponse {
    private String authUrl;
    private String sessionId;
    private boolean authenticated;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String authUrl, String sessionId, boolean authenticated, String message) {
        this.authUrl = authUrl;
        this.sessionId = sessionId;
        this.authenticated = authenticated;
        this.message = message;
    }

    // Getters and setters
    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}