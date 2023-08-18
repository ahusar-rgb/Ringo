package com.ringo.auth;

import com.auth0.jwt.JWT;
import com.ringo.config.ApplicationProperties;
import com.ringo.exception.AuthException;
import com.ringo.exception.InternalException;
import com.ringo.model.security.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AppleIdService implements IdProvider {

    private static final String APPLE_ISS = "https://appleid.apple.com";
    private final String appleKeysUrl = "https://appleid.apple.com/auth/keys";

    private final ApplicationProperties config;

    @Override
    public User getUserFromToken(String token) {
        Claims claims = decodeJwt(token);
        String email = claims.get("email").toString();
        boolean emailVerified = Boolean.parseBoolean(claims.get("email_verified").toString());
        return User.builder()
                .email(email)
                .emailVerified(emailVerified)
                .build();
    }

    private PublicKey getPublicKey(String publicKeyString, String publicKeyExponent) {
        BigInteger n = new BigInteger(1, Decoders.BASE64URL.decode(publicKeyString));
        BigInteger e = new BigInteger(1, Decoders.BASE64URL.decode(publicKeyExponent));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec publicKeySpec = new RSAPublicKeySpec(n, e);

            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception ex) {
            throw new InternalException("Error while composing public key");
        }
    }

    private Claims decodeJwt(String jwt) {
        PublicKey publicKey = getPublicKeyFromJwt(jwt);

        Claims claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(jwt)
                .getBody(); // will throw exception if token is expired, etc.

        if(!claims.get("iss").equals(APPLE_ISS))
            throw new AuthException("Invalid issuer");
        if(!claims.get("aud").equals(config.getAppleAud()))
            throw new AuthException("Invalid audience");

        return claims;
    }

    private PublicKey getPublicKeyFromJwt(String jwt) {
        String kid = JWT.decode(jwt).getKeyId();
        Response response = RestAssured.given().get(appleKeysUrl);

        KeySet keySet = response.getBody().as(KeySet.class);

        KeyDto key = Arrays.stream(keySet.keys).filter(k -> k.kid.equals(kid)).findFirst().orElseThrow(
                () -> new InternalException("Key not found"));

        return getPublicKey(key.n, key.e);
    }

    @Getter
    @Setter
    private static class KeyDto {
        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }

    @Getter
    @Setter
    private static class KeySet {
        private KeyDto[] keys;
    }
}
