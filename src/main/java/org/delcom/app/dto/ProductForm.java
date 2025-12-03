package org.delcom.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductForm {

    private UUID id;

    @NotBlank(message = "Nama produk harus diisi")
    private String name;

    @NotBlank(message = "Deskripsi harus diisi")
    private String description;

    @NotNull(message = "Harga harus diisi")
    @Positive(message = "Harga harus lebih dari 0")
    private BigDecimal price;

    @NotBlank(message = "Kategori harus diisi")
    private String category;

    @NotBlank(message = "Kondisi harus diisi")
    private String condition;

    private String imageUrl;

    // Constructor
    public ProductForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

