package com.ringo.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ringo.model.security.User;
import lombok.AllArgsConstructor;

import java.util.Date;

@AllArgsConstructor
public class JwtService {

    private final AuthenticationProperties config;

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getUsername())
                .withExpiresAt(new Date((new Date()).getTime() + config.getJwtExpirationMillis()))
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().name())
                .sign(algorithm);
    }

    public String getUsernameFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret());
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(config.getIssuer()).build();
        DecodedJWT jwt = verifier.verify(token);
        if (isTokenExpired(jwt)) {
            throw new RuntimeException("Token expired");
        }
        return jwt.getSubject();
    }

    private boolean isTokenExpired(DecodedJWT jwt) {
        Date now = new Date();
        return now.after(jwt.getExpiresAt());
    }
}
