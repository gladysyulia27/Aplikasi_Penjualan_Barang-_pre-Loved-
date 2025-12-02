package org.delcom.app.controllers;

import jakarta.servlet.http.Cookie;
import org.delcom.app.configs.ApiResponse;
import org.delcom.app.entities.Product;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final AuthService authService;
    private final FileStorageService fileStorageService;

    public ProductController(ProductService productService, AuthService authService,
                            FileStorageService fileStorageService) {
        this.productService = productService;
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String listProducts(@CookieValue(value = "token", required = false) String token,
                              Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        
        if (token != null) {
            Optional<User> userOpt = authService.getUserByToken(token);
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
        
        return "products/list";
    }

    @GetMapping("/my-products")
    public String myProducts(@CookieValue(value = "token", required = false) String token,
                            Model model) {
        if (token == null) {
            return "redirect:/auth/login";
        }

        Optional<User> userOpt = authService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = userOpt.get();
        List<Product> products = productService.getProductsByUserId(user.getId());
        model.addAttribute("products", products);
        model.addAttribute("currentUser", user);
        return "products/my-products";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable UUID id, Model model,
                               @CookieValue(value = "token", required = false) String token) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }

        Product product = productOpt.get();
        model.addAttribute("product", product);

        if (token != null) {
            Optional<User> userOpt = authService.getUserByToken(token);
            userOpt.ifPresent(user -> {
                model.addAttribute("currentUser", user);
                model.addAttribute("isOwner", product.getUserId().equals(user.getId()));
            });
        }

        return "products/detail";
    }

    @GetMapping("/add")
    public String showAddForm(@CookieValue(value = "token", required = false) String token,
                             Model model) {
        if (token == null) {
            return "redirect:/auth/login";
        }

        Optional<User> userOpt = authService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("currentUser", userOpt.get());
        return "products/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public ApiResponse<Product> addProduct(@CookieValue(value = "token", required = false) String token,
                                          @RequestParam String name,
                                          @RequestParam String description,
                                          @RequestParam BigDecimal price,
                                          @RequestParam String category,
                                          @RequestParam String condition,
                                          @RequestParam(required = false) MultipartFile image) {
        if (token == null) {
            return new ApiResponse<>("error", "Anda harus login terlebih dahulu", null);
        }

        Optional<User> userOpt = authService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("error", "Token tidak valid", null);
        }

        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }

            Product product = productService.createProduct(
                    userOpt.get().getId(), name, description, price, category, condition, imageUrl);
            return new ApiResponse<>("success", "Produk berhasil ditambahkan", product);
        } catch (Exception e) {
            return new ApiResponse<>("error", e.getMessage(), null);
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id,
                              @CookieValue(value = "token", required = false) String token,
                              Model model) {
        if (token == null) {
            return "redirect:/auth/login";
        }

        Optional<User> userOpt = authService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }

        Product product = productOpt.get();
        if (!product.getUserId().equals(userOpt.get().getId())) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("currentUser", userOpt.get());
        return "products/edit";
    }

    @PostMapping("/{id}/edit")
    @ResponseBody
    public ApiResponse<Product> updateProduct(@PathVariable UUID id,
                                             @CookieValue(value = "token", required = false) String token,
                                             @RequestParam String name,
                                             @RequestParam String description,
                                             @RequestParam BigDecimal price,
                                             @RequestParam String category,
                                             @RequestParam String condition,
                                             @RequestParam(required = false) MultipartFile image) {
        if (token == null) {
            return new ApiResponse<>("error", "Anda harus login terlebih dahulu", null);
        }

        Optional<User> userOpt = authService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("error", "Token tidak valid", null);
        }

        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                // Delete old image
                Optional<Product> oldProductOpt = productService.getProductById(id);
                oldProductOpt.ifPresent(product -> {
                    if (product.getImageUrl() != null) {
                        fileStorageService.deleteFile(product.getImageUrl());
                    }
                });
                imageUrl = fileStorageService.storeFile(image);
            }

            Product product = productService.updateProduct(
                    id, userOpt.get().getId(), name, description, price, category, condition, imageUrl);
            return new ApiResponse<>("success", "Produk berhasil diubah", product);
        } catch (Exception e) {
            return new ApiResponse<>("error", e.getMessage(), null);
        }
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public ApiResponse<String> deleteProduct(@PathVariable UUID id,
                                            @CookieValue(value = "token", required = false) String token) {
        if (token == null) {
            return new ApiResponse<>("error", "Anda harus login terlebih dahulu", null);
        }

        Optional<User> userOpt = authService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("error", "Token tidak valid", null);
        }

        try {
            // Delete image file
            Optional<Product> productOpt = productService.getProductById(id);
            productOpt.ifPresent(product -> {
                if (product.getImageUrl() != null) {
                    fileStorageService.deleteFile(product.getImageUrl());
                }
            });

            productService.deleteProduct(id, userOpt.get().getId());
            return new ApiResponse<>("success", "Produk berhasil dihapus", null);
        } catch (Exception e) {
            return new ApiResponse<>("error", e.getMessage(), null);
        }
    }
}

