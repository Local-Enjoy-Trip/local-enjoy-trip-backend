package com.ssafy.enjoytrip.core.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class OAuth2ClientConfig {
    @Bean
    @ConditionalOnProperty(prefix = "enjoytrip.oauth2.google", name = {"client-id", "client-secret"})
    ClientRegistrationRepository clientRegistrationRepository(GoogleOAuthProperties properties) {
        ClientRegistration google = CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .scope("openid", "email", "profile")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }
}
