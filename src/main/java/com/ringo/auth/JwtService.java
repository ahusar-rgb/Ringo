package com.ringo.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ringo.exception.UserException;
import com.ringo.model.company.Ticket;
import com.ringo.model.security.User;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

@AllArgsConstructor
public class JwtService {

    private final AuthenticationProperties config;

    private final String TYPE_CLAIM = "type";

    public String generateAccessToken(User user) {
        Algorithm algorithm = config.isUsePasswordHash() ? getAlgorithmWithPasswordHash(user) : getAlgorithmWithoutPasswordHash();

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withExpiresAt(new Date((new Date()).getTime() + config.getAccessTokenExpirationMillis()))
                .withClaim(TYPE_CLAIM, TokenType.ACCESS.getValue())
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().name())
                .sign(algorithm);
    }

    public String generateTicketCode(Ticket ticket) {
        Algorithm algorithm = getAlgorithmWithoutPasswordHash();

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(ticket.getId().getParticipant().getEmail())
                .withExpiresAt(Instant.from(ticket.getExpiryDate().atZone(ZoneOffset.UTC)))
                .withClaim(TYPE_CLAIM, TokenType.TICKET.getValue())
                .withClaim("event", ticket.getId().getEvent().getId())
                .withClaim("participant", ticket.getId().getParticipant().getId())
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        Algorithm algorithm = config.isUsePasswordHash() ? getAlgorithmWithPasswordHash(user) : getAlgorithmWithoutPasswordHash();

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withExpiresAt(new Date((new Date()).getTime() + config.getRefreshTokenExpirationMillis()))
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().name())
                .withClaim(TYPE_CLAIM, TokenType.REFRESH.getValue())
                .sign(algorithm);
    }

    public String generateRecoverPasswordToken(User user) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withClaim(TYPE_CLAIM, TokenType.RECOVER.getValue())
                .withExpiresAt(new Date((new Date()).getTime() + config.getRecoverPasswordTokenExpirationMillis()))
                .sign(algorithm);
    }

    public String generateEmailVerificationToken(User user) {
        Algorithm algorithm = config.isUsePasswordHash() ? getAlgorithmWithPasswordHash(user) : getAlgorithmWithoutPasswordHash();

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withClaim("username", user.getUsername())
                .withClaim(TYPE_CLAIM, TokenType.EMAIL_VERIFICATION.getValue())
                .withExpiresAt(new Date((new Date()).getTime() + config.getEmailVerificationTokenExpirationMillis()))
                .sign(algorithm);
    }

    public boolean isTokenValid(User user, String token, TokenType type) {
        JWTVerifier verifier;
        if(type == TokenType.RECOVER) {
            verifier = getVerifierWithPasswordHash(user);
        } else {
            verifier = config.isUsePasswordHash() ? getVerifierWithPasswordHash(user) : getVerifierWithoutPasswordHash();
        }

        try {
            DecodedJWT jwt = verifier.verify(token);
            Claim typeClaim = jwt.getClaim(TYPE_CLAIM);
            if(typeClaim.isMissing() || !typeClaim.asString().equals(type.getValue()))
                return false;
            return jwt.getSubject().equals(user.getEmail());
        } catch (Exception e) {
            return false;
        }
    }

    public DecodedJWT verifyTicketCode(String ticketCode) {
        JWTVerifier verifier = getVerifierWithoutPasswordHash();
        try {
            return verifier.verify(ticketCode);
        } catch (Exception e) {
            throw new UserException("Ticket code is not valid");
        }
    }

    public String getEmailFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getSubject();
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("username").asString();
    }

    private JWTVerifier getVerifierWithPasswordHash(User user) {
        Algorithm algorithm = getAlgorithmWithPasswordHash(user);
        return JWT.require(algorithm).withIssuer(config.getIssuer()).build();
    }

    private Algorithm getAlgorithmWithPasswordHash(User user) {
        return Algorithm.HMAC512(config.getSecret() + user.getPassword());
    }

    private JWTVerifier getVerifierWithoutPasswordHash() {
        Algorithm algorithm = getAlgorithmWithoutPasswordHash();
        return JWT.require(algorithm).withIssuer(config.getIssuer()).build();
    }

    private Algorithm getAlgorithmWithoutPasswordHash() {
        return Algorithm.HMAC512(config.getSecret());
    }
}
