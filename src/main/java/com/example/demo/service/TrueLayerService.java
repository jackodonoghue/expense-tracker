package com.example.demo.service;

import reactor.core.publisher.Flux;
import com.example.demo.model.truelayer.Transaction;
import com.example.demo.model.truelayer.TransactionsResponse;
import com.example.demo.model.Account;
import com.example.demo.model.AccountDto;
import com.example.demo.model.AccountsResponse;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrueLayerService {

    private final WebClient webClient;
    private final AuthService authService;

    public TrueLayerService(WebClient webClient, AuthService authService) {
        this.webClient = webClient;
        this.authService = authService;
    }

    public Mono<List<Account>> getUserAccounts(OAuth2AuthenticationToken oauthToken) {
        return authService.getAuthorisedUser(oauthToken)
                .flatMap(authorizedClient -> {
                    String accessToken = authorizedClient.getAccessToken().getTokenValue();
                    return webClient.get()
                            .uri("/accounts")
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(AccountsResponse.class)
                            .map(response -> response.getResults().stream()
                                    .map(this::mapToAccount)
                                    .collect(Collectors.toList()));
                });
    }

    public Mono<List<Transaction>> getAllTransactions(OAuth2AuthenticationToken oauthToken) {
        return getUserAccounts(oauthToken)
                .flatMapMany(Flux::fromIterable)
                .flatMap(account -> getTransactions(oauthToken, account.getAccountId()))
                .flatMap(Flux::fromIterable)
                .collectList();
    }

    public Mono<List<com.example.demo.model.truelayer.Transaction>> getTransactions(OAuth2AuthenticationToken oauthToken, String accountId) {
        return authService.getAuthorisedUser(oauthToken)
                .flatMap(authorizedClient -> {
                    String accessToken = authorizedClient.getAccessToken().getTokenValue();
                    return webClient.get()
                            .uri("/accounts/" + accountId + "/transactions")
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(com.example.demo.model.truelayer.TransactionsResponse.class)
                            .map(response -> response.getResults());
                });
    }

    public Mono<List<Transaction>> getTransactionsForAccounts(OAuth2AuthenticationToken oauthToken, List<String> accountIds) {
        return Flux.fromIterable(accountIds)
                .flatMap(accountId -> getTransactions(oauthToken, accountId))
                .flatMap(Flux::fromIterable)
                .collectList();
    }

    private Account mapToAccount(AccountDto dto) {
        Account account = new Account();
        account.setAccountId(dto.getAccountId());
        account.setDisplayName(dto.getDisplayName());
        account.setAccountType(dto.getAccountType());
        account.setCurrency(dto.getCurrency());
        account.setAccountNumber(dto.getAccountNumber().getNumber());
        account.setSortCode(dto.getAccountNumber().getSortCode());
        account.setProvider(dto.getProvider().getDisplayName());
        return account;
    }
}
