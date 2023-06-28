package com.ringo.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.ringo.exception.UserException;
import com.ringo.model.security.User;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleIdTokenService implements IdProvider{

    private static final String CLIENT_ID = "546639981524-u4a1kdbkmndq1ms0fo7s3ldtrmsufg4k.apps.googleusercontent.com";
    private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();


    @Override
    public User getUserFromToken(String token) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(token);
        } catch (Exception e) {
            throw new UserException("Invalid Google Id Token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        // Print user identifier
        String userId = payload.getSubject();
        System.out.println("User ID: " + userId);

        // Get profile information from payload
        String email = payload.getEmail();
        boolean emailVerified = payload.getEmailVerified();
        String name = (String) payload.get("name");

        return User.builder()
                .email(email)
                .name(name)
                .build();
    }
}
