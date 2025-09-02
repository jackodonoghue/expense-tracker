package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "truelayer")
public class TrueLayerConfig {

    private String clientId;
    private String clientSecret;
    private String environment = "sandbox";
    private String dataApiUrl = "https://api.truelayer.com/data/v1";
    private String authUrl = "https://auth.truelayer.com";
    private String redirectUri;

    // Getters and setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getDataApiUrl() { return dataApiUrl; }
    public void setDataApiUrl(String dataApiUrl) { this.dataApiUrl = dataApiUrl; }

    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}