package com.example.demo.config;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.ResponseCookie;
import java.time.Duration;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                                .authorizeExchange(authorize -> authorize
                                                .pathMatchers("/", "/favicon.ico", "/index.html", "/*.js", "/*.css",
                                                                "/login")
                                                .permitAll()
                                                .anyExchange().authenticated())
                                .logout(l -> l
                                                .logoutSuccessHandler((webFilterExchange, authentication) -> {
                                                        webFilterExchange.getExchange().getResponse()
                                                                        .setStatusCode(HttpStatus.OK);
                                                        webFilterExchange.getExchange().getResponse()
                                                                        .addCookie(ResponseCookie.from("SESSION", "")
                                                                                        .path("/")
                                                                                        .maxAge(Duration.ZERO)
                                                                                        .build());
                                                        return webFilterExchange.getExchange().getResponse()
                                                                        .setComplete();
                                                }))
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage(("/login"))
                                                .authenticationManager(new DelegatingReactiveAuthenticationManager(
                                                                new OAuth2LoginReactiveAuthenticationManager(
                                                                                new WebClientReactiveAuthorizationCodeTokenResponseClient(),
                                                                                oauth2UserService()))));

                return http.build();
        }

        @Bean
        public ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
                DefaultReactiveOAuth2UserService delegate = new DefaultReactiveOAuth2UserService();

                delegate.setAttributesConverter(userRequest -> attributes -> {
                        if (attributes.containsKey("results")) {
                                List<Map<String, Object>> results = (List<Map<String, Object>>) attributes
                                                .get("results");

                                if (results != null && !results.isEmpty()) {
                                        return results.get(0);
                                }
                        }

                        return attributes;
                });

                return delegate;
        }
}