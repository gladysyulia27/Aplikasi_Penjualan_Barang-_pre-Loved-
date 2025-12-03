package org.delcom.app.controllers;

import org.delcom.app.entities.Product;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomeControllerTests {
    @Test
    @DisplayName("Mengembalikan view name 'index' yang benar")
    void home_ShouldReturnIndexView() {
        // Arrange
        ProductService productService = mock(ProductService.class);
        AuthService authService = mock(AuthService.class);
        Model model = mock(Model.class);
        HomeController controller = new HomeController(productService, authService);
        
        List<Product> products = new ArrayList<>();
        when(productService.getAllProducts()).thenReturn(products);

        // Act
        String result = controller.home(null, model);

        // Assert
        assertEquals("index", result);
        verify(productService, times(1)).getAllProducts();
        verify(model, times(1)).addAttribute("products", products);
    }

    @Test
    @DisplayName("Mengembalikan view name 'index' dengan user yang login")
    void home_WithToken_ShouldReturnIndexViewWithUser() {
        // Arrange
        ProductService productService = mock(ProductService.class);
        AuthService authService = mock(AuthService.class);
        Model model = mock(Model.class);
        HomeController controller = new HomeController(productService, authService);
        
        List<Product> products = new ArrayList<>();
        org.delcom.app.entities.User user = new org.delcom.app.entities.User();
        user.setId(java.util.UUID.randomUUID());
        when(productService.getAllProducts()).thenReturn(products);
        when(authService.getUserByToken("valid-token")).thenReturn(Optional.of(user));

        // Act
        String result = controller.home("valid-token", model);

        // Assert
        assertEquals("index", result);
        verify(productService, times(1)).getAllProducts();
        verify(authService, times(1)).getUserByToken("valid-token");
        verify(model, times(1)).addAttribute("currentUser", user);
    }

    @Test
    @DisplayName("Mengembalikan view name 'index' dengan token invalid tidak menambahkan user")
    void home_WithInvalidToken_ShouldNotAddUser() {
        // Arrange
        ProductService productService = mock(ProductService.class);
        AuthService authService = mock(AuthService.class);
        Model model = mock(Model.class);
        HomeController controller = new HomeController(productService, authService);
        
        List<Product> products = new ArrayList<>();
        when(productService.getAllProducts()).thenReturn(products);
        when(authService.getUserByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        String result = controller.home("invalid-token", model);

        // Assert
        assertEquals("index", result);
        verify(productService, times(1)).getAllProducts();
        verify(authService, times(1)).getUserByToken("invalid-token");
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }
}
