package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.model.TrueLayerUser;

import com.example.demo.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService; // Inject your custom service

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) { // Constructor injection
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/info", "/", "/index.html", "/*.js", "/*.css", "/dashboard", "/login/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                   .userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> response.sendRedirect("/dashboard"))
            );
            
        return http.build();
    }
}