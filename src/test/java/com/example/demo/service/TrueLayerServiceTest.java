package com.example.demo.service;

import com.example.demo.model.Account;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.when;

class TrueLayerServiceTest {

    private MockWebServer mockWebServer;
    private TrueLayerService trueLayerService;

    @Mock
    private AuthService authService;

    @Mock
    private OAuth2AuthenticationToken oauthToken;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private OAuth2AccessToken accessToken;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        trueLayerService = new TrueLayerService(webClient, authService);

        when(authService.getAuthorisedUser(oauthToken)).thenReturn(Mono.just(authorizedClient));
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-token");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getUserAccounts_returnsAccounts() throws Exception {
        String jsonResponse = "{\"results\":[{\"account_id\":\"acc1\",\"display_name\":\"Test Account 1\",\"account_type\":\"TRANSACTION\",\"currency\":\"GBP\",\"account_number\":{\"number\":\"12345\",\"sort_code\":\"11-22-33\"},\"provider\":{\"display_name\":\"Test Bank\"}},{\"account_id\":\"acc2\",\"display_name\":\"Test Account 2\",\"account_type\":\"TRANSACTION\",\"currency\":\"GBP\",\"account_number\":{\"number\":\"12345\",\"sort_code\":\"11-22-33\"},\"provider\":{\"display_name\":\"Test Bank\"}}]}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Mono<List<Account>> result = trueLayerService.getUserAccounts(oauthToken);

        StepVerifier.create(result)
                .expectNextMatches(accounts -> accounts.size() == 2 &&
                        accounts.get(0).getAccountId().equals("acc1") &&
                        accounts.get(1).getAccountId().equals("acc2"))
                .verifyComplete();
    }
}
