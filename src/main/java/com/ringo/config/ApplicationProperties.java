package com.ringo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {
    private Integer mergeDistanceFactor;
    private Integer maxPhotoCount;
    private String googleClientId;
    private String appleAud1;
    private String appleAud2;
    private String appleAud3;
    private String adminLogin;
    private String adminPassword;
    private String domainName;
}
