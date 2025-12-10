package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.AuthTokenRepository;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTests {
    private UserRepository userRepository;
    private AuthTokenRepository authTokenRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authTokenRepository = mock(AuthTokenRepository.class);
        authService = new AuthService(userRepository, authTokenRepository);
    }

    @Test
    @DisplayName("Register user baru berhasil")
    void register_WithNewEmail_ShouldReturnUser() {
        String name = "Test User";
        String email = "test@example.com";
        String password = "password123";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        User result = authService.register(name, email, password);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertNotNull(result.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register dengan email yang sudah ada throw exception")
    void register_WithExistingEmail_ShouldThrowException() {
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.register("Test", email, "password");
        });
    }

    @Test
    @DisplayName("Login dengan email tidak ditemukan throw exception")
    void login_WithNonExistentEmail_ShouldThrowException() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            authService.login(email, "password");
        });
    }

    @Test
    @DisplayName("Login dengan password salah throw exception")
    void login_WithWrongPassword_ShouldThrowException() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        // Password yang di-encode dengan BCrypt - ini akan selalu fail karena kita tidak bisa mock BCrypt
        // Tapi kita test bahwa method dipanggil dengan benar
        user.setPassword("$2a$10$invalidHashThatWillNeverMatch");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Karena BCryptPasswordEncoder real, password yang salah akan selalu throw exception
        assertThrows(RuntimeException.class, () -> {
            authService.login(email, "wrongpassword");
        });
    }

    @Test
    @DisplayName("Login dengan kredensial valid berhasil")
    void login_WithValidCredentials_ShouldReturnAuthToken() {
        String email = "test@example.com";
        String password = "password123";
        UUID userId = UUID.randomUUID();
        
        // Encode password dengan BCrypt yang real
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword(encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authTokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> {
            AuthToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        AuthToken result = authService.login(email, password);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertFalse(result.getToken().isEmpty());
        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    @DisplayName("Get user by token valid berhasil")
    void getUserByToken_WithValidToken_ShouldReturnUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        // Generate valid token
        String token = org.delcom.app.utils.JwtUtil.generateToken(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = authService.getUserByToken(token);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    @DisplayName("Get user by token invalid mengembalikan empty")
    void getUserByToken_WithInvalidToken_ShouldReturnEmpty() {
        String token = "invalid-token";

        Optional<User> result = authService.getUserByToken(token);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Logout berhasil menghapus token")
    void logout_ShouldDeleteToken() {
        String token = "test-token";
        authService.logout(token);
        verify(authTokenRepository, times(1)).deleteByToken(token);
    }

    @Test
    @DisplayName("Get user by ID berhasil")
    void getUserById_ShouldReturnUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = authService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    @DisplayName("Get user by ID dengan user tidak ditemukan mengembalikan empty")
    void getUserById_WithUserNotFound_ShouldReturnEmpty() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = authService.getUserById(userId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Login dengan existing token menghapus token lama")
    void login_WithExistingToken_ShouldDeleteOldToken() {
        String email = "test@example.com";
        String password = "password123";
        UUID userId = UUID.randomUUID();
        
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword(encodedPassword);

        AuthToken existingToken = new AuthToken();
        existingToken.setId(UUID.randomUUID());
        existingToken.setToken("old-token");
        existingToken.setUserId(userId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authTokenRepository.findByUserId(userId)).thenReturn(Optional.of(existingToken));
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> {
            AuthToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        AuthToken result = authService.login(email, password);

        assertNotNull(result);
        verify(authTokenRepository, times(1)).delete(existingToken);
        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    @DisplayName("Get user by token dengan user tidak ditemukan mengembalikan empty")
    void getUserByToken_WithUserNotFound_ShouldReturnEmpty() {
        UUID userId = UUID.randomUUID();
        String token = org.delcom.app.utils.JwtUtil.generateToken(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = authService.getUserByToken(token);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Get user by token dengan extractUserId mengembalikan null mengembalikan empty")
    void getUserByToken_WithExtractUserIdReturnsNull_ShouldReturnEmpty() {
        // Create a token with invalid subject format that will pass validateToken
        // but extractUserId will return null because subject is not a valid UUID
        String tokenWithInvalidSubject = io.jsonwebtoken.Jwts.builder()
                .subject("invalid-uuid-format") // Not a valid UUID
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(org.delcom.app.utils.JwtUtil.getKey())
                .compact();

        // validateToken will return true (token is valid JWT)
        // but extractUserId will return null (subject is not UUID format)
        Optional<User> result = authService.getUserByToken(tokenWithInvalidSubject);

        assertFalse(result.isPresent());
        // Should not call userRepository because extractUserId returns null
        verify(userRepository, never()).findById(any());
    }
}

