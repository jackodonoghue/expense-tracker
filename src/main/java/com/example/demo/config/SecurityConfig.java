package com.example.demo.config;

import com.example.demo.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/", "/favicon.ico", "/index.html", "/*.js", "/*.css",
                                "/login/**", "/oauth2/**")
                        .permitAll()
                        .anyExchange().authenticated())
                .logout(logout -> logout
                        .logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler(HttpStatus.OK)))
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                            webFilterExchange.getExchange().getResponse().getHeaders().add("Location", "/dashboard");
                            return webFilterExchange.getChain().filter(webFilterExchange.getExchange());
                        }));

        return http.build();
    }
}