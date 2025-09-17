package com.example.demo.service;

import com.example.demo.dto.UserDTO;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

@Service
public class UserMapperService {
    public UserDTO mapToUserDTO(OAuth2AuthorizedClient oauthToken) {
        String fullName = oauthToken.getPrincipalName();
        return new UserDTO(fullName);
    }
}
