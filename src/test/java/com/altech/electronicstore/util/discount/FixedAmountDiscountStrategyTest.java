package com.altech.electronicstore.util.discount;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FixedAmountDiscountStrategyTest {

    private FixedAmountDiscountStrategy strategy;
    private Product product;
    private BasketItem basketItem;
    private Deal deal;

    @BeforeEach
    void setUp() {
        strategy = new FixedAmountDiscountStrategy();
        
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
        deal.setDiscountAmount(new BigDecimal("30.00")); // $30 discount
        deal.setMinimumQuantity(1);
    }

    @Test
    void apply_WithValidDiscountAndSufficientQuantity_ShouldReturnCorrectDiscount() {
        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Fixed discount of $30, total price is $200, so discount is $30
        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void apply_WithDiscountExceedingTotalPrice_ShouldReturnTotalPrice() {
        // Given
        basketItem.setQuantity(1); // Total price = $100
        deal.setDiscountAmount(new BigDecimal("150.00")); // Discount > total price

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should cap discount at total price
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void apply_WithZeroDiscountAmount_ShouldReturnZero() {
        // Given
        deal.setDiscountAmount(BigDecimal.ZERO);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithNullDiscountAmount_ShouldReturnZero() {
        // Given
        deal.setDiscountAmount(null);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithNegativeDiscountAmount_ShouldReturnZero() {
        // Given
        deal.setDiscountAmount(new BigDecimal("-10.00"));

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
        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void apply_WithSmallTotalPrice_ShouldCapDiscountAtTotalPrice() {
        // Given
        product.setPrice(new BigDecimal("15.00"));
        basketItem.setQuantity(1); // Total price = $15
        deal.setDiscountAmount(new BigDecimal("20.00")); // Discount > total price

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should cap discount at total price
        assertEquals(new BigDecimal("15.00"), result);
    }

    @Test
    void apply_WithExactDiscountEqualToTotalPrice_ShouldReturnTotalPrice() {
        // Given
        basketItem.setQuantity(1); // Total price = $100
        deal.setDiscountAmount(new BigDecimal("100.00")); // Discount = total price

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void apply_WithMultipleItems_ShouldConsiderTotalPrice() {
        // Given
        basketItem.setQuantity(5); // Total price = $500
        deal.setDiscountAmount(new BigDecimal("75.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Discount is less than total price, so return discount amount
        assertEquals(new BigDecimal("75.00"), result);
    }

    @Test
    void apply_WithExactMinimumQuantity_ShouldApplyDiscount() {
        // Given
        basketItem.setQuantity(3);
        deal.setMinimumQuantity(3);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should apply discount since quantity equals minimum
        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void apply_WithDecimalPrice_ShouldCalculateCorrectly() {
        // Given
        product.setPrice(new BigDecimal("33.50"));
        basketItem.setQuantity(2); // Total price = $67.00
        deal.setDiscountAmount(new BigDecimal("50.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Discount is less than total price, so return discount amount
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void apply_WithVerySmallPrice_ShouldCapCorrectly() {
        // Given
        product.setPrice(new BigDecimal("0.99"));
        basketItem.setQuantity(1); // Total price = $0.99
        deal.setDiscountAmount(new BigDecimal("5.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should cap discount at total price
        assertEquals(new BigDecimal("0.99"), result);
    }

    @Test
    void apply_WithLargeQuantity_ShouldCalculateCorrectly() {
        // Given
        basketItem.setQuantity(10); // Total price = $1000
        deal.setDiscountAmount(new BigDecimal("100.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Discount is less than total price, so return discount amount
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void apply_WithVeryLargeDiscount_ShouldCapAtTotalPrice() {
        // Given
        basketItem.setQuantity(2); // Total price = $200
        deal.setDiscountAmount(new BigDecimal("999999.99"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should cap discount at total price
        assertEquals(new BigDecimal("200.00"), result);
    }
}
