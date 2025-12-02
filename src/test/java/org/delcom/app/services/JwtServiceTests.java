package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    @DisplayName("Generate token berhasil")
    void generateToken_ShouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Get user ID from token berhasil")
    void getUserIdFromToken_ShouldReturnCorrectUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);
        
        UUID extractedUserId = jwtService.getUserIdFromToken(token);
        
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Validate token valid berhasil")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);
        
        boolean isValid = jwtService.validateToken(token);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validate token invalid mengembalikan false")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        boolean isValid = jwtService.validateToken("invalid-token");
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token kosong mengembalikan false")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        boolean isValid = jwtService.validateToken("");
        
        assertFalse(isValid);
    }
}

