package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserMapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserMapperService userMapperService;

    @Mock
    private OAuth2AuthenticationToken oauthToken;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserInfo_returnsUnauthorized_whenNoToken() {
        Mono<ResponseEntity<UserDTO>> response = authController.getUserInfo(null);
        ResponseEntity<UserDTO> entity = response.block();
        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Test
    void getUserInfo_returnsUserInfo_whenTokenIsValid() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFullName("Test User");

        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        when(authService.getAuthorisedUser(oauthToken)).thenReturn(Mono.just(authorizedClient));
        when(userMapperService.mapToUserDTO(authorizedClient)).thenReturn(userDTO);

        Mono<ResponseEntity<UserDTO>> response = authController.getUserInfo(oauthToken);
        ResponseEntity<UserDTO> entity = response.block();

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(userDTO, entity.getBody());
        verify(authService, times(1)).getAuthorisedUser(oauthToken);
        verify(userMapperService, times(1)).mapToUserDTO(authorizedClient);
    }
}
