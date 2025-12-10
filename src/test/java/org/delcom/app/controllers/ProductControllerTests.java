package org.delcom.app.controllers;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.entities.Product;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthService;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductControllerTests {
    private ProductService productService;
    private AuthService authService;
    private FileStorageService fileStorageService;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        authService = mock(AuthService.class);
        fileStorageService = mock(FileStorageService.class);
        productController = new ProductController(productService, authService, fileStorageService);
    }

    @Test
    @DisplayName("List products mengembalikan view name")
    void listProducts_ShouldReturnViewName() {
        Model model = mock(Model.class);
        List<Product> products = new ArrayList<>();
        when(productService.getAllProducts()).thenReturn(products);

        String result = productController.listProducts(null, model);

        assertEquals("products/list", result);
        verify(model, times(1)).addAttribute(eq("products"), eq(products));
    }

    @Test
    @DisplayName("My products tanpa token redirect ke login")
    void myProducts_WithoutToken_ShouldRedirectToLogin() {
        Model model = mock(Model.class);
        String result = productController.myProducts(null, model);
        assertEquals("redirect:/auth/login", result);
    }

    @Test
    @DisplayName("My products dengan token valid mengembalikan view")
    void myProducts_WithValidToken_ShouldReturnView() {
        Model model = mock(Model.class);
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductsByUserId(user.getId())).thenReturn(new ArrayList<>());

        String result = productController.myProducts(token, model);

        assertEquals("products/my-products", result);
        verify(model, times(1)).addAttribute("currentUser", user);
    }

    @Test
    @DisplayName("Product detail mengembalikan view name")
    void productDetail_ShouldReturnViewName() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);

        when(productService.getProductById(productId)).thenReturn(Optional.of(product));

        String result = productController.productDetail(productId, model, null);

        assertEquals("products/detail", result);
        verify(model, times(1)).addAttribute("product", product);
    }

    @Test
    @DisplayName("Show add form tanpa token redirect ke login")
    void showAddForm_WithoutToken_ShouldRedirectToLogin() {
        Model model = mock(Model.class);
        String result = productController.showAddForm(null, model);
        assertEquals("redirect:/auth/login", result);
    }

    @Test
    @DisplayName("Add product tanpa token mengembalikan error")
    void addProduct_WithoutToken_ShouldReturnError() {
        ApiResponse<Product> result = productController.addProduct(null, "Name", "Desc", 
            new BigDecimal("100000"), "Category", "New", null);

        assertEquals("error", result.getStatus());
    }

    @Test
    @DisplayName("Add product dengan token valid berhasil")
    void addProduct_WithValidToken_ShouldReturnSuccess() {
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());
        Product product = new Product();
        product.setId(UUID.randomUUID());

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.createProduct(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(product);

        ApiResponse<Product> result = productController.addProduct(token, "Name", "Desc",
            new BigDecimal("100000"), "Category", "New", null);

        assertEquals("success", result.getStatus());
    }

    @Test
    @DisplayName("Show edit form tanpa token redirect ke login")
    void showEditForm_WithoutToken_ShouldRedirectToLogin() {
        Model model = mock(Model.class);
        String result = productController.showEditForm(UUID.randomUUID(), null, model);
        assertEquals("redirect:/auth/login", result);
    }

    @Test
    @DisplayName("Update product tanpa token mengembalikan error")
    void updateProduct_WithoutToken_ShouldReturnError() {
        ApiResponse<Product> result = productController.updateProduct(UUID.randomUUID(), null,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", null);

        assertEquals("error", result.getStatus());
    }

    @Test
    @DisplayName("Delete product tanpa token mengembalikan error")
    void deleteProduct_WithoutToken_ShouldReturnError() {
        ApiResponse<String> result = productController.deleteProduct(UUID.randomUUID(), null);
        assertEquals("error", result.getStatus());
    }

    @Test
    @DisplayName("Delete product dengan token valid berhasil")
    void deleteProduct_WithValidToken_ShouldReturnSuccess() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productService).deleteProduct(productId, userId);

        ApiResponse<String> result = productController.deleteProduct(productId, token);

        assertEquals("success", result.getStatus());
    }

    @Test
    @DisplayName("Update product dengan token valid berhasil")
    void updateProduct_WithValidToken_ShouldReturnSuccess() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(productService.updateProduct(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(product);

        ApiResponse<Product> result = productController.updateProduct(productId, token,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", null);

        assertEquals("success", result.getStatus());
    }

    @Test
    @DisplayName("Show edit form dengan token valid mengembalikan view")
    void showEditForm_WithValidToken_ShouldReturnView() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));

        String result = productController.showEditForm(productId, token, model);

        assertEquals("products/edit", result);
        verify(model, times(1)).addAttribute("product", product);
    }

    @Test
    @DisplayName("Product detail dengan token dan owner mengembalikan view dengan isOwner")
    void productDetail_WithTokenAndOwner_ShouldReturnViewWithIsOwner() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));

        String result = productController.productDetail(productId, model, token);

        assertEquals("products/detail", result);
        verify(model, times(1)).addAttribute("isOwner", true);
    }

    @Test
    @DisplayName("List products dengan token mengembalikan view dengan user")
    void listProducts_WithToken_ShouldReturnViewWithUser() {
        Model model = mock(Model.class);
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());

        when(productService.getAllProducts()).thenReturn(new ArrayList<>());
        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));

        String result = productController.listProducts(token, model);

        assertEquals("products/list", result);
        verify(model, times(1)).addAttribute("currentUser", user);
    }

    @Test
    @DisplayName("My products dengan token invalid redirect ke login")
    void myProducts_WithInvalidToken_ShouldRedirectToLogin() {
        Model model = mock(Model.class);
        String token = "invalid-token";
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        String result = productController.myProducts(token, model);
        assertEquals("redirect:/auth/login", result);
    }

    @Test
    @DisplayName("Show add form dengan token valid mengembalikan view")
    void showAddForm_WithValidToken_ShouldReturnView() {
        Model model = mock(Model.class);
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));

        String result = productController.showAddForm(token, model);

        assertEquals("products/add", result);
        verify(model, times(1)).addAttribute("currentUser", user);
    }

    @Test
    @DisplayName("Show add form dengan token invalid redirect ke login")
    void showAddForm_WithInvalidToken_ShouldRedirectToLogin() {
        Model model = mock(Model.class);
        String token = "invalid-token";
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        String result = productController.showAddForm(token, model);
        assertEquals("redirect:/auth/login", result);
    }

    @Test
    @DisplayName("Add product dengan image empty tidak menyimpan image")
    void addProduct_WithEmptyImage_ShouldReturnSuccessWithoutImage() {
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());
        Product product = new Product();
        product.setId(UUID.randomUUID());
        MultipartFile emptyImage = mock(MultipartFile.class);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(emptyImage.isEmpty()).thenReturn(true);
        when(productService.createProduct(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(product);

        ApiResponse<Product> result = productController.addProduct(token, "Name", "Desc",
            new BigDecimal("100000"), "Category", "New", emptyImage);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, never()).storeFile(any());
        verify(productService, times(1)).createProduct(any(), any(), any(), any(), any(), any(), eq((String) null));
    }

    @Test
    @DisplayName("Add product dengan image berhasil")
    void addProduct_WithImage_ShouldReturnSuccess() {
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());
        Product product = new Product();
        product.setId(UUID.randomUUID());
        MultipartFile image = mock(MultipartFile.class);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(image.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(image)).thenReturn("/uploads/test.jpg");
        when(productService.createProduct(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(product);

        ApiResponse<Product> result = productController.addProduct(token, "Name", "Desc",
            new BigDecimal("100000"), "Category", "New", image);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, times(1)).storeFile(image);
    }

    @Test
    @DisplayName("Add product dengan exception mengembalikan error")
    void addProduct_WithException_ShouldReturnError() {
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.createProduct(any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Error creating product"));

        ApiResponse<Product> result = productController.addProduct(token, "Name", "Desc",
            new BigDecimal("100000"), "Category", "New", null);

        assertEquals("error", result.getStatus());
        assertEquals("Error creating product", result.getMessage());
    }

    @Test
    @DisplayName("Add product dengan token invalid mengembalikan error")
    void addProduct_WithInvalidToken_ShouldReturnError() {
        String token = "invalid-token";
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        ApiResponse<Product> result = productController.addProduct(token, "Name", "Desc",
            new BigDecimal("100000"), "Category", "New", null);

        assertEquals("error", result.getStatus());
        assertEquals("Token tidak valid", result.getMessage());
    }

    @Test
    @DisplayName("Show edit form dengan product tidak ditemukan redirect ke products")
    void showEditForm_ProductNotFound_ShouldRedirectToProducts() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(UUID.randomUUID());

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        String result = productController.showEditForm(productId, token, model);
        assertEquals("redirect:/products", result);
    }

    @Test
    @DisplayName("Show edit form dengan user bukan owner redirect ke products")
    void showEditForm_UserNotOwner_ShouldRedirectToProducts() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(ownerId);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));

        String result = productController.showEditForm(productId, token, model);
        assertEquals("redirect:/products", result);
    }

    @Test
    @DisplayName("Show edit form dengan token invalid redirect ke login")
    void showEditForm_WithInvalidToken_ShouldRedirectToLogin() {
        Model model = mock(Model.class);
        String token = "invalid-token";
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        String result = productController.showEditForm(UUID.randomUUID(), token, model);
        assertEquals("redirect:/auth/login", result);
    }

    @Test
    @DisplayName("Update product dengan image berhasil")
    void updateProduct_WithImage_ShouldReturnSuccess() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product oldProduct = new Product();
        oldProduct.setId(productId);
        oldProduct.setUserId(userId);
        oldProduct.setImageUrl("/uploads/old.jpg");
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setUserId(userId);
        MultipartFile image = mock(MultipartFile.class);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(oldProduct));
        when(image.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(image)).thenReturn("/uploads/new.jpg");
        when(productService.updateProduct(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(updatedProduct);

        ApiResponse<Product> result = productController.updateProduct(productId, token,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", image);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, times(1)).deleteFile("/uploads/old.jpg");
        verify(fileStorageService, times(1)).storeFile(image);
    }

    @Test
    @DisplayName("Update product dengan image empty tidak menyimpan image")
    void updateProduct_WithEmptyImage_ShouldReturnSuccessWithoutImage() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);
        MultipartFile emptyImage = mock(MultipartFile.class);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(emptyImage.isEmpty()).thenReturn(true);
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(productService.updateProduct(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(product);

        ApiResponse<Product> result = productController.updateProduct(productId, token,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", emptyImage);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, never()).storeFile(any());
        verify(fileStorageService, never()).deleteFile(any());
        verify(productService, times(1)).updateProduct(any(), any(), any(), any(), any(), any(), any(), eq((String) null));
    }

    @Test
    @DisplayName("Update product dengan image tapi product imageUrl null")
    void updateProduct_WithImageButNullImageUrl_ShouldReturnSuccess() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product oldProduct = new Product();
        oldProduct.setId(productId);
        oldProduct.setUserId(userId);
        oldProduct.setImageUrl(null);
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setUserId(userId);
        MultipartFile image = mock(MultipartFile.class);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(oldProduct));
        when(image.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(image)).thenReturn("/uploads/new.jpg");
        when(productService.updateProduct(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(updatedProduct);

        ApiResponse<Product> result = productController.updateProduct(productId, token,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", image);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(fileStorageService, times(1)).storeFile(image);
    }

    @Test
    @DisplayName("Update product dengan image tapi old product tidak ditemukan")
    void updateProduct_WithImageButOldProductNotFound_ShouldReturnSuccess() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setUserId(userId);
        MultipartFile image = mock(MultipartFile.class);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.empty());
        when(image.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(image)).thenReturn("/uploads/new.jpg");
        when(productService.updateProduct(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(updatedProduct);

        ApiResponse<Product> result = productController.updateProduct(productId, token,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", image);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(fileStorageService, times(1)).storeFile(image);
    }

    @Test
    @DisplayName("Update product dengan exception mengembalikan error")
    void updateProduct_WithException_ShouldReturnError() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(productService.updateProduct(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Error updating product"));

        ApiResponse<Product> result = productController.updateProduct(productId, token,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", null);

        assertEquals("error", result.getStatus());
        assertEquals("Error updating product", result.getMessage());
    }

    @Test
    @DisplayName("Update product dengan token invalid mengembalikan error")
    void updateProduct_WithInvalidToken_ShouldReturnError() {
        String token = "invalid-token";
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        ApiResponse<Product> result = productController.updateProduct(UUID.randomUUID(), token,
            "Name", "Desc", new BigDecimal("100000"), "Category", "New", null);

        assertEquals("error", result.getStatus());
        assertEquals("Token tidak valid", result.getMessage());
    }

    @Test
    @DisplayName("Product detail dengan product tidak ditemukan redirect ke products")
    void productDetail_ProductNotFound_ShouldRedirectToProducts() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        String result = productController.productDetail(productId, model, null);
        assertEquals("redirect:/products", result);
    }

    @Test
    @DisplayName("Product detail dengan token tapi bukan owner")
    void productDetail_WithTokenButNotOwner_ShouldReturnViewWithIsOwnerFalse() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(ownerId);

        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));

        String result = productController.productDetail(productId, model, token);

        assertEquals("products/detail", result);
        verify(model, times(1)).addAttribute("isOwner", false);
    }

    @Test
    @DisplayName("Product detail dengan token invalid tidak menambahkan user")
    void productDetail_WithInvalidToken_ShouldNotAddUser() {
        Model model = mock(Model.class);
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        String token = "invalid-token";

        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        String result = productController.productDetail(productId, model, token);

        assertEquals("products/detail", result);
        verify(model, never()).addAttribute(eq("currentUser"), any());
        verify(model, never()).addAttribute(eq("isOwner"), anyBoolean());
    }

    @Test
    @DisplayName("Delete product dengan image berhasil")
    void deleteProduct_WithImage_ShouldReturnSuccess() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);
        product.setImageUrl("/uploads/test.jpg");

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productService).deleteProduct(productId, userId);

        ApiResponse<String> result = productController.deleteProduct(productId, token);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, times(1)).deleteFile("/uploads/test.jpg");
    }

    @Test
    @DisplayName("Delete product dengan exception mengembalikan error")
    void deleteProduct_WithException_ShouldReturnError() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        doThrow(new RuntimeException("Error deleting product"))
            .when(productService).deleteProduct(productId, userId);

        ApiResponse<String> result = productController.deleteProduct(productId, token);

        assertEquals("error", result.getStatus());
        assertEquals("Error deleting product", result.getMessage());
    }

    @Test
    @DisplayName("Delete product dengan token invalid mengembalikan error")
    void deleteProduct_WithInvalidToken_ShouldReturnError() {
        String token = "invalid-token";
        when(authService.getUserByToken(token)).thenReturn(Optional.empty());

        ApiResponse<String> result = productController.deleteProduct(UUID.randomUUID(), token);

        assertEquals("error", result.getStatus());
        assertEquals("Token tidak valid", result.getMessage());
    }

    @Test
    @DisplayName("Delete product tanpa image berhasil")
    void deleteProduct_WithoutImage_ShouldReturnSuccess() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "valid-token";
        User user = new User();
        user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);
        product.setImageUrl(null);

        when(authService.getUserByToken(token)).thenReturn(Optional.of(user));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productService).deleteProduct(productId, userId);

        ApiResponse<String> result = productController.deleteProduct(productId, token);

        assertEquals("success", result.getStatus());
        verify(fileStorageService, never()).deleteFile(anyString());
    }
}

