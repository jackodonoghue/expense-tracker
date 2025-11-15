package com.example.demo.controller;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import com.example.demo.model.Account;
import com.example.demo.model.truelayer.Transaction;
import com.example.demo.service.TrueLayerService;

import reactor.core.publisher.Mono;

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
    void getTransactions_returnsUnauthorized_whenNoToken() {
        Mono<ResponseEntity<List<Transaction>>> response = expenseController.getTransactions(null, null, 100);
        ResponseEntity<List<Transaction>> entity = response.block();
        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Test
    void getTransactions_returnsAllTransactions_whenNoAccountIds() {
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        when(t1.getTimestamp()).thenReturn(new Date());
        when(t2.getTimestamp()).thenReturn(new Date());
        List<Transaction> transactions = Arrays.asList(t1, t2);
        when(trueLayerService.getAllTransactions(oauthToken)).thenReturn(Mono.just(transactions));

        Mono<ResponseEntity<List<Transaction>>> response = expenseController.getTransactions(oauthToken, null, 100);
        ResponseEntity<List<Transaction>> entity = response.block();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(transactions, entity.getBody());
        verify(trueLayerService, times(1)).getAllTransactions(oauthToken);
        verify(trueLayerService, never()).getTransactionsForAccounts(any(), any());
    }

    @Test
    void getTransactions_returnsTransactionsForAccountIds_whenAccountIdsProvided() {
        List<String> accountIds = Arrays.asList("acc1", "acc2");
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        when(t1.getTimestamp()).thenReturn(new Date());
        when(t2.getTimestamp()).thenReturn(new Date());
        List<Transaction> transactions = Arrays.asList(t1, t2);
        when(trueLayerService.getTransactionsForAccounts(oauthToken, accountIds)).thenReturn(Mono.just(transactions));

        Mono<ResponseEntity<List<Transaction>>> response = expenseController.getTransactions(oauthToken, accountIds,
                100);
        ResponseEntity<List<Transaction>> entity = response.block();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(transactions, entity.getBody());
        verify(trueLayerService, never()).getAllTransactions(oauthToken);
        verify(trueLayerService, times(1)).getTransactionsForAccounts(oauthToken, accountIds);
    }

    @Test
    void getTransactions_returnsSortedTransactions() {
        Transaction t1 = new Transaction();
        t1.setTimestamp(new Date(1000));
        Transaction t2 = new Transaction();
        t2.setTimestamp(new Date(2000));
        List<Transaction> transactions = Arrays.asList(t1, t2);

        when(trueLayerService.getAllTransactions(oauthToken)).thenReturn(Mono.just(transactions));

        Mono<ResponseEntity<List<Transaction>>> response = expenseController.getTransactions(oauthToken, null, 100);
        ResponseEntity<List<Transaction>> entity = response.block();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(t2, entity.getBody().get(0));
        assertEquals(t1, entity.getBody().get(1));
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

    @Test
    void getTransactions_returnsNoContent_whenNoTransactions() {
        when(trueLayerService.getAllTransactions(oauthToken)).thenReturn(Mono.empty());
        Mono<ResponseEntity<List<Transaction>>> response = expenseController.getTransactions(oauthToken, null, 100);
        ResponseEntity<List<Transaction>> entity = response.block();
        assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Test
    void getTransactions_returnsAllTransactions_whenAccountIdsIsEmpty() {
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        when(t1.getTimestamp()).thenReturn(new Date());
        when(t2.getTimestamp()).thenReturn(new Date());
        List<Transaction> transactions = Arrays.asList(t1, t2);
        when(trueLayerService.getAllTransactions(oauthToken)).thenReturn(Mono.just(transactions));

        Mono<ResponseEntity<List<Transaction>>> response = expenseController.getTransactions(oauthToken,
                Collections.emptyList(), 100);
        ResponseEntity<List<Transaction>> entity = response.block();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(transactions, entity.getBody());
        verify(trueLayerService, times(1)).getAllTransactions(oauthToken);
        verify(trueLayerService, never()).getTransactionsForAccounts(any(), any());
    }

    @Test
    void getTransactions_returnsInternalServerError_whenServiceFails() {
        when(trueLayerService.getAllTransactions(oauthToken)).thenReturn(Mono.error(new RuntimeException("Service failure")));

        Mono<ResponseEntity<List<Transaction>>> response = expenseController.getTransactions(oauthToken, null, 100);

        assertThrows(RuntimeException.class, response::block);
    }

    @Test
    void getUserAccounts_returnsInternalServerError_whenServiceFails() {
                when(trueLayerService.getUserAccounts(oauthToken)).thenReturn(Mono.error(new RuntimeException("Service failure")));

        Mono<ResponseEntity<List<Account>>> response = expenseController.getUserAccounts(oauthToken);

        assertThrows(RuntimeException.class, response::block);
    }
}