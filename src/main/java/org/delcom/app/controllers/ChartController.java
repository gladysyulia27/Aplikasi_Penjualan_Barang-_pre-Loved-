package org.delcom.app.controllers;

import jakarta.servlet.http.Cookie;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/charts")
public class ChartController {
    private final ProductService productService;
    private final AuthService authService;

    public ChartController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @GetMapping
    public String showCharts(@CookieValue(value = "token", required = false) String token,
                            Model model) {
        List<Object[]> categoryStats = productService.getCategoryStatistics();
        List<Object[]> conditionStats = productService.getConditionStatistics();

        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("conditionStats", conditionStats);

        if (token != null) {
            Optional<User> userOpt = authService.getUserByToken(token);
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }

        return "charts/index";
    }
}

