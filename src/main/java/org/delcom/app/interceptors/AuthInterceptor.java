package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.AuthTokenRepository;
import org.delcom.app.repositories.UserRepository;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    protected AuthContext authContext;

    @Autowired
    protected AuthTokenRepository authTokenRepository;

    @Autowired
    protected UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Skip auth untuk endpoint public
        if (isPublicEndpoint(request)) {
            return true;
        }

        // Ambil bearer token dari header
        String rawAuthToken = request.getHeader("Authorization");
        String token = extractToken(rawAuthToken);

        // Validasi token
        if (token == null || token.isEmpty()) {
            sendErrorResponse(response, 401, "Token autentikasi tidak ditemukan");
            return false;
        }

        // Validasi format token JWT
        if (!JwtUtil.validateToken(token, true)) {
            sendErrorResponse(response, 401, "Token autentikasi tidak valid");
            return false;
        }

        // Ekstrak userId dari token
        UUID userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            sendErrorResponse(response, 401, "Format token autentikasi tidak valid");
            return false;
        }

        // Cari token di database
        AuthToken authToken = authTokenRepository.findByToken(token).orElse(null);
        if (authToken == null || !authToken.getUserId().equals(userId)) {
            sendErrorResponse(response, 401, "Token autentikasi sudah expired");
            return false;
        }

        // Ambil data user
        User authUser = userRepository.findById(userId).orElse(null);
        if (authUser == null) {
            sendErrorResponse(response, 404, "User tidak ditemukan");
            return false;
        }

        // Set user ke auth context
        authContext.setAuthUser(authUser);
        return true;
    }

    private String extractToken(String rawAuthToken) {
        if (rawAuthToken != null && rawAuthToken.startsWith("Bearer ")) {
            return rawAuthToken.substring(7); // hapus "Bearer "
        }
        return null;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        // String method = request.getMethod();

        // Endpoint public yang tidak perlu auth
        return path.startsWith("/api/auth") || path.equals("/error");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"status\":\"fail\",\"message\":\"%s\",\"data\":null}",
                message);
        response.getWriter().write(jsonResponse);
    }
}

