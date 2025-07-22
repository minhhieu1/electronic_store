package com.altech.electronicstore.repository;

import com.altech.electronicstore.entity.BasketItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {
    Optional<BasketItem> findByBasketIdAndProductId(Long basketId, Long productId);
    
    @Modifying
    @Query("DELETE FROM BasketItem bi WHERE bi.basket.id = :basketId AND bi.product.id = :productId")
    int deleteByBasketIdAndProductIdWithCount(@Param("basketId") Long basketId, @Param("productId") Long productId);
}
