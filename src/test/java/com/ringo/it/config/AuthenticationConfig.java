package com.ringo.it.config;

import com.ringo.auth.AuthenticationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfig {
    @Bean
    public AuthenticationProperties getAuthenticationProperties() {
        AuthenticationProperties config = new AuthenticationProperties();
        config.setIssuer("Ringo");
        config.setSecret("mysecret");
        config.setUsePasswordHash(false);
        config.setEmailVerificationTokenExpirationMillis(900000L);

        return config;
    }
}
