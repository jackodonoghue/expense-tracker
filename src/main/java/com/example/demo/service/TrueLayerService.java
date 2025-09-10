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
    private final OAuth2AuthorizedClientService authorizedClientService;

    public TrueLayerService(WebClient webClient,
                            OAuth2AuthorizedClientService authorizedClientService) {
        this.webClient = webClient;
        this.authorizedClientService = authorizedClientService;
    }

    public Mono<List<Account>> getUserAccounts(OAuth2AuthenticationToken oauthToken) {
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        String principalName = oauthToken.getName();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(registrationId, principalName);

        if (authorizedClient == null) {
            return Mono.error(new IllegalStateException("Authorized client not found for user: " + principalName));
        }

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
