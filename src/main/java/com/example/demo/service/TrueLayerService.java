package com.example.demo.service;

import com.example.demo.model.Account;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TrueLayerService {

    private final WebClient webClient;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrueLayerService(WebClient webClient,
            OAuth2AuthorizedClientService authorizedClientService) {
        this.webClient = webClient;
        this.authorizedClientService = authorizedClientService;
    }

    public List<Account> getUserAccounts(OAuth2AuthenticationToken oauthToken) {
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        String principalName = oauthToken.getName();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(registrationId, principalName);

        if (authorizedClient == null) {
            throw new IllegalStateException("Authorized client not found for user: " + principalName);
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        // Use WebClient for non-blocking (reactive) requests
        String response = webClient.get()
                .uri("/accounts")
                .header("Authorization", "Bearer " + accessToken.getTokenValue())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            List<Account> accounts = new ArrayList<>();
            for (JsonNode accountNode : jsonResponse.get("results")) {
                Account account = parseAccount(accountNode);
                accounts.add(account);
            }

            return accounts;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse accounts response", e);
        }
    }

    private Account parseAccount(JsonNode accountNode) {
        Account account = new Account();

        account.setAccountId(accountNode.path("account_id").asText());
        account.setDisplayName(accountNode.path("display_name").asText());
        account.setAccountType(accountNode.path("account_type").asText());
        account.setCurrency(accountNode.path("currency").asText());
        account.setAccountNumber(accountNode.path("account_number").path("number").asText());
        account.setSortCode(accountNode.path("account_number").path("sort_code").asText());
        account.setProvider(accountNode.path("provider").path("display_name").asText());

        return account;
    }
}
