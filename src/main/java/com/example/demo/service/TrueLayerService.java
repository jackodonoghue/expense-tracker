package com.example.demo.service;

import com.example.demo.config.TrueLayerConfig;
import com.example.demo.exception.TrueLayerException;
import com.example.demo.model.Account;
import com.example.demo.model.Balance;
import com.example.demo.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrueLayerService {

    private static final Logger logger = LoggerFactory.getLogger(TrueLayerService.class);

    @Autowired
    private TrueLayerConfig trueLayerConfig;

    @Autowired
    private WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Exchange authorization code for access token
     */
    public String exchangeCodeForToken(String code, String state) throws TrueLayerException {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", trueLayerConfig.getClientId());
            formData.add("client_secret", trueLayerConfig.getClientSecret());
            formData.add("redirect_uri", trueLayerConfig.getRedirectUri());
            formData.add("code", code);

            String response = webClient.post()
                .uri(trueLayerConfig.getAuthUrl() + "/connect/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            String accessToken = jsonResponse.get("access_token").asText();

            logger.info("Successfully exchanged code for access token");
            return accessToken;

        } catch (WebClientResponseException e) {
            logger.error("Error exchanging code for token: {}", e.getResponseBodyAsString());
            throw new TrueLayerException("Failed to exchange authorization code", e);
        } catch (Exception e) {
            logger.error("Unexpected error during token exchange", e);
            throw new TrueLayerException("Token exchange failed", e);
        }
    }

    /**
     * Validate access token
     */
    public boolean validateToken(String accessToken) {
        try {
            webClient.get()
                .uri(trueLayerConfig.getDataApiUrl() + "/info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            return true;
        } catch (Exception e) {
            logger.warn("Token validation failed", e);
            return false;
        }
    }

    /**
     * Get user accounts
     */
    public List<Account> getUserAccounts(String accessToken) throws TrueLayerException {
        try {
            String response = webClient.get()
                .uri(trueLayerConfig.getDataApiUrl() + "/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode accountsArray = jsonResponse.get("results");

            List<Account> accounts = new ArrayList<>();
            if (accountsArray != null && accountsArray.isArray()) {
                for (JsonNode accountNode : accountsArray) {
                    Account account = parseAccount(accountNode);
                    accounts.add(account);
                }
            }

            return accounts;

        } catch (WebClientResponseException e) {
            logger.error("Error retrieving accounts: {}", e.getResponseBodyAsString());
            throw new TrueLayerException("Failed to retrieve accounts", e);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving accounts", e);
            throw new TrueLayerException("Account retrieval failed", e);
        }
    }

    /**
     * Get transactions for a specific account
     */
    public List<Transaction> getAccountTransactions(String accessToken, String accountId, int limit) throws TrueLayerException {
        try {
            String uri = String.format("%s/accounts/%s/transactions?limit=%d", 
                                     trueLayerConfig.getDataApiUrl(), accountId, limit);

            String response = webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode transactionsArray = jsonResponse.get("results");

            List<Transaction> transactions = new ArrayList<>();
            if (transactionsArray != null && transactionsArray.isArray()) {
                for (JsonNode transactionNode : transactionsArray) {
                    Transaction transaction = parseTransaction(transactionNode);
                    transactions.add(transaction);
                }
            }

            return transactions;

        } catch (WebClientResponseException e) {
            logger.error("Error retrieving transactions: {}", e.getResponseBodyAsString());
            throw new TrueLayerException("Failed to retrieve transactions", e);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving transactions", e);
            throw new TrueLayerException("Transaction retrieval failed", e);
        }
    }

    /**
     * Get all transactions across all accounts
     */
    public List<Transaction> getAllTransactions(String accessToken, int limit) throws TrueLayerException {
        List<Account> accounts = getUserAccounts(accessToken);
        List<Transaction> allTransactions = new ArrayList<>();

        for (Account account : accounts) {
            try {
                List<Transaction> accountTransactions = getAccountTransactions(accessToken, account.getAccountId(), limit);
                allTransactions.addAll(accountTransactions);
            } catch (Exception e) {
                logger.warn("Failed to retrieve transactions for account: {}", account.getAccountId(), e);
            }
        }

        return allTransactions;
    }

    /**
     * Get account balances
     */
    public List<Balance> getAccountBalances(String accessToken) throws TrueLayerException {
        List<Account> accounts = getUserAccounts(accessToken);
        List<Balance> balances = new ArrayList<>();

        for (Account account : accounts) {
            try {
                String uri = String.format("%s/accounts/%s/balance", 
                                         trueLayerConfig.getDataApiUrl(), account.getAccountId());

                String response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

                JsonNode jsonResponse = objectMapper.readTree(response);
                JsonNode balanceArray = jsonResponse.get("results");

                if (balanceArray != null && balanceArray.isArray() && balanceArray.size() > 0) {
                    Balance balance = parseBalance(balanceArray.get(0), account);
                    balances.add(balance);
                }

            } catch (Exception e) {
                logger.warn("Failed to retrieve balance for account: {}", account.getAccountId(), e);
            }
        }

        return balances;
    }

    /**
     * Get user information
     */
    public Map<String, Object> getUserInfo(String accessToken) throws TrueLayerException {
        try {
            String response = webClient.get()
                .uri(trueLayerConfig.getDataApiUrl() + "/info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode resultsArray = jsonResponse.get("results");

            Map<String, Object> userInfo = new HashMap<>();
            if (resultsArray != null && resultsArray.isArray() && resultsArray.size() > 0) {
                JsonNode userNode = resultsArray.get(0);
                userInfo.put("full_name", userNode.path("full_name").asText());
                userInfo.put("email", userNode.path("emails").isArray() && userNode.path("emails").size() > 0 
                    ? userNode.path("emails").get(0).asText() : "");
                userInfo.put("phone", userNode.path("phones").isArray() && userNode.path("phones").size() > 0 
                    ? userNode.path("phones").get(0).asText() : "");
                userInfo.put("date_of_birth", userNode.path("date_of_birth").asText());
            }

            return userInfo;

        } catch (WebClientResponseException e) {
            logger.error("Error retrieving user info: {}", e.getResponseBodyAsString());
            throw new TrueLayerException("Failed to retrieve user info", e);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving user info", e);
            throw new TrueLayerException("User info retrieval failed", e);
        }
    }

    // Helper methods to parse JSON responses
    private Account parseAccount(JsonNode accountNode) {
        Account account = new Account();
        account.setAccountId(accountNode.path("account_id").asText());
        account.setAccountType(accountNode.path("account_type").asText());
        account.setDisplayName(accountNode.path("display_name").asText());
        account.setCurrency(accountNode.path("currency").asText());
        account.setAccountNumber(accountNode.path("account_number").path("number").asText());
        account.setSortCode(accountNode.path("account_number").path("sort_code").asText());
        account.setProvider(accountNode.path("provider").path("display_name").asText());
        return account;
    }

    private Transaction parseTransaction(JsonNode transactionNode) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionNode.path("transaction_id").asText());
        transaction.setAccountId(transactionNode.path("account_id").asText());
        transaction.setAmount(transactionNode.path("amount").asDouble());
        transaction.setCurrency(transactionNode.path("currency").asText());
        transaction.setDescription(transactionNode.path("description").asText());
        transaction.setTransactionType(transactionNode.path("transaction_type").asText());
        transaction.setTransactionCategory(transactionNode.path("transaction_category").asText());

        // Parse timestamp
        String timestampStr = transactionNode.path("timestamp").asText();
        if (!timestampStr.isEmpty()) {
            transaction.setTimestamp(LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME));
        }

        return transaction;
    }

    private Balance parseBalance(JsonNode balanceNode, Account account) {
        Balance balance = new Balance();
        balance.setAccountId(account.getAccountId());
        balance.setAccountName(account.getDisplayName());
        balance.setCurrency(balanceNode.path("currency").asText());
        balance.setCurrent(balanceNode.path("current").asDouble());
        balance.setAvailable(balanceNode.path("available").asDouble());
        balance.setOverdraft(balanceNode.path("overdraft").asDouble());

        String lastUpdateStr = balanceNode.path("last_update").asText();
        if (!lastUpdateStr.isEmpty()) {
            balance.setLastUpdate(LocalDateTime.parse(lastUpdateStr, DateTimeFormatter.ISO_DATE_TIME));
        }

        return balance;
    }
}