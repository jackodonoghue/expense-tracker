package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.AccountDto;
import com.example.demo.model.AccountsResponse;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
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
        OAuth2AuthorizedClient authorizedClient = authService.getAuthorisedUser(oauthToken);

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient.get()
                .uri("/accounts")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(AccountsResponse.class)  // Map to typed DTO
                .map(response -> response.getResults().stream()
                        .map(this::mapToAccount)
                        .collect(Collectors.toList()));
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
