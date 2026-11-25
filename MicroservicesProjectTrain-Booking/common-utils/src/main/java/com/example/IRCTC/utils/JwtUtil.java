package com.example.IRCTC.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.IRCTC.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/** Issues and verifies signed access tokens using externally supplied configuration. */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final Duration tokenExpiration;

    public JwtUtil(JwtProperties properties) {
        if (!StringUtils.hasText(properties.getSecret())) {
            throw new IllegalStateException(
                    "APP_SECURITY_JWT_SECRET must be set to a Base64-encoded secret of at least 32 bytes.");
        }

        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret().trim()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "APP_SECURITY_JWT_SECRET must be valid Base64 and decode to at least 32 bytes.", exception);
        }

        this.tokenExpiration = properties.getExpiration();
        if (tokenExpiration == null || tokenExpiration.isZero() || tokenExpiration.isNegative()) {
            throw new IllegalStateException("app.security.jwt.expiration must be a positive duration.");
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get("roles");
        if (!(roles instanceof List<?> roleValues)) {
            return List.of();
        }
        return roleValues.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(String username) {
        return generateToken(username, List.of());
    }

    public String generateToken(String username, List<String> roles) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("A username is required to issue an access token.");
        }

        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles == null ? List.of() : List.copyOf(roles))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(tokenExpiration)))
                .signWith(signingKey)
                .compact();
    }

    /** Returns false for malformed, unsigned, expired, or otherwise invalid tokens. */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return StringUtils.hasText(claims.getSubject())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
