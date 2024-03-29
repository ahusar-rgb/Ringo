package com.ringo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {
    private Integer mergeDistanceFactor;
    private Integer maxPhotoCount;
    private String googleClientId1;
    private String googleClientId2;
    private String googleClientId3;
    private String appleAud1;
    private String appleAud2;
    private String appleAud3;
    private String adminLogin;
    private String adminPassword;
    private String domainName;
    private String stripePublicKey;
    private String stripeSecretKey;
    private String stripeWebhookSecret;
    private Double applicationFeeInPercent;
    private String stripeReturnUrl;
    private String stripeReauthUrl;
    private String noReplyPassword;
    private String ticketLifetimeAfterEventEndInDays;
    private String paymentTimeoutInMinutes;
}
