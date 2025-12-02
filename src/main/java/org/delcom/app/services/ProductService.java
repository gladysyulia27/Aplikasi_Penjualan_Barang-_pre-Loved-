package org.delcom.app.services;

import org.delcom.app.entities.Product;
import org.delcom.app.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByUserId(UUID userId) {
        return productRepository.findByUserId(userId);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    public Product createProduct(UUID userId, String name, String description, 
                                BigDecimal price, String category, String condition, 
                                String imageUrl) {
        Product product = new Product(userId, name, description, price, category, condition, imageUrl);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(UUID id, UUID userId, String name, String description,
                                BigDecimal price, String category, String condition,
                                String imageUrl) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product tidak ditemukan");
        }

        Product product = productOpt.get();
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk mengubah produk ini");
        }

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setCondition(condition);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            product.setImageUrl(imageUrl);
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id, UUID userId) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product tidak ditemukan");
        }

        Product product = productOpt.get();
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk menghapus produk ini");
        }

        productRepository.delete(product);
    }

    public List<Object[]> getCategoryStatistics() {
        return productRepository.countByCategory();
    }

    public List<Object[]> getConditionStatistics() {
        return productRepository.countByCondition();
    }

    public Long countProductsByUserId(UUID userId) {
        return productRepository.countByUserId(userId);
    }
}

