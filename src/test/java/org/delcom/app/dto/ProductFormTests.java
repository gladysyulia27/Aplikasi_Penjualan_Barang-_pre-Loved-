package org.delcom.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductFormTests {

    @Test
    @DisplayName("ProductForm constructor berhasil membuat instance")
    void constructor_ShouldCreateInstance() {
        ProductForm form = new ProductForm();
        assertNotNull(form);
    }

    @Test
    @DisplayName("ProductForm getter dan setter id berhasil")
    void id_GetterAndSetter_ShouldWork() {
        ProductForm form = new ProductForm();
        UUID id = UUID.randomUUID();
        form.setId(id);
        assertEquals(id, form.getId());
    }

    @Test
    @DisplayName("ProductForm getter dan setter name berhasil")
    void name_GetterAndSetter_ShouldWork() {
        ProductForm form = new ProductForm();
        form.setName("Test Product");
        assertEquals("Test Product", form.getName());
    }

    @Test
    @DisplayName("ProductForm getter dan setter description berhasil")
    void description_GetterAndSetter_ShouldWork() {
        ProductForm form = new ProductForm();
        form.setDescription("Test Description");
        assertEquals("Test Description", form.getDescription());
    }

    @Test
    @DisplayName("ProductForm getter dan setter price berhasil")
    void price_GetterAndSetter_ShouldWork() {
        ProductForm form = new ProductForm();
        BigDecimal price = new BigDecimal("100000");
        form.setPrice(price);
        assertEquals(price, form.getPrice());
    }

    @Test
    @DisplayName("ProductForm getter dan setter category berhasil")
    void category_GetterAndSetter_ShouldWork() {
        ProductForm form = new ProductForm();
        form.setCategory("Electronics");
        assertEquals("Electronics", form.getCategory());
    }

    @Test
    @DisplayName("ProductForm getter dan setter condition berhasil")
    void condition_GetterAndSetter_ShouldWork() {
        ProductForm form = new ProductForm();
        form.setCondition("Baru");
        assertEquals("Baru", form.getCondition());
    }

    @Test
    @DisplayName("ProductForm getter dan setter imageUrl berhasil")
    void imageUrl_GetterAndSetter_ShouldWork() {
        ProductForm form = new ProductForm();
        form.setImageUrl("/uploads/images/test.jpg");
        assertEquals("/uploads/images/test.jpg", form.getImageUrl());
    }
}

