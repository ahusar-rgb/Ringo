package com.ringo.config;

public class Constants {

    public final static String TOKEN_PREFIX = "Bearer ";
    public static final Object NO_CREDENTIALS = null;
    public static final String[] SIGN_UP_URLS = {
            "/api/organisations/sign-up/google",
            "/api/participants/sign-up/google",
            "/api/auth/login/google",
            "/oauth2/authorization/google",
            "/login/oauth2/code/google"
    };
    public static String[] PUBLIC_URLS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/**",
            "/api/organisations/sign-up",
            "/api/participants/sign-up"
    };
}
