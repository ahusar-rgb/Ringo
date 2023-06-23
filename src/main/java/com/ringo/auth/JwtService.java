package com.ringo.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ringo.exception.AuthException;
import com.ringo.model.company.Ticket;
import com.ringo.model.security.User;
import lombok.AllArgsConstructor;

import java.time.ZoneOffset;
import java.util.Date;

@AllArgsConstructor
public class JwtService {

    private final AuthenticationProperties config;

    public String generateAccessToken(User user) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withExpiresAt(new Date((new Date()).getTime() + config.getAccessTokenExpirationMillis()))
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().name())
                .withClaim("refresh", false)
                .sign(algorithm);
    }

    public String generateTicketCode(Ticket ticket) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(ticket.getId().getParticipant().getEmail())
                .withExpiresAt(ticket.getExpiryDate().toInstant(ZoneOffset.UTC))
                .withClaim("event", ticket.getId().getEvent().getId())
                .withClaim("participant", ticket.getId().getParticipant().getId())
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withExpiresAt(new Date((new Date()).getTime() + config.getRefreshTokenExpirationMillis()))
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().name())
                .withClaim("refresh", true)
                .sign(algorithm);
    }

    public String generateRecoverPasswordToken(User user) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withClaim("recover", true)
                .withExpiresAt(new Date((new Date()).getTime() + config.getRecoverPasswordTokenExpirationMillis()))
                .sign(algorithm);
    }

    public boolean isTokenValid(User user, String token) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(config.getIssuer()).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject().equals(user.getEmail());
        } catch (TokenExpiredException e) {
            throw new AuthException("Token expired");
        } catch (Exception e) {
            throw new AuthException("Token is not valid");
        }
    }

    public String getEmailFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getSubject();
    }

    private boolean isTokenExpired(DecodedJWT jwt) {
        Date now = new Date();
        return now.after(jwt.getExpiresAt());
    }

    public Date getTokenExpirationDate(String token) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret());
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(config.getIssuer()).withClaim("refresh", false).build();
        DecodedJWT jwt = verifier.verify(token);

        return jwt.getExpiresAt();
    }

    public DecodedJWT verifyTicketCode(String ticketCode) {
        try {
            Algorithm algorithm = Algorithm.HMAC512(config.getSecret());
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(config.getIssuer()).build();
            return verifier.verify(ticketCode);
        } catch (Exception e) {
            throw new AuthException("Ticket code is not valid");
        }
    }
}
