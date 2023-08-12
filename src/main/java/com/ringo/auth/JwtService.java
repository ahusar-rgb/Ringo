package com.ringo.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
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

    private final String TYPE_CLAIM = "type";

    public String generateAccessToken(User user) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());

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
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(ticket.getId().getParticipant().getEmail())
                .withExpiresAt(ticket.getExpiryDate().toInstant(ZoneOffset.UTC))
                .withClaim(TYPE_CLAIM, TokenType.TICKET.getValue())
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
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());

        return JWT.create()
                .withIssuer(config.getIssuer())
                .withSubject(user.getEmail())
                .withClaim(TYPE_CLAIM, TokenType.EMAIL_VERIFICATION.getValue())
                .withExpiresAt(new Date((new Date()).getTime() + config.getEmailVerificationTokenExpirationMillis()))
                .sign(algorithm);
    }

    public boolean isTokenValid(User user, String token, TokenType type) {
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret() + user.getPassword());
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(config.getIssuer()).build();
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
        Algorithm algorithm = Algorithm.HMAC512(config.getSecret());
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(config.getIssuer()).build();
        try {
            return verifier.verify(ticketCode);
        } catch (Exception e) {
            throw new AuthException("Ticket code is not valid");
        }
    }

    public String getEmailFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getSubject();
    }
}
