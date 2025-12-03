package org.delcom.app.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTests {

    @Test
    @DisplayName("Generate token berhasil")
    void generateToken_ShouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Extract user ID from token berhasil")
    void extractUserId_ShouldReturnCorrectUserId() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        
        UUID extractedUserId = JwtUtil.extractUserId(token);
        
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Validate token valid berhasil")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        
        boolean isValid = JwtUtil.validateToken(token, false);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validate token dengan ignoreExpired true berhasil")
    void validateToken_WithIgnoreExpired_ShouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        
        boolean isValid = JwtUtil.validateToken(token, true);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validate token invalid mengembalikan false")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        boolean isValid = JwtUtil.validateToken("invalid-token", false);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token kosong mengembalikan false")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        boolean isValid = JwtUtil.validateToken("", false);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Extract user ID dari token invalid mengembalikan null")
    void extractUserId_WithInvalidToken_ShouldReturnNull() {
        UUID extractedUserId = JwtUtil.extractUserId("invalid-token");
        
        assertNull(extractedUserId);
    }

    @Test
    @DisplayName("Extract user ID dari token kosong mengembalikan null")
    void extractUserId_WithEmptyToken_ShouldReturnNull() {
        UUID extractedUserId = JwtUtil.extractUserId("");
        
        assertNull(extractedUserId);
    }

    @Test
    @DisplayName("Extract user ID dari token null mengembalikan null")
    void extractUserId_WithNullToken_ShouldReturnNull() {
        UUID extractedUserId = JwtUtil.extractUserId(null);
        
        assertNull(extractedUserId);
    }

    @Test
    @DisplayName("Validate token dengan expired token dan ignoreExpired false mengembalikan false")
    void validateToken_WithExpiredTokenAndIgnoreExpiredFalse_ShouldReturnFalse() {
        // Create an expired token by generating one with expiration in the past
        UUID userId = UUID.randomUUID();
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new java.util.Date(System.currentTimeMillis() - 100000))
                .expiration(new java.util.Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(JwtUtil.getKey())
                .compact();
        
        boolean isValid = JwtUtil.validateToken(expiredToken, false);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token dengan expired token dan ignoreExpired true mengembalikan true")
    void validateToken_WithExpiredTokenAndIgnoreExpiredTrue_ShouldReturnTrue() {
        // Create an expired token
        UUID userId = UUID.randomUUID();
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new java.util.Date(System.currentTimeMillis() - 100000))
                .expiration(new java.util.Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(JwtUtil.getKey())
                .compact();
        
        boolean isValid = JwtUtil.validateToken(expiredToken, true);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("GetKey mengembalikan SecretKey")
    void getKey_ShouldReturnSecretKey() {
        javax.crypto.SecretKey key = JwtUtil.getKey();
        
        assertNotNull(key);
    }
}

