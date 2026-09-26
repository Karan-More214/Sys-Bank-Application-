package com.example.BankManagement.Security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and validates the JWTs that back /api/** auth. The secret must be at least
 * 256 bits for HS256; JWT_SECRET should be set as a real env var in every deployed
 * environment (Render, etc.) - the fallback below is for local dev only and is not a
 * secret worth protecting, since anyone can read it straight out of this source file.
 */
@Service
public class JwtService {

    @Value("${jwt.secret:local-dev-only-secret-key-change-me-please-32bytes+}")
    private String secret;

    @Value("${jwt.expiration-ms:14400000}")
    private long expirationMs;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    /** Returns the validated claims, or null if the token is missing, malformed, expired, or has a bad signature. */
    public Claims validateAndParse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
