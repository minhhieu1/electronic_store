package com.altech.electronicstore.repository;

import com.altech.electronicstore.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    
    @Query("SELECT d FROM Deal d WHERE d.product.id = :productId AND d.expirationDate > :currentTime")
    List<Deal> findActiveDealsForProduct(@Param("productId") Long productId, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT d FROM Deal d WHERE d.product.id IN :productIds AND d.expirationDate > :currentTime")
    List<Deal> findActiveDealsForProducts(@Param("productIds") Set<Long> productIds, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT d FROM Deal d WHERE d.expirationDate > :currentTime")
    List<Deal> findAllActiveDeals(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(d) > 0 FROM Deal d WHERE d.product.id = :productId AND d.dealType.id = :dealTypeId AND d.expirationDate > :currentTime and (:excludeDealId IS NULL OR d.id <> :excludeDealId)")
    boolean existsByProductIdAndDealTypeIdAndNotExpired(@Param("productId") Long productId, @Param("dealTypeId") Long dealTypeId, @Param("currentTime") LocalDateTime currentTime, @Param("excludeDealId") Long excludeDealId);
}
