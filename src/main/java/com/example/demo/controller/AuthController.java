package com.example.demo.controller;

import com.example.demo.config.TrueLayerConfig;
import com.example.demo.model.AuthResponse;
import com.example.demo.model.ErrorResponse;
import com.example.demo.service.TrueLayerService;
import com.example.demo.service.TokenStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:4200"})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private TrueLayerConfig trueLayerConfig;

    @Autowired
    private TrueLayerService trueLayerService;

    @Autowired
    private TokenStorageService tokenStorageService;

    /**
     * Initiates the TrueLayer authentication flow
     * Returns the authorization URL that the user should be redirected to
     */
    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam(required = false) String sessionId) {
        try {
            // Generate a state parameter for security
            String state = sessionId != null ? sessionId : UUID.randomUUID().toString();

            // Build TrueLayer authorization URL
            StringBuilder authUrlBuilder = new StringBuilder();
            authUrlBuilder.append(trueLayerConfig.getAuthUrl())
                         .append("?response_type=code")
                         .append("&client_id=").append(URLEncoder.encode(trueLayerConfig.getClientId(), StandardCharsets.UTF_8))
                         .append("&redirect_uri=").append(URLEncoder.encode(trueLayerConfig.getRedirectUri(), StandardCharsets.UTF_8))
                         .append("&scope=").append(URLEncoder.encode("info accounts balance transactions offline_access", StandardCharsets.UTF_8).replace("+", "%20"))
                         .append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8))
                         .append("&providers=").append(URLEncoder.encode("uk-cs-mock uk-ob-all uk-oauth-all", StandardCharsets.UTF_8.toString()).replace("+", "%20"));
            String authUrl = authUrlBuilder.toString();

            System.out.println("Generated TrueLayer auth URL: " + authUrl);

            logger.info("Generated TrueLayer auth URL for state: {}", state);

            return ResponseEntity.ok(new AuthResponse(authUrl, state, false, null));

        } catch (Exception e) {
            logger.error("Error generating auth URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(new ErrorResponse("Failed to generate authentication URL", e.getMessage()));
        }
    }

    /**
     * Handles the OAuth callback from TrueLayer
     * Exchanges authorization code for access token
     */
    @GetMapping("/callback")
    public RedirectView callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(required = false) String error) {

        logger.info("Received callback with code: {} and state: {}", code, state);

        if (error != null) {
            logger.error("Authentication error: {}", error);
            return new RedirectView("http://localhost:4200/login?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }

        try {
            // Exchange authorization code for access token
            String accessToken = trueLayerService.exchangeCodeForToken(code, state);

            // Store the token securely
            tokenStorageService.storeToken(state, accessToken);

            logger.info("Successfully authenticated user with state: {}", state);

            // Redirect to frontend dashboard with session ID
            return new RedirectView("http://localhost:4200/dashboard?sessionId=" + state);

        } catch (Exception e) {
            logger.error("Error during token exchange", e);
            return new RedirectView("http://localhost:4200/login?error=" + URLEncoder.encode("Authentication failed", StandardCharsets.UTF_8));
        }
    }

    /**
     * Check authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<AuthResponse> getAuthStatus(@RequestParam(required = false) String sessionId) {
        try {
            if (sessionId == null) {
                return ResponseEntity.ok(new AuthResponse(null, null, false, "No session ID provided"));
            }

            String token = tokenStorageService.getToken(sessionId);
            boolean isAuthenticated = token != null && trueLayerService.validateToken(token);

            String message = isAuthenticated ? "User is authenticated" : "User is not authenticated";

            return ResponseEntity.ok(new AuthResponse(null, sessionId, isAuthenticated, message));

        } catch (Exception e) {
            logger.error("Error checking auth status", e);
            return ResponseEntity.ok(new AuthResponse(null, sessionId, false, "Error checking authentication status"));
        }
    }

    /**
     * Logout user and invalidate tokens
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestParam String sessionId) {
        try {
            tokenStorageService.removeToken(sessionId);
            logger.info("User logged out successfully for session: {}", sessionId);

            return ResponseEntity.ok(new AuthResponse(null, sessionId, false, "Successfully logged out"));

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(new AuthResponse(null, sessionId, false, "Error during logout"));
        }
    }
}