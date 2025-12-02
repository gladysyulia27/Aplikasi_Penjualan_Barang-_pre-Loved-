package org.delcom.app.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.app.configs.ApiResponse;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTests {
    private AuthService authService;
    private AuthController authController;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("Show register form mengembalikan view name")
    void showRegisterForm_ShouldReturnViewName() {
        Model model = mock(Model.class);
        String result = authController.showRegisterForm(model);
        assertEquals("auth/register", result);
    }

    @Test
    @DisplayName("Register berhasil mengembalikan success response")
    void register_WithValidData_ShouldReturnSuccess() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");

        when(authService.register("Test User", "test@example.com", "password123"))
            .thenReturn(user);

        ApiResponse<User> result = authController.register("Test User", "test@example.com", "password123");

        assertEquals("success", result.getStatus());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("Register dengan email sudah ada mengembalikan error response")
    void register_WithExistingEmail_ShouldReturnError() {
        when(authService.register(any(), any(), any()))
            .thenThrow(new RuntimeException("Email sudah terdaftar"));

        ApiResponse<User> result = authController.register("Test", "existing@example.com", "password");

        assertEquals("error", result.getStatus());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Show login form mengembalikan view name")
    void showLoginForm_ShouldReturnViewName() {
        String result = authController.showLoginForm();
        assertEquals("auth/login", result);
    }

    @Test
    @DisplayName("Login berhasil mengembalikan success response")
    void login_WithValidCredentials_ShouldReturnSuccess() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        ApiResponse<String> result = authController.login("test@example.com", "password123", response);

        assertEquals("success", result.getStatus());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan kredensial salah mengembalikan error response")
    void login_WithInvalidCredentials_ShouldReturnError() {
        when(authService.login(any(), any()))
            .thenThrow(new RuntimeException("Email atau password salah"));

        ApiResponse<String> result = authController.login("test@example.com", "wrong", response);

        assertEquals("error", result.getStatus());
    }

    @Test
    @DisplayName("Logout berhasil mengembalikan success response")
    void logout_ShouldReturnSuccess() {
        ApiResponse<String> result = authController.logout("test-token", response);

        assertEquals("success", result.getStatus());
        verify(authService, times(1)).logout("test-token");
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Logout tanpa token tetap berhasil")
    void logout_WithoutToken_ShouldReturnSuccess() {
        ApiResponse<String> result = authController.logout(null, response);

        assertEquals("success", result.getStatus());
        verify(authService, never()).logout(any());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }
}

