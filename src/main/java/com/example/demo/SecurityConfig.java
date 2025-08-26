package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository)
            throws Exception {

        // default resolver that Spring uses
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        // wrap the resolver to log + customize
        OAuth2AuthorizationRequestResolver customResolver = new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return customize(defaultResolver.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return customize(defaultResolver.resolve(request, clientRegistrationId));
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
                if (req == null) return null;

                Map<String, Object> extraParams = new HashMap<>(req.getAdditionalParameters());
                extraParams.put("providers", "uk-cs-mock uk-ob-all uk-oauth-all");

                OAuth2AuthorizationRequest newReq = OAuth2AuthorizationRequest.from(req)
                        .additionalParameters(extraParams)
                        .build();

                // 👇 log the actual URL that Spring will redirect to
                System.out.println("🔗 OAuth2 Authorization Request URL: "
                        + newReq.getAuthorizationRequestUri());

                return newReq;
            }
        };

        http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(authz -> authz
                    .authorizationRequestResolver(customResolver)
                )
            );

        return http.build();
    }
}
