package com.altech.electronicstore.repository;

import com.altech.electronicstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByAvailabilityTrue(Pageable pageable);
    
    Page<Product> findByCategoryAndAvailabilityTrue(String category, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.availability = true AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:availability IS NULL OR p.availability = :availability)")
    Page<Product> findFilteredProducts(
        @Param("category") String category,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("availability") Boolean availability,
        Pageable pageable
    );
    
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.availability = true")
    java.util.List<String> findDistinctCategories();
}
