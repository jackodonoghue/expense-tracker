package com.example.demo.controller;

import com.example.demo.service.TrueLayerService;
import com.example.demo.model.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    private final TrueLayerService trueLayerService;

    public ExpenseController(TrueLayerService trueLayerService) {
        this.trueLayerService = trueLayerService;
    }

    @GetMapping("/accounts")
    public Mono<ResponseEntity<List<Account>>> getUserAccounts(OAuth2AuthenticationToken oauthToken) {
        if (oauthToken == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return trueLayerService.getUserAccounts(oauthToken)
                .map(accounts -> {
                    logger.debug("Fetched {} accounts for user {}", accounts.size(), oauthToken.getName());
                    return ResponseEntity.ok(accounts);
                })
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }
}