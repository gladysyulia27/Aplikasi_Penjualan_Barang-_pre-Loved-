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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        Model model = mock(Model.class);
        String result = authController.showLoginForm(null, null, model);
        assertEquals("auth/login", result);
    }

    @Test
    @DisplayName("Show login form dengan user sudah login redirect ke home")
    void showLoginForm_WithLoggedInUser_ShouldRedirect() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        String result = authController.showLoginForm(null, "valid-token", model);
        assertEquals("redirect:/", result);
    }

    @Test
    @DisplayName("Show login form dengan token tapi user tidak ditemukan")
    void showLoginForm_WithTokenButUserNotFound_ShouldReturnLoginForm() {
        Model model = mock(Model.class);
        
        when(authService.getUserByToken("invalid-token")).thenReturn(Optional.empty());
        
        String result = authController.showLoginForm(null, "invalid-token", model);
        assertEquals("auth/login", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect URL valid")
    void showLoginForm_WithValidRedirectUrl_ShouldRedirect() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        String encodedRedirect = java.net.URLEncoder.encode("/products", java.nio.charset.StandardCharsets.UTF_8);
        String result = authController.showLoginForm(encodedRedirect, "valid-token", model);
        assertEquals("redirect:/products", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect URL yang mengandung double slash")
    void showLoginForm_WithRedirectUrlContainingDoubleSlash_ShouldRedirectToHome() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        String encodedRedirect = java.net.URLEncoder.encode("//products", java.nio.charset.StandardCharsets.UTF_8);
        String result = authController.showLoginForm(encodedRedirect, "valid-token", model);
        assertEquals("redirect:/", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect URL yang mengandung path traversal")
    void showLoginForm_WithRedirectUrlContainingPathTraversal_ShouldRedirectToHome() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        String encodedRedirect = java.net.URLEncoder.encode("/products/..", java.nio.charset.StandardCharsets.UTF_8);
        String result = authController.showLoginForm(encodedRedirect, "valid-token", model);
        assertEquals("redirect:/", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect URL yang tidak dimulai dengan slash")
    void showLoginForm_WithRedirectUrlNotStartingWithSlash_ShouldRedirectToHome() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        String encodedRedirect = java.net.URLEncoder.encode("products", java.nio.charset.StandardCharsets.UTF_8);
        String result = authController.showLoginForm(encodedRedirect, "valid-token", model);
        assertEquals("redirect:/", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect URL root")
    void showLoginForm_WithRedirectUrlRoot_ShouldRedirectToHome() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        String result = authController.showLoginForm("/", "valid-token", model);
        assertEquals("redirect:/", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect URL yang menyebabkan exception saat decode")
    void showLoginForm_WithInvalidEncodedRedirect_ShouldRedirectToHome() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        // Invalid encoded string that will cause exception
        String invalidEncoded = "%E0%A4%A";
        String result = authController.showLoginForm(invalidEncoded, "valid-token", model);
        assertEquals("redirect:/", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect parameter empty string tidak menambahkan ke model")
    void showLoginForm_WithEmptyRedirectParameter_ShouldNotAddToModel() {
        Model model = mock(Model.class);
        
        String result = authController.showLoginForm("", null, model);
        
        assertEquals("auth/login", result);
        verify(model, never()).addAttribute(eq("redirectUrl"), any());
    }

    @Test
    @DisplayName("Show login form dengan redirect parameter empty string dan user sudah login redirect ke home")
    void showLoginForm_WithEmptyRedirectAndLoggedInUser_ShouldRedirectToHome() {
        Model model = mock(Model.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));
        
        String result = authController.showLoginForm("", "valid-token", model);
        assertEquals("redirect:/", result);
    }

    @Test
    @DisplayName("Show login form dengan redirect parameter untuk form")
    void showLoginForm_WithRedirectParameter_ShouldAddToModel() {
        Model model = mock(Model.class);
        String encodedRedirect = java.net.URLEncoder.encode("/products", java.nio.charset.StandardCharsets.UTF_8);
        
        String result = authController.showLoginForm(encodedRedirect, null, model);
        
        assertEquals("auth/login", result);
        verify(model, times(1)).addAttribute(eq("redirectUrl"), eq("/products"));
    }

    @Test
    @DisplayName("Show login form dengan redirect parameter yang menyebabkan exception")
    void showLoginForm_WithInvalidRedirectParameter_ShouldAddOriginalToModel() {
        Model model = mock(Model.class);
        String invalidEncoded = "%E0%A4%A";
        
        String result = authController.showLoginForm(invalidEncoded, null, model);
        
        assertEquals("auth/login", result);
        verify(model, times(1)).addAttribute(eq("redirectUrl"), eq(invalidEncoded));
    }

    @Test
    @DisplayName("Login berhasil mengembalikan success response")
    void login_WithValidCredentials_ShouldReturnSuccess() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        ApiResponse<String> result = authController.login("test@example.com", "password123", null, response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL valid")
    void login_WithValidRedirectUrl_ShouldReturnRedirectUrl() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        String encodedRedirect = java.net.URLEncoder.encode("/products", java.nio.charset.StandardCharsets.UTF_8);
        ApiResponse<String> result = authController.login("test@example.com", "password123", encodedRedirect, response);

        assertEquals("success", result.getStatus());
        assertEquals("/products", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL yang mengandung double slash")
    void login_WithRedirectUrlContainingDoubleSlash_ShouldReturnHome() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        String encodedRedirect = java.net.URLEncoder.encode("//products", java.nio.charset.StandardCharsets.UTF_8);
        ApiResponse<String> result = authController.login("test@example.com", "password123", encodedRedirect, response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL yang mengandung path traversal")
    void login_WithRedirectUrlContainingPathTraversal_ShouldReturnHome() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        String encodedRedirect = java.net.URLEncoder.encode("/products/..", java.nio.charset.StandardCharsets.UTF_8);
        ApiResponse<String> result = authController.login("test@example.com", "password123", encodedRedirect, response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL yang tidak dimulai dengan slash")
    void login_WithRedirectUrlNotStartingWithSlash_ShouldReturnHome() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        String encodedRedirect = java.net.URLEncoder.encode("products", java.nio.charset.StandardCharsets.UTF_8);
        ApiResponse<String> result = authController.login("test@example.com", "password123", encodedRedirect, response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL empty string")
    void login_WithEmptyRedirectUrl_ShouldReturnHome() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        ApiResponse<String> result = authController.login("test@example.com", "password123", "", response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL null string")
    void login_WithRedirectUrlNullString_ShouldReturnHome() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        ApiResponse<String> result = authController.login("test@example.com", "password123", "null", response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL root")
    void login_WithRedirectUrlRoot_ShouldReturnHome() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        ApiResponse<String> result = authController.login("test@example.com", "password123", "/", response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan redirect URL yang menyebabkan exception saat decode")
    void login_WithInvalidEncodedRedirect_ShouldReturnHome() {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        when(authService.login("test@example.com", "password123")).thenReturn(authToken);

        // Invalid encoded string that will cause exception
        String invalidEncoded = "%E0%A4%A";
        ApiResponse<String> result = authController.login("test@example.com", "password123", invalidEncoded, response);

        assertEquals("success", result.getStatus());
        assertEquals("/", result.getData());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Login dengan kredensial salah mengembalikan error response")
    void login_WithInvalidCredentials_ShouldReturnError() {
        when(authService.login(any(), any()))
            .thenThrow(new RuntimeException("Email atau password salah"));

        ApiResponse<String> result = authController.login("test@example.com", "wrong", null, response);

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

