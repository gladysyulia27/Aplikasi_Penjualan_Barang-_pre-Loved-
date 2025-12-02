package org.delcom.app.repositories;

import org.delcom.app.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByUserId(UUID userId);
    List<Product> findByCategory(String category);
    List<Product> findByCondition(String condition);
    
    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();
    
    @Query("SELECT p.condition, COUNT(p) FROM Product p GROUP BY p.condition")
    List<Object[]> countByCondition();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.userId = :userId")
    Long countByUserId(UUID userId);
}

