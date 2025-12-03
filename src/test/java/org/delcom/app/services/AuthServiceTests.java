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
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authTokenRepository = mock(AuthTokenRepository.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, authTokenRepository, jwtService);
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
        when(jwtService.generateToken(userId)).thenReturn("test-token");
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> {
            AuthToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        AuthToken result = authService.login(email, password);

        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        verify(authTokenRepository, times(1)).save(any(AuthToken.class));
    }

    @Test
    @DisplayName("Get user by token valid berhasil")
    void getUserByToken_WithValidToken_ShouldReturnUser() {
        String token = "valid-token";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = authService.getUserByToken(token);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    @DisplayName("Get user by token invalid mengembalikan empty")
    void getUserByToken_WithInvalidToken_ShouldReturnEmpty() {
        String token = "invalid-token";
        when(jwtService.validateToken(token)).thenReturn(false);

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
        when(jwtService.generateToken(userId)).thenReturn("new-token");
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
        String token = "valid-token";
        UUID userId = UUID.randomUUID();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUserIdFromToken(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = authService.getUserByToken(token);

        assertFalse(result.isPresent());
    }
}

