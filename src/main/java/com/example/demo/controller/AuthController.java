package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.AuthService;
import com.example.demo.service.UserMapperService;
import com.example.demo.dto.UserDTO;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapperService userMapperService;

    public AuthController(AuthService authService, UserMapperService userMapperService) {
        this.authService = authService;
        this.userMapperService = userMapperService;
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<UserDTO>> getUserInfo(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        if (oAuth2AuthenticationToken == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return authService.getAuthorisedUser(oAuth2AuthenticationToken)
                .map(userMapperService::mapToUserDTO)
                .map(ResponseEntity::ok);
    }
}
