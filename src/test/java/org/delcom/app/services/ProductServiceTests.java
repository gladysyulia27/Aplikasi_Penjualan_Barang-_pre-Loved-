package org.delcom.app.services;

import org.delcom.app.entities.Product;
import org.delcom.app.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTests {
    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("Get all products berhasil")
    void getAllProducts_ShouldReturnAllProducts() {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get products by user ID berhasil")
    void getProductsByUserId_ShouldReturnUserProducts() {
        UUID userId = UUID.randomUUID();
        List<Product> products = Arrays.asList(new Product());
        when(productRepository.findByUserId(userId)).thenReturn(products);

        List<Product> result = productService.getProductsByUserId(userId);

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Get products by category berhasil")
    void getProductsByCategory_ShouldReturnCategoryProducts() {
        String category = "Pakaian";
        List<Product> products = Arrays.asList(new Product());
        when(productRepository.findByCategory(category)).thenReturn(products);

        List<Product> result = productService.getProductsByCategory(category);

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByCategory(category);
    }

    @Test
    @DisplayName("Get product by ID berhasil")
    void getProductById_ShouldReturnProduct() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductById(productId);

        assertTrue(result.isPresent());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Create product berhasil")
    void createProduct_ShouldSaveAndReturnProduct() {
        UUID userId = UUID.randomUUID();
        String name = "Test Product";
        String description = "Description";
        BigDecimal price = new BigDecimal("100000");
        String category = "Pakaian";
        String condition = "New";
        String imageUrl = "/images/test.jpg";

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        Product result = productService.createProduct(userId, name, description, price, category, condition, imageUrl);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Update product berhasil")
    void updateProduct_WithValidProduct_ShouldUpdateProduct() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updateProduct(
            productId, userId, "New Name", "New Desc", 
            new BigDecimal("200000"), "Elektronik", "Like New", null
        );

        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Update product tidak ditemukan throw exception")
    void updateProduct_ProductNotFound_ShouldThrowException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(productId, UUID.randomUUID(), "Name", "Desc", 
                new BigDecimal("100000"), "Category", "New", null);
        });
    }

    @Test
    @DisplayName("Update product dengan user ID berbeda throw exception")
    void updateProduct_WithDifferentUserId_ShouldThrowException() {
        UUID productId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setUserId(ownerId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(productId, differentUserId, "Name", "Desc",
                new BigDecimal("100000"), "Category", "New", null);
        });
    }

    @Test
    @DisplayName("Update product dengan image URL baru berhasil")
    void updateProduct_WithNewImageUrl_ShouldUpdateImage() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updateProduct(
            productId, userId, "New Name", "New Desc",
            new BigDecimal("200000"), "Elektronik", "Like New", "/new-image.jpg"
        );

        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Update product dengan image URL empty string tidak update image")
    void updateProduct_WithEmptyImageUrl_ShouldNotUpdateImage() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);
        product.setImageUrl("/old-image.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updateProduct(
            productId, userId, "New Name", "New Desc",
            new BigDecimal("200000"), "Elektronik", "Like New", ""
        );

        assertNotNull(result);
        assertEquals("/old-image.jpg", product.getImageUrl()); // Image URL should not change
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete product dengan user ID berbeda throw exception")
    void deleteProduct_WithDifferentUserId_ShouldThrowException() {
        UUID productId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setUserId(ownerId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(productId, differentUserId);
        });
    }

    @Test
    @DisplayName("Delete product tidak ditemukan throw exception")
    void deleteProduct_ProductNotFound_ShouldThrowException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(productId, UUID.randomUUID());
        });
    }

    @Test
    @DisplayName("Delete product berhasil")
    void deleteProduct_WithValidProduct_ShouldDeleteProduct() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setUserId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        productService.deleteProduct(productId, userId);

        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    @DisplayName("Get category statistics berhasil")
    void getCategoryStatistics_ShouldReturnStatistics() {
        List<Object[]> stats = Arrays.asList(
            new Object[]{"Pakaian", 5L},
            new Object[]{"Elektronik", 3L}
        );
        when(productRepository.countByCategory()).thenReturn(stats);

        List<Object[]> result = productService.getCategoryStatistics();

        assertEquals(2, result.size());
        verify(productRepository, times(1)).countByCategory();
    }

    @Test
    @DisplayName("Get condition statistics berhasil")
    void getConditionStatistics_ShouldReturnStatistics() {
        List<Object[]> stats = Arrays.asList(
            new Object[]{"New", 2L},
            new Object[]{"Like New", 3L}
        );
        when(productRepository.countByCondition()).thenReturn(stats);

        List<Object[]> result = productService.getConditionStatistics();

        assertEquals(2, result.size());
        verify(productRepository, times(1)).countByCondition();
    }

    @Test
    @DisplayName("Count products by user ID berhasil")
    void countProductsByUserId_ShouldReturnCount() {
        UUID userId = UUID.randomUUID();
        when(productRepository.countByUserId(userId)).thenReturn(5L);

        Long result = productService.countProductsByUserId(userId);

        assertEquals(5L, result);
        verify(productRepository, times(1)).countByUserId(userId);
    }
}

