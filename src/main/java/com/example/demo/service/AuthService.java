package com.example.demo.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public AuthService(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public Mono<OAuth2AuthorizedClient> getAuthorisedUser(OAuth2AuthenticationToken oauthToken) {
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        String principalName = oauthToken.getName();

        return authorizedClientService.loadAuthorizedClient(registrationId, principalName);
    }
}
