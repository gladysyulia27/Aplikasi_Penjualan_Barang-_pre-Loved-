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
    @DisplayName("Validate token dengan malformed token mengembalikan false")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Create a malformed token (not a valid JWT format)
        String malformedToken = "not.a.valid.jwt.token";
        
        boolean isValid = JwtUtil.validateToken(malformedToken, false);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token dengan signature exception mengembalikan false")
    void validateToken_WithSignatureException_ShouldReturnFalse() {
        // Create a token with different secret key (will cause signature exception)
        UUID userId = UUID.randomUUID();
        String tokenWithWrongKey = io.jsonwebtoken.Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("different-secret-key-that-will-cause-signature-exception".getBytes()))
                .compact();
        
        boolean isValid = JwtUtil.validateToken(tokenWithWrongKey, false);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token kosong mengembalikan false")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        boolean isValid = JwtUtil.validateToken("", false);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token null mengembalikan false")
    void validateToken_WithNullToken_ShouldReturnFalse() {
        boolean isValid = JwtUtil.validateToken(null, false);
        
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
    @DisplayName("Extract user ID dari token dengan subject bukan UUID format mengembalikan null")
    void extractUserId_WithTokenSubjectNotUUID_ShouldReturnNull() {
        // Create a token with invalid subject format (not a valid UUID)
        String tokenWithInvalidSubject = io.jsonwebtoken.Jwts.builder()
                .subject("invalid-uuid-format") // Not a valid UUID
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(JwtUtil.getKey())
                .compact();
        
        UUID extractedUserId = JwtUtil.extractUserId(tokenWithInvalidSubject);
        
        assertNull(extractedUserId);
    }

    @Test
    @DisplayName("Extract user ID dari expired token mengembalikan null")
    void extractUserId_WithExpiredToken_ShouldReturnNull() {
        // Create an expired token - will throw ExpiredJwtException which is caught by catch (Exception e)
        UUID userId = UUID.randomUUID();
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new java.util.Date(System.currentTimeMillis() - 100000))
                .expiration(new java.util.Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(JwtUtil.getKey())
                .compact();
        
        UUID extractedUserId = JwtUtil.extractUserId(expiredToken);
        
        assertNull(extractedUserId);
    }

    @Test
    @DisplayName("Extract user ID dari token dengan wrong signature mengembalikan null")
    void extractUserId_WithWrongSignature_ShouldReturnNull() {
        // Create a token with different secret key - will throw SignatureException which is caught by catch (Exception e)
        // Need at least 256 bits (32 bytes) for HMAC-SHA key
        UUID userId = UUID.randomUUID();
        String differentSecretKey = "different-secret-key-that-is-long-enough-for-hmac-sha256-algorithm-12345678901234567890";
        String tokenWithWrongKey = io.jsonwebtoken.Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(differentSecretKey.getBytes()))
                .compact();
        
        UUID extractedUserId = JwtUtil.extractUserId(tokenWithWrongKey);
        
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

    @Test
    @DisplayName("JwtUtil constructor dapat dipanggil")
    void jwtUtil_Constructor_ShouldBeCallable() {
        // Test default constructor untuk coverage
        JwtUtil jwtUtil = new JwtUtil();
        assertNotNull(jwtUtil);
    }
}

