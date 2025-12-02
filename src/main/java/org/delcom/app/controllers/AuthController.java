package org.delcom.app.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.app.configs.ApiResponse;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    @ResponseBody
    public ApiResponse<User> register(@RequestParam String name,
                                     @RequestParam String email,
                                     @RequestParam String password) {
        try {
            User user = authService.register(name, email, password);
            return new ApiResponse<>("success", "Registrasi berhasil", user);
        } catch (Exception e) {
            return new ApiResponse<>("error", e.getMessage(), null);
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ApiResponse<String> login(@RequestParam String email,
                                     @RequestParam String password,
                                     HttpServletResponse response) {
        try {
            AuthToken authToken = authService.login(email, password);
            Cookie cookie = new Cookie("token", authToken.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 24 hours
            response.addCookie(cookie);
            return new ApiResponse<>("success", "Login berhasil", authToken.getToken());
        } catch (Exception e) {
            return new ApiResponse<>("error", e.getMessage(), null);
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public ApiResponse<String> logout(@CookieValue(value = "token", required = false) String token,
                                     HttpServletResponse response) {
        if (token != null) {
            authService.logout(token);
        }
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return new ApiResponse<>("success", "Logout berhasil", null);
    }
}

