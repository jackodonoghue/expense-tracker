package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simple in-memory token storage service
 * In production, this should use Redis or a database
 */
@Service
public class TokenStorageService {

    private static final Logger logger = LoggerFactory.getLogger(TokenStorageService.class);
    private static final long TOKEN_EXPIRY_MINUTES = 60; // 1 hour

    private final ConcurrentHashMap<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TokenStorageService() {
        // Clean up expired tokens every 10 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 10, 10, TimeUnit.MINUTES);
    }

    /**
     * Store access token for a session
     */
    public void storeToken(String sessionId, String accessToken) {
        long expiryTime = System.currentTimeMillis() + (TOKEN_EXPIRY_MINUTES * 60 * 1000);
        tokenStore.put(sessionId, new TokenEntry(accessToken, expiryTime));
        logger.info("Stored token for session: {}", sessionId);
    }

    /**
     * Retrieve access token for a session
     */
    public String getToken(String sessionId) {
        TokenEntry entry = tokenStore.get(sessionId);
        if (entry == null) {
            logger.warn("No token found for session: {}", sessionId);
            return null;
        }

        if (System.currentTimeMillis() > entry.expiryTime) {
            logger.warn("Token expired for session: {}", sessionId);
            tokenStore.remove(sessionId);
            return null;
        }

        return entry.accessToken;
    }

    /**
     * Remove token for a session (logout)
     */
    public void removeToken(String sessionId) {
        tokenStore.remove(sessionId);
        logger.info("Removed token for session: {}", sessionId);
    }

    /**
     * Check if session has a valid token
     */
    public boolean hasValidToken(String sessionId) {
        return getToken(sessionId) != null;
    }

    /**
     * Clean up expired tokens
     */
    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(entry -> {
            boolean expired = currentTime > entry.getValue().expiryTime;
            if (expired) {
                logger.debug("Cleaned up expired token for session: {}", entry.getKey());
            }
            return expired;
        });
    }

    /**
     * Inner class to hold token and expiry information
     */
    private static class TokenEntry {
        final String accessToken;
        final long expiryTime;

        TokenEntry(String accessToken, long expiryTime) {
            this.accessToken = accessToken;
            this.expiryTime = expiryTime;
        }
    }
}