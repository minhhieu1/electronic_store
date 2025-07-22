package com.altech.electronicstore.repository;

import com.altech.electronicstore.entity.Basket;
import com.altech.electronicstore.entity.BasketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
    Optional<Basket> findByUserIdAndStatus(Long userId, BasketStatus status);
    
    @Query("SELECT b FROM Basket b LEFT JOIN FETCH b.basketItems bi LEFT JOIN FETCH bi.product WHERE b.user.id = :userId AND b.status = :status")
    Optional<Basket> findByUserIdAndStatusWithItems(@Param("userId") Long userId, @Param("status") BasketStatus status);
    
    @Query("SELECT b FROM Basket b LEFT JOIN FETCH b.basketItems bi LEFT JOIN FETCH bi.product WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Basket> findByUserIdWithItemsOrderByCreatedAtDesc(@Param("userId") Long userId);
}
