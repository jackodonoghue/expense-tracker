package com.example.demo.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Value("${user-info-uri}")
    private String userInfoUri;

    private final WebClient webClient;

    public CustomOAuth2UserService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2AccessToken accessToken = userRequest.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = webClient.get()
                .uri(userInfoUri)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(Map.class)
                .block();

        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null || !responseBody.containsKey("results")) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info_response"),
                    "Invalid user info response");
        }

        // Extract the first user from the results array
        List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");

        if (results == null || results.isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("empty_user_info_results"), "No user info found");
        }

        Map<String, Object> userInfo = results.get(0);

        // Create a flat map with attributes you want to expose in OAuth2User
        Map<String, Object> attributes = Map.of(
                "full_name", userInfo.get("full_name"),
                "emails", userInfo.get("emails"),
                "phones", userInfo.get("phones"),
                "addresses", userInfo.get("addresses"),
                "update_timestamp", userInfo.get("update_timestamp"));

        // Use full_name as the key for the user name attribute (adjust as needed)
        return new DefaultOAuth2User(
                Collections.singletonList(() -> "ROLE_USER"),
                attributes,
                "full_name");
    }
}
