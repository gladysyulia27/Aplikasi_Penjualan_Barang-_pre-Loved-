package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTests {
    @Test
    @DisplayName("Product constructor tanpa parameter berhasil")
    void product_DefaultConstructor_ShouldCreateProduct() {
        Product product = new Product();
        assertNotNull(product);
    }

    @Test
    @DisplayName("Product constructor dengan parameter berhasil")
    void product_ConstructorWithParams_ShouldCreateProduct() {
        UUID userId = UUID.randomUUID();
        Product product = new Product(userId, "Test Product", "Description", 
            new BigDecimal("100000"), "Pakaian", "New", "/images/test.jpg");
        
        assertEquals(userId, product.getUserId());
        assertEquals("Test Product", product.getName());
        assertEquals("Description", product.getDescription());
        assertEquals(new BigDecimal("100000"), product.getPrice());
        assertEquals("Pakaian", product.getCategory());
        assertEquals("New", product.getCondition());
        assertEquals("/images/test.jpg", product.getImageUrl());
    }

    @Test
    @DisplayName("Product getter dan setter berfungsi")
    void product_GettersAndSetters_ShouldWork() {
        Product product = new Product();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        product.setId(id);
        product.setUserId(userId);
        product.setName("Product");
        product.setDescription("Desc");
        product.setPrice(new BigDecimal("50000"));
        product.setCategory("Category");
        product.setCondition("Good");
        product.setImageUrl("/image.jpg");
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        assertEquals(id, product.getId());
        assertEquals(userId, product.getUserId());
        assertEquals("Product", product.getName());
        assertEquals("Desc", product.getDescription());
        assertEquals(new BigDecimal("50000"), product.getPrice());
        assertEquals("Category", product.getCategory());
        assertEquals("Good", product.getCondition());
        assertEquals("/image.jpg", product.getImageUrl());
        assertEquals(now, product.getCreatedAt());
        assertEquals(now, product.getUpdatedAt());
    }

    @Test
    @DisplayName("Product PrePersist callback mengatur createdAt dan updatedAt")
    void product_PrePersist_ShouldSetTimestamps() throws Exception {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(new BigDecimal("100000"));
        product.setCategory("Category");
        product.setCondition("New");

        // Call PrePersist callback using reflection
        java.lang.reflect.Method onCreate = Product.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(product);

        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
        // Timestamps should be very close (within 1 second) since they're set in the same method
        long diffSeconds = java.time.Duration.between(product.getCreatedAt(), product.getUpdatedAt()).getSeconds();
        assertTrue(Math.abs(diffSeconds) <= 1, 
                   "createdAt and updatedAt should be within 1 second, but difference was: " + diffSeconds);
    }

    @Test
    @DisplayName("Product PreUpdate callback mengatur updatedAt")
    void product_PreUpdate_ShouldSetUpdatedAt() throws Exception {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(new BigDecimal("100000"));
        product.setCategory("Category");
        product.setCondition("New");
        LocalDateTime oldTime = LocalDateTime.now().minusDays(1);
        product.setCreatedAt(oldTime);
        product.setUpdatedAt(oldTime);

        // Call PreUpdate callback using reflection
        java.lang.reflect.Method onUpdate = Product.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(product);

        assertNotNull(product.getUpdatedAt());
        assertTrue(product.getUpdatedAt().isAfter(oldTime));
        assertEquals(oldTime, product.getCreatedAt()); // createdAt should not change
    }
}

