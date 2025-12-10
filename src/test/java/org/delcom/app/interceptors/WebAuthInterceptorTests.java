package org.delcom.app.interceptors;

import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.Cookie;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebAuthInterceptorTests {
    private WebAuthInterceptor webAuthInterceptor;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        webAuthInterceptor = new WebAuthInterceptor();
        
        // Use reflection to set private field
        try {
            java.lang.reflect.Field field = WebAuthInterceptor.class.getDeclaredField("authService");
            field.setAccessible(true);
            field.set(webAuthInterceptor, authService);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /auth/login mengembalikan true")
    void preHandle_WithAuthLoginEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /api/auth/login mengembalikan true")
    void preHandle_WithApiAuthLoginEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /static/test.css mengembalikan true")
    void preHandle_WithStaticEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/static/test.css");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /css/style.css mengembalikan true")
    void preHandle_WithCssEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/css/style.css");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /js/app.js mengembalikan true")
    void preHandle_WithJsEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/js/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /images/logo.png mengembalikan true")
    void preHandle_WithImagesEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/images/logo.png");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /uploads/images/test.jpg mengembalikan true")
    void preHandle_WithUploadsEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/uploads/images/test.jpg");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /error mengembalikan true")
    void preHandle_WithErrorEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /_internal mengembalikan true")
    void preHandle_WithInternalPath_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/_internal");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan public endpoint /favicon.ico mengembalikan true")
    void preHandle_WithFaviconEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/favicon.ico");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle tanpa cookies mengembalikan false dan redirect")
    void preHandle_WithoutCookies_ShouldReturnFalseAndRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // No cookies set (defaults to null)

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals("/auth/login?redirect=%2Fproducts", response.getRedirectedUrl());
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan root path tanpa token redirect ke login tanpa redirect param")
    void preHandle_WithRootPathWithoutToken_ShouldRedirectToLoginWithoutRedirectParam() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // No cookies set (defaults to null)

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals("/auth/login", response.getRedirectedUrl());
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan empty request URI tanpa token redirect ke login tanpa redirect param")
    void preHandle_WithEmptyRequestUriWithoutToken_ShouldRedirectToLoginWithoutRedirectParam() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // No cookies set (defaults to null)

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals("/auth/login", response.getRedirectedUrl());
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan cookies tapi tanpa token cookie mengembalikan false dan redirect")
    void preHandle_WithCookiesButNoTokenCookie_ShouldReturnFalseAndRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Cookie[] cookies = new Cookie[]{
            new Cookie("other", "value"),
            new Cookie("session", "session123")
        };
        request.setCookies(cookies);

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals("/auth/login?redirect=%2Fproducts", response.getRedirectedUrl());
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan token cookie kosong mengembalikan false dan redirect")
    void preHandle_WithEmptyTokenCookie_ShouldReturnFalseAndRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Cookie[] cookies = new Cookie[]{
            new Cookie("token", "")
        };
        request.setCookies(cookies);

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals("/auth/login?redirect=%2Fproducts", response.getRedirectedUrl());
        verify(authService, never()).getUserByToken(any());
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan token cookie invalid mengembalikan false dan redirect")
    void preHandle_WithInvalidToken_ShouldReturnFalseAndRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String invalidToken = "invalid-token";
        Cookie[] cookies = new Cookie[]{
            new Cookie("token", invalidToken)
        };
        request.setCookies(cookies);

        when(authService.getUserByToken(invalidToken)).thenReturn(Optional.empty());

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals("/auth/login", response.getRedirectedUrl());
        verify(authService, times(1)).getUserByToken(invalidToken);
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan token cookie valid mengembalikan true")
    void preHandle_WithValidToken_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String validToken = "valid-token";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        Cookie[] cookies = new Cookie[]{
            new Cookie("token", validToken)
        };
        request.setCookies(cookies);

        when(authService.getUserByToken(validToken)).thenReturn(Optional.of(user));

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, times(1)).getUserByToken(validToken);
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan multiple cookies dan token di tengah mengembalikan true")
    void preHandle_WithMultipleCookiesAndTokenInMiddle_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String validToken = "valid-token";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        Cookie[] cookies = new Cookie[]{
            new Cookie("session", "session123"),
            new Cookie("token", validToken),
            new Cookie("other", "value")
        };
        request.setCookies(cookies);

        when(authService.getUserByToken(validToken)).thenReturn(Optional.of(user));

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authService, times(1)).getUserByToken(validToken);
    }

    @Test
    @DisplayName("WebAuthInterceptor preHandle dengan request URI yang perlu di-encode mengembalikan false dan redirect dengan encoded URL")
    void preHandle_WithRequestUriNeedingEncoding_ShouldReturnFalseAndRedirectWithEncodedUrl() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products?category=electronics&sort=price");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // No cookies set (defaults to null)

        boolean result = webAuthInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertTrue(response.getRedirectedUrl().contains("/auth/login?redirect="));
        assertTrue(response.getRedirectedUrl().contains("%2Fproducts"));
        verify(authService, never()).getUserByToken(any());
    }
}

