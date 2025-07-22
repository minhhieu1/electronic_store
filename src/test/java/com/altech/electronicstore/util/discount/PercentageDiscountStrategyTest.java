package com.altech.electronicstore.util.discount;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PercentageDiscountStrategyTest {

    private PercentageDiscountStrategy strategy;
    private Product product;
    private BasketItem basketItem;
    private Deal deal;

    @BeforeEach
    void setUp() {
        strategy = new PercentageDiscountStrategy();
        
        // Create test product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        
        // Create test basket item
        basketItem = new BasketItem();
        basketItem.setProduct(product);
        basketItem.setQuantity(2);
        
        // Create test deal
        deal = new Deal();
        deal.setId(1L);
        deal.setDiscountPercent(new BigDecimal("20.00")); // 20% discount
        deal.setMinimumQuantity(1);
    }

    @Test
    void apply_WithValidDiscountAndSufficientQuantity_ShouldReturnCorrectDiscount() {
        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Total price: 100.00 * 2 = 200.00
        // 20% discount: 200.00 * 0.20 = 40.00
        assertEquals(new BigDecimal("40.00"), result);
    }

    @Test
    void apply_WithZeroDiscountPercent_ShouldReturnZero() {
        // Given
        deal.setDiscountPercent(BigDecimal.ZERO);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithNullDiscountPercent_ShouldReturnZero() {
        // Given
        deal.setDiscountPercent(null);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithNegativeDiscountPercent_ShouldReturnZero() {
        // Given
        deal.setDiscountPercent(new BigDecimal("-10.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithInsufficientQuantity_ShouldReturnZero() {
        // Given
        basketItem.setQuantity(1);
        deal.setMinimumQuantity(2);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithNullMinimumQuantity_ShouldDefaultToOne() {
        // Given
        basketItem.setQuantity(1);
        deal.setMinimumQuantity(null);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should apply discount since quantity (1) >= default minimum (1)
        assertEquals(new BigDecimal("20.00"), result); // 100 * 1 * 0.20
    }

    @Test
    void apply_WithHighPercentageDiscount_ShouldCalculateCorrectly() {
        // Given
        deal.setDiscountPercent(new BigDecimal("75.00")); // 75% discount

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Total price: 100.00 * 2 = 200.00
        // 75% discount: 200.00 * 0.75 = 150.00
        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    void apply_WithDecimalPrice_ShouldRoundCorrectly() {
        // Given
        product.setPrice(new BigDecimal("33.33"));
        basketItem.setQuantity(3);
        deal.setDiscountPercent(new BigDecimal("15.50")); // 15.5% discount

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Total price: 33.33 * 3 = 99.99
        // 15.5% discount: 99.99 * 0.155 = 15.498 -> rounded to 15.50
        assertEquals(new BigDecimal("15.50"), result);
    }

    @Test
    void apply_WithSingleItem_ShouldCalculateCorrectly() {
        // Given
        basketItem.setQuantity(1);
        deal.setDiscountPercent(new BigDecimal("10.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Total price: 100.00 * 1 = 100.00
        // 10% discount: 100.00 * 0.10 = 10.00
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void apply_WithLargeQuantity_ShouldCalculateCorrectly() {
        // Given
        basketItem.setQuantity(10);
        deal.setDiscountPercent(new BigDecimal("5.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Total price: 100.00 * 10 = 1000.00
        // 5% discount: 1000.00 * 0.05 = 50.00
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void apply_WithExactMinimumQuantity_ShouldApplyDiscount() {
        // Given
        basketItem.setQuantity(5);
        deal.setMinimumQuantity(5);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should apply discount since quantity equals minimum
        assertEquals(new BigDecimal("100.00"), result); // 100 * 5 * 0.20
    }

    @Test
    void apply_WithSmallDiscountPercent_ShouldRoundCorrectly() {
        // Given
        product.setPrice(new BigDecimal("1.00"));
        basketItem.setQuantity(1);
        deal.setDiscountPercent(new BigDecimal("0.01")); // 0.01% discount

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Total price: 1.00 * 1 = 1.00
        // 0.01% discount: 1.00 * 0.0001 = 0.0001 -> rounded to 0.00
        assertEquals(new BigDecimal("0.00"), result);
    }
}
