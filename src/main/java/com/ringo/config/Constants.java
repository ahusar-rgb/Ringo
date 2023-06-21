package com.ringo.config;

public class Constants {

    public final static String TOKEN_PREFIX = "Bearer ";
    public static final Object NO_CREDENTIALS = null;
    public static String[] PUBLIC_URLS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/**",
            "/api/organisations/sign-up/**",
            "/api/participants/sign-up/**"
    };
}
