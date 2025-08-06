package com.example.bankcards.security;

import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.InvalidJwtToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken
            (UUID id, String username, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("id", id.toString())
                .claim("role", role.getValue())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken
            (String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtToken("Invalid or expired token", e);
        }
    }

    public Claims extractClaims
            (String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtToken("Invalid claims: " + e.getMessage(), e);
        }
    }

    public String extractUserName(String token) {
        return extractClaims(token).getSubject();
    }

    public UUID extractId(String token) {
        return UUID.fromString(extractClaims(token).get("id", String.class));
    }

    public Role extractRole(String token) {
        String roleString = extractClaims(token).get("role", String.class);
        return Role.valueOf(roleString);
    }

}
