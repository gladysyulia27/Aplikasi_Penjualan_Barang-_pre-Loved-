package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.AuthTokenRepository;
import org.delcom.app.repositories.UserRepository;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthInterceptorTests {
    private AuthInterceptor authInterceptor;
    private AuthContext authContext;
    private AuthTokenRepository authTokenRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        authContext = mock(AuthContext.class);
        authTokenRepository = mock(AuthTokenRepository.class);
        userRepository = mock(UserRepository.class);
        
        authInterceptor = new AuthInterceptor();
        
        // Use reflection to set private fields
        try {
            java.lang.reflect.Field field1 = AuthInterceptor.class.getDeclaredField("authContext");
            field1.setAccessible(true);
            field1.set(authInterceptor, authContext);
            
            java.lang.reflect.Field field2 = AuthInterceptor.class.getDeclaredField("authTokenRepository");
            field2.setAccessible(true);
            field2.set(authInterceptor, authTokenRepository);
            
            java.lang.reflect.Field field3 = AuthInterceptor.class.getDeclaredField("userRepository");
            field3.setAccessible(true);
            field3.set(authInterceptor, userRepository);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan public endpoint mengembalikan true")
    void preHandle_WithPublicEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authContext, never()).setAuthUser(any());
    }

    @Test
    @DisplayName("AuthInterceptor preHandle tanpa token mengembalikan false")
    void preHandle_WithoutToken_ShouldReturnFalse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan token valid berhasil")
    void preHandle_WithValidToken_ShouldReturnTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        User user = new User();
        user.setId(userId);
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUserId(userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(authToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        boolean result = authInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authContext, times(1)).setAuthUser(user);
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan token invalid mengembalikan false")
    void preHandle_WithInvalidToken_ShouldReturnFalse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi tidak valid"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan endpoint /error mengembalikan true")
    void preHandle_WithErrorEndpoint_ShouldReturnTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(authContext, never()).setAuthUser(any());
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan authToken tidak ditemukan mengembalikan false")
    void preHandle_WithTokenNotFoundInDatabase_ShouldReturnFalse() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi sudah expired"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan authToken userId tidak match mengembalikan false")
    void preHandle_WithTokenUserIdMismatch_ShouldReturnFalse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUserId(differentUserId); // Different userId

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(authToken));

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi sudah expired"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan user tidak ditemukan mengembalikan false")
    void preHandle_WithUserNotFound_ShouldReturnFalse() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUserId(userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(authToken));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(404, response.getStatus());
        assertTrue(response.getContentAsString().contains("User tidak ditemukan"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan Authorization header tanpa Bearer mengembalikan false")
    void preHandle_WithAuthorizationWithoutBearer_ShouldReturnFalse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "some-token-without-bearer");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan Authorization header kosong mengembalikan false")
    void preHandle_WithEmptyAuthorizationHeader_ShouldReturnFalse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan Authorization header null mengembalikan false")
    void preHandle_WithNullAuthorizationHeader_ShouldReturnFalse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        // Don't set Authorization header at all (null)
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan Bearer token kosong mengembalikan false")
    void preHandle_WithBearerEmptyToken_ShouldReturnFalse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer "); // Bearer dengan space tapi tanpa token
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    @DisplayName("AuthInterceptor preHandle dengan token yang extractUserId mengembalikan null")
    void preHandle_WithTokenExtractUserIdReturnsNull_ShouldReturnFalse() throws Exception {
        // Create a token that will pass validateToken but extractUserId returns null
        // We can use a token with invalid subject format
        String tokenWithInvalidSubject = io.jsonwebtoken.Jwts.builder()
                .subject("invalid-uuid-format") // Not a valid UUID
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(org.delcom.app.utils.JwtUtil.getKey())
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer " + tokenWithInvalidSubject);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // The token will pass validateToken but extractUserId will return null
        // because UUID.fromString("invalid-uuid-format") will throw exception
        boolean result = authInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Format token autentikasi tidak valid"));
    }
}

