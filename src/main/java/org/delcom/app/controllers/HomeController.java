package org.delcom.app.controllers;

import jakarta.servlet.http.Cookie;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {
    private final ProductService productService;
    private final AuthService authService;

    public HomeController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String home(@CookieValue(value = "token", required = false) String token,
                      Model model) {
        // Token sudah divalidasi oleh WebAuthInterceptor, jadi pasti valid
        if (token != null) {
            Optional<User> userOpt = authService.getUserByToken(token);
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
        
        model.addAttribute("products", productService.getAllProducts());
        return "index";
    }
}
