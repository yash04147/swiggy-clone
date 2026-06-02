package com.yash.delivery.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // ⚠️ In production, NEVER hardcode secret
    private static final String SECRET =
            "my-super-secret-key-my-super-secret-key";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Token validity: 24 hours
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24;

    // 🔑 Generate token
    public String generateToken(String userId, String email, String role) {

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    // 📖 Extract claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 🆔 Extract user ID
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ⏰ Check expiration
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // ✅ Validate token
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }
}