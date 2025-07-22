package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.basket.BasketDto;
import com.altech.electronicstore.dto.basket.BasketItemDto;
import com.altech.electronicstore.entity.Basket;
import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.entity.BasketStatus;
import com.altech.electronicstore.util.discount.DiscountEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasketMapperTest {

    @Mock
    private DiscountEngine discountEngine;

    @InjectMocks
    private BasketMapper basketMapper;

    @Test
    void toBasketDto_ShouldMapBasketWithItemsAndCalculateDiscounts() {
        // Given
        User user = createUser(1L, "john.doe@example.com");
        Product product1 = createProduct(1L, "Product 1", BigDecimal.valueOf(100.00));
        Product product2 = createProduct(2L, "Product 2", BigDecimal.valueOf(50.00));
        
        BasketItem item1 = createBasketItem(1L, product1, 2);
        BasketItem item2 = createBasketItem(2L, product2, 3);
        
        Basket basket = createBasket(1L, user);
        basket.setBasketItems(new HashSet<>(Arrays.asList(item1, item2)));
        
        // Mock discount calculations
        Map<Long, BigDecimal> discountMap = new HashMap<>();
        discountMap.put(1L, BigDecimal.valueOf(20.00)); // 20 discount for product 1
        discountMap.put(2L, BigDecimal.valueOf(15.00)); // 15 discount for product 2
        
        when(discountEngine.calculateDiscountsForBasketItems(anyList()))
                .thenReturn(discountMap);

        // When
        BasketDto result = basketMapper.toBasketDto(basket);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(2, result.getItems().size());
        assertEquals(5, result.getTotalItems()); // 2 + 3
        
        // Total price: (2 * 100) + (3 * 50) = 200 + 150 = 350
        // Total discount: 20 + 15 = 35
        // Final amount: 350 - 35 = 315
        assertEquals(BigDecimal.valueOf(315.00), result.getTotalAmount());
        
        // Check items
        BasketItemDto itemDto1 = result.getItems().stream()
                .filter(item -> item.getProductId().equals(1L))
                .findFirst().orElse(null);
        assertNotNull(itemDto1);
        assertEquals(2, itemDto1.getQuantity());
        assertEquals(BigDecimal.valueOf(100.00), itemDto1.getUnitPrice());
        assertEquals(BigDecimal.valueOf(200.00), itemDto1.getTotalPrice());
        assertEquals(BigDecimal.valueOf(20.00), itemDto1.getDiscountApplied());
        
        BasketItemDto itemDto2 = result.getItems().stream()
                .filter(item -> item.getProductId().equals(2L))
                .findFirst().orElse(null);
        assertNotNull(itemDto2);
        assertEquals(3, itemDto2.getQuantity());
        assertEquals(BigDecimal.valueOf(50.00), itemDto2.getUnitPrice());
        assertEquals(BigDecimal.valueOf(150.00), itemDto2.getTotalPrice());
        assertEquals(BigDecimal.valueOf(15.00), itemDto2.getDiscountApplied());
    }

    @Test
    void toBasketDto_WithEmptyBasket_ShouldReturnZeroAmounts() {
        // Given
        User user = createUser(1L, "empty@example.com");
        Basket basket = createBasket(1L, user);
        basket.setBasketItems(new HashSet<>());
        
        when(discountEngine.calculateDiscountsForBasketItems(anyList()))
                .thenReturn(new HashMap<>());

        // When
        BasketDto result = basketMapper.toBasketDto(basket);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(0, result.getItems().size());
        assertEquals(0, result.getTotalItems());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
    }

    @Test
    void toBasketDto_WithNoDiscounts_ShouldCalculateCorrectAmounts() {
        // Given
        User user = createUser(1L, "nodiscount@example.com");
        Product product = createProduct(1L, "Expensive Product", BigDecimal.valueOf(1000.00));
        BasketItem item = createBasketItem(1L, product, 1);
        
        Basket basket = createBasket(1L, user);
        basket.setBasketItems(new HashSet<>(Collections.singletonList(item)));
        
        Map<Long, BigDecimal> discountMap = new HashMap<>();
        discountMap.put(1L, BigDecimal.ZERO);
        
        when(discountEngine.calculateDiscountsForBasketItems(anyList()))
                .thenReturn(discountMap);

        // When
        BasketDto result = basketMapper.toBasketDto(basket);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000.00), result.getTotalAmount());
        assertEquals(1, result.getTotalItems());
        
        BasketItemDto itemDto = result.getItems().get(0);
        assertEquals(BigDecimal.valueOf(1000.00), itemDto.getTotalPrice());
        assertEquals(BigDecimal.ZERO, itemDto.getDiscountApplied());
    }

    @Test
    void toBasketDto_WithHighDiscounts_ShouldCalculateCorrectly() {
        // Given
        User user = createUser(1L, "highdisc@example.com");
        Product product = createProduct(1L, "Cheap Product", BigDecimal.valueOf(10.00));
        BasketItem item = createBasketItem(1L, product, 1);
        
        Basket basket = createBasket(1L, user);
        basket.setBasketItems(new HashSet<>(Collections.singletonList(item)));
        
        // Discount higher than product price
        Map<Long, BigDecimal> discountMap = new HashMap<>();
        discountMap.put(1L, BigDecimal.valueOf(15.00));
        
        when(discountEngine.calculateDiscountsForBasketItems(anyList()))
                .thenReturn(discountMap);

        // When
        BasketDto result = basketMapper.toBasketDto(basket);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(10.00).subtract(BigDecimal.valueOf(15.00)), result.getTotalAmount());
        assertEquals(1, result.getTotalItems());
    }

    @Test
    void toBasketDto_WithLargeQuantities_ShouldCalculateCorrectly() {
        // Given
        User user = createUser(1L, "bulk@example.com");
        Product product = createProduct(1L, "Bulk Product", BigDecimal.valueOf(2.50));
        BasketItem item = createBasketItem(1L, product, 1000);
        
        Basket basket = createBasket(1L, user);
        basket.setBasketItems(new HashSet<>(Collections.singletonList(item)));
        
        Map<Long, BigDecimal> discountMap = new HashMap<>();
        discountMap.put(1L, BigDecimal.valueOf(250.00)); // Bulk discount
        
        when(discountEngine.calculateDiscountsForBasketItems(anyList()))
                .thenReturn(discountMap);

        // When
        BasketDto result = basketMapper.toBasketDto(basket);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2500.00).subtract(BigDecimal.valueOf(250.00)), result.getTotalAmount()); // 2500 - 250 = 2250
        assertEquals(1000, result.getTotalItems());
        
        BasketItemDto itemDto = result.getItems().get(0);
        assertEquals(BigDecimal.valueOf(2500.00), itemDto.getTotalPrice()); // 1000 * 2.50
        assertEquals(BigDecimal.valueOf(250.00), itemDto.getDiscountApplied());
    }

    @Test
    void toBasketDto_WithMultipleItemsSameProduct_ShouldCalculateCorrectly() {
        // Given
        User user = createUser(1L, "multi@example.com");
        Product product = createProduct(1L, "Multi Product", BigDecimal.valueOf(25.00));
        BasketItem item1 = createBasketItem(1L, product, 2);
        BasketItem item2 = createBasketItem(2L, product, 3);
        
        Basket basket = createBasket(1L, user);
        basket.setBasketItems(new HashSet<>(Arrays.asList(item1, item2)));
        
        Map<Long, BigDecimal> discountMap = new HashMap<>();
        discountMap.put(1L, BigDecimal.valueOf(10.00)); // 10 discount for this product
        
        when(discountEngine.calculateDiscountsForBasketItems(anyList()))
                .thenReturn(discountMap);

        // When
        BasketDto result = basketMapper.toBasketDto(basket);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals(5, result.getTotalItems()); // 2 + 3
        
        // Total price: (2 * 25) + (3 * 25) = 50 + 75 = 125
        // Total discount: 10 (only counted once in the map)
        assertEquals(BigDecimal.valueOf(115.00), result.getTotalAmount()); // 125 - 10
    }

    // Helper methods
    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private Product createProduct(Long id, String name, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }

    private BasketItem createBasketItem(Long id, Product product, Integer quantity) {
        BasketItem item = new BasketItem();
        item.setId(id);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private Basket createBasket(Long id, User user) {
        Basket basket = new Basket();
        basket.setId(id);
        basket.setUser(user);
        basket.setStatus(BasketStatus.ACTIVE);
        basket.setCreatedAt(LocalDateTime.now());
        return basket;
    }
}
