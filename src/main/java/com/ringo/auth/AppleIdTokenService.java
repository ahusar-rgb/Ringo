package com.ringo.auth;

import com.ringo.exception.InternalException;
import com.ringo.exception.UserException;
import com.ringo.model.security.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

@Component
public class AppleIdTokenService {

    public User getUserFromToken(String token) {
        try {
            Claims claims = decodeJwt(token);
            String email = claims.get("email").toString();
            return User.builder().email(email).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserException("Invalid token");
        }
    }

    private PublicKey getPublicKey() throws Exception {
        String publicKeyString = "1JiU4l3YCeT4o0gVmxGTEK1IXR-Ghdg5Bzka12tzmtdCxU00ChH66aV-4HRBjF1t95IsaeHeDFRgmF0lJbTDTqa6_VZo2hc0zTiUAsGLacN6slePvDcR1IMucQGtPP5tGhIbU-HKabsKOFdD4VQ5PCXifjpN9R-1qOR571BxCAl4u1kUUIePAAJcBcqGRFSI_I1j_jbN3gflK_8ZNmgnPrXA0kZXzj1I7ZHgekGbZoxmDrzYm2zmja1MsE5A_JX7itBYnlR41LOtvLRCNtw7K3EFlbfB6hkPL-Swk5XNGbWZdTROmaTNzJhV-lWT0gGm6V1qWAK2qOZoIDa_3Ud0Gw";
        String publicKeyExponent = "AQAB";

        BigInteger n = new BigInteger(1, Decoders.BASE64URL.decode(publicKeyString));
        BigInteger e = new BigInteger(1, Decoders.BASE64URL.decode(publicKeyExponent));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        KeySpec publicKeySpec = new RSAPublicKeySpec(n, e);

        return keyFactory.generatePublic(publicKeySpec);
    }

    private Claims decodeJwt(String jwt) throws Exception {
        PublicKey publicKey = getPublicKey();
        Claims claims =  Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(jwt)
                .getBody(); // will throw exception if token is expired, etc.

        if(!claims.get("iss").equals("https://appleid.apple.com"))
            throw new InternalException("Invalid issuer");
        if(!claims.get("aud").equals("com.andrii-kuiava.RingoApp"))
            throw new InternalException("Invalid audience");

        return claims;
    }
}
