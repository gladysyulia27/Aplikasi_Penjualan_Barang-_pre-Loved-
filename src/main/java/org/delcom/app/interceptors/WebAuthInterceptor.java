package org.delcom.app.interceptors;

import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@Component
public class WebAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Skip auth untuk endpoint public
        if (isPublicEndpoint(request)) {
            return true;
        }

        // Ambil token dari cookie
        Cookie[] cookies = request.getCookies();
        String token = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Jika tidak ada token, redirect ke login
        if (token == null || token.isEmpty()) {
            String requestUri = request.getRequestURI();
            // Hanya redirect jika bukan root path, untuk root path langsung ke login tanpa redirect param
            if (requestUri.equals("/") || requestUri.isEmpty()) {
                response.sendRedirect("/auth/login");
            } else {
                // Encode URL dengan benar
                String redirectParam = java.net.URLEncoder.encode(requestUri, "UTF-8");
                String redirectUrl = "/auth/login?redirect=" + redirectParam;
                response.sendRedirect(redirectUrl);
            }
            return false;
        }

        // Validasi token
        Optional<User> userOpt = authService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            // Token tidak valid, redirect ke login
            response.sendRedirect("/auth/login");
            return false;
        }

        // Token valid, lanjutkan
        return true;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Endpoint public yang tidak perlu auth
        return path.startsWith("/auth/") ||           // Login, register
               path.startsWith("/api/auth/") ||      // API auth
               path.startsWith("/static/") ||        // Static resources
               path.startsWith("/css/") ||           // CSS
               path.startsWith("/js/") ||            // JavaScript
               path.startsWith("/images/") ||        // Images
               path.startsWith("/uploads/") ||       // Uploaded files
               path.equals("/error") ||              // Error page
               path.startsWith("/_") ||              // Internal Spring paths
               path.equals("/favicon.ico");          // Favicon
    }
}

