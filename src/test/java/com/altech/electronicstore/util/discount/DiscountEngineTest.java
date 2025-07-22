package com.altech.electronicstore.util.discount;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.DealType;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.service.DealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountEngineTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private DiscountEngine discountEngine;

    private Product product;
    private BasketItem basketItem;
    private Deal deal;
    private DealType dealType;

    @BeforeEach
    void setUp() {
        // Create test product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        
        // Create test basket item
        basketItem = new BasketItem();
        basketItem.setProduct(product);
        basketItem.setQuantity(2);
        
        // Create test deal type
        dealType = new DealType();
        dealType.setId(1L);
        dealType.setName("Percentage Discount");
        dealType.setStrategyClass("com.altech.electronicstore.util.discount.PercentageDiscountStrategy");
        
        // Create test deal
        deal = new Deal();
        deal.setId(1L);
        deal.setProduct(product);
        deal.setDealType(dealType);
        deal.setDiscountPercent(new BigDecimal("20.00"));
        deal.setMinimumQuantity(1);
        deal.setExpirationDate(LocalDateTime.now().plusDays(1)); // Not expired
    }

    @Test
    void calculateDiscount_WithValidDeal_ShouldReturnCorrectDiscount() {
        // Given
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        // Should apply 20% discount: $200 * 20% = $40.00
        assertEquals(new BigDecimal("40.00"), result);
    }

    @Test
    void calculateDiscount_WithMultipleDeals_ShouldReturnSumOfDiscounts() {
        // Given
        Deal deal2 = new Deal();
        deal2.setId(2L);
        deal2.setProduct(product);
        DealType dealType2 = new DealType();
        dealType2.setName("Fixed Amount Discount");
        dealType2.setStrategyClass("com.altech.electronicstore.util.discount.FixedAmountDiscountStrategy");
        deal2.setDealType(dealType2);
        deal2.setDiscountAmount(new BigDecimal("30.00"));
        deal2.setMinimumQuantity(1);
        deal2.setExpirationDate(LocalDateTime.now().plusDays(1));

        List<Deal> deals = Arrays.asList(deal, deal2);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        // Should sum both discounts: $40.00 + $30.00 = $70.00
        assertEquals(new BigDecimal("70.00"), result);
    }

    @Test
    void calculateDiscount_WithNoDeals_ShouldReturnZero() {
        // Given
        List<Deal> deals = Collections.emptyList();

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateDiscount_WithExpiredDeal_ShouldSkipExpiredDeal() {
        // Given
        deal.setExpirationDate(LocalDateTime.now().minusDays(1)); // Expired
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateDiscount_WithInsufficientQuantity_ShouldReturnZero() {
        // Given
        basketItem.setQuantity(1);
        deal.setMinimumQuantity(2);
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateDiscount_WithInvalidStrategyClass_ShouldThrowException() {
        // Given
        dealType.setStrategyClass("com.altech.electronicstore.util.discount.NonExistentStrategy");
        List<Deal> deals = Arrays.asList(deal);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            discountEngine.calculateDiscount(basketItem, deals)
        );
    }

    @Test
    void calculateDiscount_WithNullDealType_ShouldThrowException() {
        // Given
        deal.setDealType(null);
        List<Deal> deals = Arrays.asList(deal);

        // When & Then
        assertThrows(NullPointerException.class, () -> 
            discountEngine.calculateDiscount(basketItem, deals)
        );
    }

    @Test
    void calculateDiscount_WithNullStrategyClass_ShouldThrowException() {
        // Given
        dealType.setStrategyClass(null);
        List<Deal> deals = Arrays.asList(deal);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            discountEngine.calculateDiscount(basketItem, deals)
        );
    }

    @Test
    void calculateDiscount_WithFixedAmountStrategy_ShouldReturnCorrectDiscount() {
        // Given
        dealType.setStrategyClass("com.altech.electronicstore.util.discount.FixedAmountDiscountStrategy");
        deal.setDiscountAmount(new BigDecimal("50.00"));
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void calculateDiscount_WithBuyOneGetFiftyPercentOffStrategy_ShouldReturnCorrectDiscount() {
        // Given
        dealType.setStrategyClass("com.altech.electronicstore.util.discount.BuyOneGetFiftyPercentOffStrategy");
        deal.setDiscountPercent(new BigDecimal("50.00"));
        deal.setMinimumQuantity(2);
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        // 2 items / 2 minimum = 1 discounted item
        // Discount = 1 item * $100 * 50% = $50.00
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void calculateDiscountsForBasketItems_WithMultipleItems_ShouldReturnCorrectMap() {
        // Given
        Product product2 = new Product();
        product2.setId(2L);
        product2.setPrice(new BigDecimal("50.00"));
        
        BasketItem basketItem2 = new BasketItem();
        basketItem2.setProduct(product2);
        basketItem2.setQuantity(1);
        
        List<BasketItem> items = Arrays.asList(basketItem, basketItem2);
        
        Map<Long, List<Deal>> productDealsMap = new HashMap<>();
        productDealsMap.put(1L, Arrays.asList(deal));
        productDealsMap.put(2L, Collections.emptyList());
        
        when(dealService.getActiveDealsForProducts(any())).thenReturn(productDealsMap);

        // When
        Map<Long, BigDecimal> result = discountEngine.calculateDiscountsForBasketItems(items);

        // Then
        assertEquals(new BigDecimal("40.00"), result.get(1L));
        assertEquals(BigDecimal.ZERO, result.get(2L));
    }

    @Test
    void getDealsForProducts_ShouldCallServiceWithCorrectProductIds() {
        // Given
        Product product2 = new Product();
        product2.setId(2L);
        
        BasketItem basketItem2 = new BasketItem();
        basketItem2.setProduct(product2);
        
        List<BasketItem> items = Arrays.asList(basketItem, basketItem2);
        
        when(dealService.getActiveDealsForProducts(any())).thenReturn(Collections.emptyMap());

        // When
        discountEngine.getDealsForProducts(items);

        // Then
        verify(dealService).getActiveDealsForProducts(Set.of(1L, 2L));
    }

    @Test
    void calculateDiscountsForBasketItems_WithProvidedDealsMap_ShouldUseProvidedDeals() {
        // Given
        List<BasketItem> items = Arrays.asList(basketItem);
        Map<Long, List<Deal>> productDealsMap = Map.of(1L, Arrays.asList(deal));

        // When
        Map<Long, BigDecimal> result = discountEngine.calculateDiscountsForBasketItems(items, productDealsMap);

        // Then
        assertEquals(new BigDecimal("40.00"), result.get(1L));
    }

    @Test
    void calculateDiscount_WithMixedExpiredAndActiveDeals_ShouldOnlyApplyActiveDeals() {
        // Given
        Deal expiredDeal = new Deal();
        expiredDeal.setId(2L);
        expiredDeal.setProduct(product);
        expiredDeal.setDealType(dealType);
        expiredDeal.setDiscountPercent(new BigDecimal("30.00"));
        expiredDeal.setMinimumQuantity(1);
        expiredDeal.setExpirationDate(LocalDateTime.now().minusDays(1)); // Expired
        
        List<Deal> deals = Arrays.asList(deal, expiredDeal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        // Should only apply active deal: $200 * 20% = $40.00
        assertEquals(new BigDecimal("40.00"), result);
    }



    @Test
    void calculateDiscount_WithZeroQuantity_ShouldReturnZero() {
        // Given
        basketItem.setQuantity(0);
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateDiscount_WithLargeDiscount_ShouldCalculateCorrectly() {
        // Given
        basketItem.setQuantity(10);
        deal.setDiscountPercent(new BigDecimal("90.00"));
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        // $1000 * 90% = $900.00
        assertEquals(new BigDecimal("900.00"), result);
    }

    @Test
    void calculateDiscount_WithDecimalCalculations_ShouldRoundCorrectly() {
        // Given
        product.setPrice(new BigDecimal("33.75"));
        basketItem.setQuantity(3);
        deal.setDiscountPercent(new BigDecimal("15.50"));
        List<Deal> deals = Arrays.asList(deal);

        // When
        BigDecimal result = discountEngine.calculateDiscount(basketItem, deals);

        // Then
        // $101.25 * 15.50% = $15.69
        assertEquals(new BigDecimal("15.69"), result);
    }
}
