package com.example.demo.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class BankingService {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String sandboxBaseUrl = "https://api.truelayer-sandbox.com";

    public Map getAccounts(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                sandboxBaseUrl + "/data/v1/accounts",
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }
}
