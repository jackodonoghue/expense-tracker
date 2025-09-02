package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.model.Balance;
import com.example.demo.model.Transaction;
import com.example.demo.model.ErrorResponse;
import com.example.demo.service.TrueLayerService;
import com.example.demo.service.TokenStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expenses")
@CrossOrigin(origins = {"http://localhost:4200"})
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @Autowired
    private TrueLayerService trueLayerService;

    @Autowired
    private TokenStorageService tokenStorageService;

    /**
     * Get user's bank accounts
     */
    @GetMapping("/accounts")
    public ResponseEntity<?> getAccounts(@RequestParam String sessionId) {
        try {
            String accessToken = tokenStorageService.getToken(sessionId);
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                   .body(new ErrorResponse("No access token found", "Please authenticate first"));
            }

            List<Account> accounts = trueLayerService.getUserAccounts(accessToken);
            logger.info("Retrieved {} accounts for session: {}", accounts.size(), sessionId);

            return ResponseEntity.ok(accounts);

        } catch (Exception e) {
            logger.error("Error retrieving accounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(new ErrorResponse("Failed to retrieve accounts", e.getMessage()));
        }
    }

    /**
     * Get transactions for a specific account
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam String sessionId,
            @RequestParam(required = false) String accountId,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            String accessToken = tokenStorageService.getToken(sessionId);
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                   .body(new ErrorResponse("No access token found", "Please authenticate first"));
            }

            List<Transaction> transactions;
            if (accountId != null) {
                transactions = trueLayerService.getAccountTransactions(accessToken, accountId, limit);
            } else {
                transactions = trueLayerService.getAllTransactions(accessToken, limit);
            }

            logger.info("Retrieved {} transactions for session: {}", transactions.size(), sessionId);

            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            logger.error("Error retrieving transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(new ErrorResponse("Failed to retrieve transactions", e.getMessage()));
        }
    }

    /**
     * Get account balances
     */
    @GetMapping("/balances")
    public ResponseEntity<?> getBalances(@RequestParam String sessionId) {
        try {
            String accessToken = tokenStorageService.getToken(sessionId);
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                   .body(new ErrorResponse("No access token found", "Please authenticate first"));
            }

            List<Balance> balances = trueLayerService.getAccountBalances(accessToken);
            logger.info("Retrieved {} account balances for session: {}", balances.size(), sessionId);

            return ResponseEntity.ok(balances);

        } catch (Exception e) {
            logger.error("Error retrieving balances", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(new ErrorResponse("Failed to retrieve balances", e.getMessage()));
        }
    }

    /**
     * Get user information
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestParam String sessionId) {
        try {
            String accessToken = tokenStorageService.getToken(sessionId);
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                   .body(new ErrorResponse("No access token found", "Please authenticate first"));
            }

            Map<String, Object> userInfo = trueLayerService.getUserInfo(accessToken);
            logger.info("Retrieved user info for session: {}", sessionId);

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("Error retrieving user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(new ErrorResponse("Failed to retrieve user info", e.getMessage()));
        }
    }
}