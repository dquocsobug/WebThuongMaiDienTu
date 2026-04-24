package com.example.webbanhang.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:webbanhang-secret-key-must-be-at-least-256-bits-long-for-hs256}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs; // mặc định 24 giờ

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Tạo token
    public String generateToken(String email, String role, Integer userId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // Lấy email từ token
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // Lấy role từ token
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Lấy userId từ token
    public Integer getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Integer.class);
    }

    // Kiểm tra token còn hạn không
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}