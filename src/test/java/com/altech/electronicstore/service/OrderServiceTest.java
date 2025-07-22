package com.altech.electronicstore.service;

import com.altech.electronicstore.entity.*;
import com.altech.electronicstore.exception.BasketNotFoundException;
import com.altech.electronicstore.repository.OrderRepository;
import com.altech.electronicstore.repository.UserRepository;
import com.altech.electronicstore.util.discount.DiscountEngine;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BasketService basketService;

    @Mock
    private DealService dealService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiscountEngine discountEngine;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private BasketItem testBasketItem;
    private Basket testBasket;
    private Deal testDeal;
    private Order testOrder;
    private DealType testDealType;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Create test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setStock(10);

        // Create test basket item
        testBasketItem = new BasketItem();
        testBasketItem.setId(1L);
        testBasketItem.setProduct(testProduct);
        testBasketItem.setQuantity(2);

        // Create test basket with items
        testBasket = new Basket();
        testBasket.setId(1L);
        testBasket.setUser(testUser);
        testBasket.setStatus(BasketStatus.CHECKED_OUT);
        testBasket.setBasketItems(new HashSet<>());
        
        // Link basket item to basket properly
        testBasketItem.setBasket(testBasket);
        testBasket.getBasketItems().add(testBasketItem);

        // Create test deal type
        testDealType = new DealType();
        testDealType.setId(1L);
        testDealType.setName("Buy 2 Get 1 Free");

        // Create test deal
        testDeal = new Deal();
        testDeal.setId(1L);
        testDeal.setDealType(testDealType);
        testDeal.setProduct(testProduct);
        testDeal.setExpirationDate(LocalDateTime.now().plusDays(30));
        testDeal.setMinimumQuantity(2);

        // Create test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(BigDecimal.valueOf(200.00));
    }

    @Test
    void checkout_WhenValidBasket_ShouldCreateOrder() {
        // Given
        Long userId = 1L;

        when(basketService.checkoutBasket(userId)).thenReturn(testBasket);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(discountEngine.getDealsForProducts(anyList()))
                .thenReturn(Map.of(1L, Arrays.asList(testDeal)));
        when(discountEngine.calculateDiscountsForBasketItems(anyList(), anyMap()))
                .thenReturn(Map.of(1L, BigDecimal.TEN));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.checkout(userId);

        // Then
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(basketService).checkoutBasket(userId);
        verify(userRepository).findById(userId);
        verify(discountEngine).getDealsForProducts(anyList());
        verify(discountEngine).calculateDiscountsForBasketItems(anyList(), anyMap());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkout_WhenBasketNotFound_ShouldThrowException() {
        // Given
        Long userId = 1L;

        when(basketService.checkoutBasket(userId)).thenThrow(new BasketNotFoundException("Basket not found"));

        // When & Then
        assertThrows(BasketNotFoundException.class, () -> orderService.checkout(userId));
        verify(basketService).checkoutBasket(userId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkout_WhenBasketServiceThrowsException_ShouldThrowException() {
        // Given
        Long userId = 1L;

        when(basketService.checkoutBasket(userId)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.checkout(userId));
        verify(basketService).checkoutBasket(userId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkout_WhenBasketWithNoItems_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Basket emptyBasket = new Basket();
        emptyBasket.setId(1L);
        emptyBasket.setUser(testUser);
        emptyBasket.setBasketItems(new HashSet<>()); // Empty basket

        when(basketService.checkoutBasket(userId)).thenReturn(emptyBasket);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.checkout(userId));
        assertEquals("Cannot create order from empty basket", exception.getMessage());
        verify(basketService).checkoutBasket(userId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkout_WhenBasketWithMultipleProducts_ShouldCreateOrder() {
        // Given
        Long userId = 1L;
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(50.00));

        BasketItem basketItem2 = new BasketItem();
        basketItem2.setId(2L);
        basketItem2.setProduct(product2);
        basketItem2.setQuantity(1);
        basketItem2.setBasket(testBasket);
        
        // Add second item to basket
        testBasket.getBasketItems().add(basketItem2);

        when(basketService.checkoutBasket(userId)).thenReturn(testBasket);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(discountEngine.getDealsForProducts(anyList()))
                .thenReturn(Map.of(1L, Arrays.asList(testDeal), 2L, Collections.emptyList()));
        when(discountEngine.calculateDiscountsForBasketItems(anyList(), anyMap()))
                .thenReturn(Map.of(1L, BigDecimal.TEN, 2L, BigDecimal.ZERO));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.checkout(userId);

        // Then
        assertNotNull(result);
        verify(basketService).checkoutBasket(userId);
        verify(userRepository).findById(userId);
        verify(discountEngine).getDealsForProducts(anyList());
        verify(discountEngine).calculateDiscountsForBasketItems(anyList(), anyMap());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkout_WhenBasketWithDeals_ShouldApplyDealsAndCreateOrder() {
        // Given
        Long userId = 1L;
        testBasketItem.setQuantity(3); // Enough to trigger the deal

        when(basketService.checkoutBasket(userId)).thenReturn(testBasket);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(discountEngine.getDealsForProducts(anyList()))
                .thenReturn(Map.of(1L, Arrays.asList(testDeal)));
        when(discountEngine.calculateDiscountsForBasketItems(anyList(), anyMap()))
                .thenReturn(Map.of(1L, BigDecimal.TEN));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.checkout(userId);

        // Then
        assertNotNull(result);
        verify(basketService).checkoutBasket(userId);
        verify(userRepository).findById(userId);
        verify(discountEngine).getDealsForProducts(anyList());
        verify(discountEngine).calculateDiscountsForBasketItems(anyList(), anyMap());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkout_WhenDealsServiceThrowsException_ShouldPropagateException() {
        // Given
        Long userId = 1L;

        when(basketService.checkoutBasket(userId)).thenReturn(testBasket);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(discountEngine.getDealsForProducts(anyList()))
                .thenThrow(new RuntimeException("Deals service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.checkout(userId));
        verify(basketService).checkoutBasket(userId);
        verify(userRepository).findById(userId);
        verify(discountEngine).getDealsForProducts(anyList());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkout_WhenOrderRepositoryThrowsException_ShouldPropagateException() {
        // Given
        Long userId = 1L;

        when(basketService.checkoutBasket(userId)).thenReturn(testBasket);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(discountEngine.getDealsForProducts(anyList()))
                .thenReturn(Map.of(1L, Arrays.asList(testDeal)));
        when(discountEngine.calculateDiscountsForBasketItems(anyList(), anyMap()))
                .thenReturn(Map.of(1L, BigDecimal.TEN));
        when(orderRepository.save(any(Order.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.checkout(userId));
        verify(basketService).checkoutBasket(userId);
        verify(userRepository).findById(userId);
        verify(discountEngine).getDealsForProducts(anyList());
        verify(discountEngine).calculateDiscountsForBasketItems(anyList(), anyMap());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkout_WhenNullUserId_ShouldHandleGracefully() {
        // Given
        Long nullUserId = null;

        when(basketService.checkoutBasket(nullUserId)).thenThrow(new IllegalArgumentException("User ID cannot be null"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(nullUserId));
        verify(basketService).checkoutBasket(nullUserId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkout_WhenZeroUserId_ShouldProceedNormally() {
        // Given
        Long zeroUserId = 0L;
        
        User zeroUser = new User();
        zeroUser.setId(0L);
        zeroUser.setUsername("zerouser");

        when(basketService.checkoutBasket(zeroUserId)).thenReturn(testBasket);
        when(userRepository.findById(zeroUserId)).thenReturn(Optional.of(zeroUser));
        when(discountEngine.getDealsForProducts(anyList()))
                .thenReturn(Collections.emptyMap());
        when(discountEngine.calculateDiscountsForBasketItems(anyList(), anyMap()))
                .thenReturn(Collections.emptyMap());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.checkout(zeroUserId);

        // Then
        assertNotNull(result);
        verify(basketService).checkoutBasket(zeroUserId);
        verify(userRepository).findById(zeroUserId);
        verify(discountEngine).getDealsForProducts(anyList());
        verify(discountEngine).calculateDiscountsForBasketItems(anyList(), anyMap());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkout_WhenNegativeUserId_ShouldProceedNormally() {
        // Given
        Long negativeUserId = -1L;
        
        User negativeUser = new User();
        negativeUser.setId(-1L);
        negativeUser.setUsername("negativeuser");

        when(basketService.checkoutBasket(negativeUserId)).thenReturn(testBasket);
        when(userRepository.findById(negativeUserId)).thenReturn(Optional.of(negativeUser));
        when(discountEngine.getDealsForProducts(anyList()))
                .thenReturn(Collections.emptyMap());
        when(discountEngine.calculateDiscountsForBasketItems(anyList(), anyMap()))
                .thenReturn(Collections.emptyMap());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.checkout(negativeUserId);

        // Then
        assertNotNull(result);
        verify(basketService).checkoutBasket(negativeUserId);
        verify(userRepository).findById(negativeUserId);
        verify(discountEngine).getDealsForProducts(anyList());
        verify(discountEngine).calculateDiscountsForBasketItems(anyList(), anyMap());
        verify(orderRepository).save(any(Order.class));
    }
}
