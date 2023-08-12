package com.ringo.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt", ignoreUnknownFields = false)
@Data
public class AuthenticationProperties {
    private String secret;
    private String issuer;
    private long accessTokenExpirationMillis;
    private long refreshTokenExpirationMillis;
    private long recoverPasswordTokenExpirationMillis;
    private long emailVerificationTokenExpirationMillis;
}
