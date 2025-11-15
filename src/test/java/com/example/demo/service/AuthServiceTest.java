package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private ReactiveOAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private OAuth2AuthenticationToken oauthToken;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAuthorisedUser_callsLoadAuthorizedClient() {
        String registrationId = "test-registration-id";
        String principalName = "test-principal-name";

        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn(registrationId);
        when(oauthToken.getName()).thenReturn(principalName);
        when(authorizedClientService.loadAuthorizedClient(registrationId, principalName))
                .thenReturn(Mono.just(authorizedClient));

        Mono<OAuth2AuthorizedClient> result = authService.getAuthorisedUser(oauthToken);

        StepVerifier.create(result)
                .expectNext(authorizedClient)
                .verifyComplete();

        verify(authorizedClientService, times(1)).loadAuthorizedClient(registrationId, principalName);
    }
}
