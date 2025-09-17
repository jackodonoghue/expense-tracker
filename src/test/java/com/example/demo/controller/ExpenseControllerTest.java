package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.TrueLayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseControllerTest {
    @Mock
    private TrueLayerService trueLayerService;

    @Mock
    private OAuth2AuthenticationToken oauthToken;

    @InjectMocks
    private ExpenseController expenseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserAccounts_returnsUnauthorized_whenNoToken() {
        Mono<ResponseEntity<List<Account>>> response = expenseController.getUserAccounts(null);
        ResponseEntity<List<Account>> entity = response.block();
        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Test
    void getUserAccounts_returnsAccounts_whenAccountsExist() {
        List<Account> accounts = Arrays.asList(mock(Account.class), mock(Account.class));
        when(trueLayerService.getUserAccounts(oauthToken)).thenReturn(Mono.just(accounts));
        when(oauthToken.getName()).thenReturn("user1");

        Mono<ResponseEntity<List<Account>>> response = expenseController.getUserAccounts(oauthToken);
        ResponseEntity<List<Account>> entity = response.block();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(accounts, entity.getBody());
    }

    @Test
    void getUserAccounts_returnsNoContent_whenNoAccounts() {
        when(trueLayerService.getUserAccounts(oauthToken)).thenReturn(Mono.empty());
        Mono<ResponseEntity<List<Account>>> response = expenseController.getUserAccounts(oauthToken);
        ResponseEntity<List<Account>> entity = response.block();
        assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());
    }
}
