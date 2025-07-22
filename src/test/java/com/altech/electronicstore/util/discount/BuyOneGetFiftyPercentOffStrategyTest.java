package com.altech.electronicstore.util.discount;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BuyOneGetFiftyPercentOffStrategyTest {

    private BuyOneGetFiftyPercentOffStrategy strategy;
    private Product product;
    private BasketItem basketItem;
    private Deal deal;

    @BeforeEach
    void setUp() {
        strategy = new BuyOneGetFiftyPercentOffStrategy();
        
        // Create test product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        
        // Create test basket item
        basketItem = new BasketItem();
        basketItem.setProduct(product);
        basketItem.setQuantity(4);
        
        // Create test deal
        deal = new Deal();
        deal.setId(1L);
        deal.setDiscountPercent(new BigDecimal("50.00")); // 50% off
        deal.setMinimumQuantity(2); // Minimum 2 items to qualify for 1 discount
    }

    @Test
    void apply_WithQualifyingQuantity_ShouldReturnCorrectDiscount() {
        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $100 * 50% = $100.00
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void apply_WithExactMinimumQuantity_ShouldReturnOneDiscount() {
        // Given
        basketItem.setQuantity(2);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 2 items / 2 minimum = 1 discounted item
        // Discount = 1 item * $100 * 50% = $50.00
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void apply_WithInsufficientQuantity_ShouldReturnZero() {
        // Given
        basketItem.setQuantity(1);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithOddQuantity_ShouldDiscountOnlyQualifyingItems() {
        // Given
        basketItem.setQuantity(5);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 5 items / 2 minimum = 2 discounted items (5 / 2 = 2 integer division)
        // Discount = 2 items * $100 * 50% = $100.00
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void apply_WithHigherMinimumQuantity_ShouldCalculateCorrectly() {
        // Given
        basketItem.setQuantity(9);
        deal.setMinimumQuantity(3);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 9 items / 3 minimum = 3 discounted items
        // Discount = 3 items * $100 * 50% = $150.00
        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    void apply_WithDifferentDiscountPercentage_ShouldCalculateCorrectly() {
        // Given
        deal.setDiscountPercent(new BigDecimal("25.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $100 * 25% = $50.00
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void apply_WithNullDiscountPercent_ShouldDefaultToFiftyPercent() {
        // Given
        deal.setDiscountPercent(null);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should default to 50%
        // Discount = 2 items * $100 * 50% = $100.00
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void apply_WithZeroDiscountPercent_ShouldReturnZero() {
        // Given
        deal.setDiscountPercent(BigDecimal.ZERO);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void apply_WithNegativeDiscountPercent_ShouldReturnNegativeDiscount() {
        // Given
        deal.setDiscountPercent(new BigDecimal("-10.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $100 * -10% = -$20.00
        assertEquals(new BigDecimal("-20.00"), result);
    }

    @Test
    void apply_WithHundredPercentDiscount_ShouldReturnFullPrice() {
        // Given
        deal.setDiscountPercent(new BigDecimal("100.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $100 * 100% = $200.00
        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void apply_WithOverHundredPercentDiscount_ShouldCalculateCorrectly() {
        // Given
        deal.setDiscountPercent(new BigDecimal("150.00"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $100 * 150% = $300.00
        assertEquals(new BigDecimal("300.00"), result);
    }

    @Test
    void apply_WithNullMinimumQuantity_ShouldDefaultToTwo() {
        // Given
        deal.setMinimumQuantity(null);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // Should default to minimum quantity 2
        // 4 items / 2 default minimum = 2 discounted items
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void apply_WithMinimumQuantityGreaterThanTotal_ShouldReturnZero() {
        // Given
        basketItem.setQuantity(3);
        deal.setMinimumQuantity(5);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void apply_WithDecimalPrice_ShouldRoundCorrectly() {
        // Given
        product.setPrice(new BigDecimal("33.33"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $33.33 * 50% = $33.34 (rounded)
        assertEquals(new BigDecimal("33.34"), result);
    }

    @Test
    void apply_WithSmallDecimalPrice_ShouldRoundCorrectly() {
        // Given
        product.setPrice(new BigDecimal("0.33"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $0.33 * 50% = $0.34
        assertEquals(new BigDecimal("0.34"), result);
    }

    @Test
    void apply_WithDecimalDiscountPercent_ShouldCalculateCorrectly() {
        // Given
        deal.setDiscountPercent(new BigDecimal("33.33"));

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 4 items / 2 minimum = 2 discounted items
        // Discount = 2 items * $100 * 33.33% = $66.66
        assertEquals(new BigDecimal("66.66"), result);
    }

    @Test
    void apply_WithLargeQuantity_ShouldCalculateCorrectly() {
        // Given
        basketItem.setQuantity(100);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 100 items / 2 minimum = 50 discounted items
        // Discount = 50 items * $100 * 50% = $2500.00
        assertEquals(new BigDecimal("2500.00"), result);
    }

    @Test
    void apply_WithOneQuantityAndMinimumOne_ShouldApplyDiscount() {
        // Given
        basketItem.setQuantity(1);
        deal.setMinimumQuantity(1);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 1 item / 1 minimum = 1 discounted item
        // Discount = 1 item * $100 * 50% = $50.00
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void apply_WithComplexScenario_ShouldCalculateCorrectly() {
        // Given
        basketItem.setQuantity(17);
        product.setPrice(new BigDecimal("25.75"));
        deal.setDiscountPercent(new BigDecimal("15.50"));
        deal.setMinimumQuantity(4);

        // When
        BigDecimal result = strategy.apply(basketItem, deal);

        // Then
        // 17 items / 4 minimum = 4 discounted items
        // Discount = 4 items * $25.75 * 15.50% = $15.96
        assertEquals(new BigDecimal("15.96"), result);
    }
}
