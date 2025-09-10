package com.example.demo.controller;

import com.example.demo.service.TrueLayerService;
import com.example.demo.model.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @Autowired
    private TrueLayerService trueLayerService;

    /**
     * Get user's bank accounts
     */
    @GetMapping("/accounts")
    public ResponseEntity<?> getUserAccounts(OAuth2AuthenticationToken oauthToken) {
        if (oauthToken == null) {
            throw new IllegalStateException("User not authenticated");
        }
        List<Account> accounts = trueLayerService.getUserAccounts(oauthToken);

        logger.info("Fetched {} accounts for user {}", accounts.size(), oauthToken.getName());
        
        return ResponseEntity.ok(accounts);
    }
}