package com.example.demo;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String SECRET = "MySuperSecretKeyForJWTThatIsLongEnough123!";
    private static final long EXPIRY = 1000 * 60 * 60 * 10; // 10 hours

    private SecretKey getSigninKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateTokens(String username) {
        return Jwts.builder().subject(username).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRY)).signWith(getSigninKey()).compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
