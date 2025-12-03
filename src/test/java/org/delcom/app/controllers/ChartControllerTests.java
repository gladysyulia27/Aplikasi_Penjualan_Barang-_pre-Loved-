package org.delcom.app.controllers;

import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChartControllerTests {
    private ProductService productService;
    private AuthService authService;
    private ChartController chartController;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        authService = mock(AuthService.class);
        chartController = new ChartController(productService, authService);
    }

    @Test
    @DisplayName("Show charts mengembalikan view name")
    void showCharts_ShouldReturnViewName() {
        Model model = mock(Model.class);
        List<Object[]> categoryStats = new ArrayList<>();
        categoryStats.add(new Object[]{"Pakaian", 5L});
        List<Object[]> conditionStats = new ArrayList<>();
        conditionStats.add(new Object[]{"New", 2L});

        when(productService.getCategoryStatistics()).thenReturn(categoryStats);
        when(productService.getConditionStatistics()).thenReturn(conditionStats);

        String result = chartController.showCharts(null, model);

        assertEquals("charts/index", result);
        verify(model, times(1)).addAttribute("categoryStats", categoryStats);
        verify(model, times(1)).addAttribute("conditionStats", conditionStats);
    }

    @Test
    @DisplayName("Show charts dengan token mengembalikan view dengan user")
    void showCharts_WithToken_ShouldReturnViewWithUser() {
        Model model = mock(Model.class);
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());

        List<Object[]> categoryStats = new ArrayList<>();
        categoryStats.add(new Object[]{"Pakaian", 5L});
        List<Object[]> conditionStats = new ArrayList<>();
        conditionStats.add(new Object[]{"New", 2L});

        when(productService.getCategoryStatistics()).thenReturn(categoryStats);
        when(productService.getConditionStatistics()).thenReturn(conditionStats);
        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));

        String result = chartController.showCharts(token, model);

        assertEquals("charts/index", result);
        verify(model, times(1)).addAttribute("currentUser", user);
    }

    @Test
    @DisplayName("Show charts dengan token invalid tidak menambahkan user")
    void showCharts_WithInvalidToken_ShouldNotAddUser() {
        Model model = mock(Model.class);
        String token = "invalid-token";

        List<Object[]> categoryStats = new ArrayList<>();
        categoryStats.add(new Object[]{"Pakaian", 5L});
        List<Object[]> conditionStats = new ArrayList<>();
        conditionStats.add(new Object[]{"New", 2L});

        when(productService.getCategoryStatistics()).thenReturn(categoryStats);
        when(productService.getConditionStatistics()).thenReturn(conditionStats);
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        String result = chartController.showCharts(token, model);

        assertEquals("charts/index", result);
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }
}

