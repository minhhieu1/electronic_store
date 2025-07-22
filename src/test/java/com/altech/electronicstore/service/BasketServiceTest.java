package com.altech.electronicstore.service;

import com.altech.electronicstore.entity.*;
import com.altech.electronicstore.exception.BasketNotFoundException;
import com.altech.electronicstore.exception.InsufficientStockException;
import com.altech.electronicstore.repository.BasketItemRepository;
import com.altech.electronicstore.repository.BasketRepository;
import com.altech.electronicstore.repository.ProductRepository;
import com.altech.electronicstore.repository.UserRepository;
import com.altech.electronicstore.dto.product.StockValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private BasketItemRepository basketItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private BasketService basketService;

    private User testUser;
    private Product testProduct;
    private Basket testBasket;
    private BasketItem testBasketItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(10);
        testProduct.setAvailability(true);

        testBasket = new Basket();
        testBasket.setId(1L);
        testBasket.setUser(testUser);
        testBasket.setStatus(BasketStatus.ACTIVE);
        testBasket.setCreatedAt(LocalDateTime.now());
        testBasket.setBasketItems(new HashSet<>());

        testBasketItem = new BasketItem();
        testBasketItem.setId(1L);
        testBasketItem.setBasket(testBasket);
        testBasketItem.setProduct(testProduct);
        testBasketItem.setQuantity(2);
    }

    @Test
    void getBasketByUserId_WhenActiveBasketExists_ShouldReturnBasket() {
        // Given
        Long userId = 1L;
        when(basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));

        // When
        Basket result = basketService.getBasketByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(testBasket.getId(), result.getId());
        assertEquals(BasketStatus.ACTIVE, result.getStatus());
        verify(basketRepository).findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE);
    }

    @Test
    void getBasketByUserId_WhenNoActiveBasket_ShouldCreateNewBasket() {
        // Given
        Long userId = 1L;
        when(basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(basketRepository.save(any(Basket.class))).thenReturn(testBasket);

        // When
        Basket result = basketService.getBasketByUserId(userId);

        // Then
        assertNotNull(result);
        verify(basketRepository).findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE);
        verify(userRepository).findById(userId);
        verify(basketRepository).save(any(Basket.class));
    }

    @Test
    void getAllBasketsByUserId_ShouldReturnAllUserBaskets() {
        // Given
        Long userId = 1L;
        List<Basket> baskets = Arrays.asList(testBasket);
        when(basketRepository.findByUserIdWithItemsOrderByCreatedAtDesc(userId)).thenReturn(baskets);

        // When
        List<Basket> result = basketService.getAllBasketsByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBasket.getId(), result.get(0).getId());
        verify(basketRepository).findByUserIdWithItemsOrderByCreatedAtDesc(userId);
    }

    @Test
    void addItemToBasket_WhenBasketExistsAndProductAvailable_ShouldAddItem() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        Integer quantity = 3;

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productService.validateAndGetStock(productId, quantity))
                .thenReturn(new StockValidationResult(true, 10, true));
        when(basketItemRepository.findByBasketIdAndProductId(testBasket.getId(), productId))
                .thenReturn(Optional.empty());
        when(basketItemRepository.save(any(BasketItem.class))).thenReturn(testBasketItem);
        when(basketRepository.findById(testBasket.getId())).thenReturn(Optional.of(testBasket));

        // When
        Basket result = basketService.addItemToBasket(userId, productId, quantity);

        // Then
        assertNotNull(result);
        verify(basketRepository).findByUserIdAndStatus(userId, BasketStatus.ACTIVE);
        verify(productRepository).findById(productId);
        verify(productService).validateAndGetStock(productId, quantity);
        verify(basketItemRepository).save(any(BasketItem.class));
    }

    @Test
    void addItemToBasket_WhenProductNotAvailable_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        Integer quantity = 3;

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productService.validateAndGetStock(productId, quantity))
                .thenReturn(new StockValidationResult(false, 0, false));

        // When & Then
        assertThrows(RuntimeException.class, () -> basketService.addItemToBasket(userId, productId, quantity));
        verify(basketItemRepository, never()).save(any(BasketItem.class));
    }

    @Test
    void addItemToBasket_WhenInsufficientStock_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        Integer quantity = 15; // More than available stock

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productService.validateAndGetStock(productId, quantity))
                .thenReturn(new StockValidationResult(false, 10, true));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> basketService.addItemToBasket(userId, productId, quantity));
        verify(basketItemRepository, never()).save(any(BasketItem.class));
    }

    @Test
    void addItemToBasket_WhenItemAlreadyExists_ShouldUpdateQuantity() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        Integer quantity = 3;
        Integer existingQuantity = 2;
        Integer newTotalQuantity = existingQuantity + quantity;

        testBasketItem.setQuantity(existingQuantity);

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productService.validateAndGetStock(productId, newTotalQuantity))
                .thenReturn(new StockValidationResult(true, 10, true));
        when(basketItemRepository.findByBasketIdAndProductId(testBasket.getId(), productId))
                .thenReturn(Optional.of(testBasketItem));
        when(basketItemRepository.save(any(BasketItem.class))).thenReturn(testBasketItem);
        when(basketRepository.findById(testBasket.getId())).thenReturn(Optional.of(testBasket));

        // When
        Basket result = basketService.addItemToBasket(userId, productId, quantity);

        // Then
        assertNotNull(result);
        assertEquals(newTotalQuantity, testBasketItem.getQuantity());
        verify(basketItemRepository).save(testBasketItem);
    }

    @Test
    void removeItemFromBasket_WhenItemExists_ShouldRemoveItem() {
        // Given
        Long userId = 1L;
        Long productId = 1L;

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(basketItemRepository.deleteByBasketIdAndProductIdWithCount(testBasket.getId(), productId))
                .thenReturn(1); // Return count of deleted items
        when(basketRepository.findById(testBasket.getId())).thenReturn(Optional.of(testBasket));

        // When
        Basket result = basketService.removeItemFromBasket(userId, productId);

        // Then
        assertNotNull(result);
        verify(basketItemRepository).deleteByBasketIdAndProductIdWithCount(testBasket.getId(), productId);
        verify(basketRepository).findById(testBasket.getId());
    }

    @Test
    void removeItemFromBasket_WhenBasketNotActive_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        testBasket.setStatus(BasketStatus.CHECKED_OUT);

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));

        // When & Then
        assertThrows(RuntimeException.class, () -> basketService.removeItemFromBasket(userId, productId));
        verify(basketItemRepository, never()).delete(any(BasketItem.class));
    }

    @Test
    void removeItemFromBasket_WhenItemNotExists_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long productId = 1L;

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(basketItemRepository.deleteByBasketIdAndProductIdWithCount(testBasket.getId(), productId))
                .thenReturn(0); // No rows deleted - item not found

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> basketService.removeItemFromBasket(userId, productId));
        assertEquals("Item not found in basket", exception.getMessage());
        verify(basketItemRepository).deleteByBasketIdAndProductIdWithCount(testBasket.getId(), productId);
    }

    @Test
    void updateItemQuantity_WhenValidQuantity_ShouldUpdateItem() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        Integer newQuantity = 5;

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(basketItemRepository.findByBasketIdAndProductId(testBasket.getId(), productId))
                .thenReturn(Optional.of(testBasketItem));
        when(productService.validateAndGetStock(productId, newQuantity))
                .thenReturn(new StockValidationResult(true, 10, true));
        when(basketItemRepository.save(any(BasketItem.class))).thenReturn(testBasketItem);
        when(basketRepository.findById(testBasket.getId())).thenReturn(Optional.of(testBasket));

        // When
        Basket result = basketService.updateItemQuantity(userId, productId, newQuantity);

        // Then
        assertNotNull(result);
        assertEquals(newQuantity, testBasketItem.getQuantity());
        verify(basketItemRepository).save(testBasketItem);
    }

    @Test
    void updateItemQuantity_WhenQuantityIsZero_ShouldRemoveItem() {
        // Given
        Long userId = 1L;
        Long productId = 1L;
        Integer newQuantity = 0;

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(basketItemRepository.findByBasketIdAndProductId(testBasket.getId(), productId))
                .thenReturn(Optional.of(testBasketItem));
        when(basketRepository.findById(testBasket.getId())).thenReturn(Optional.of(testBasket));

        // When
        Basket result = basketService.updateItemQuantity(userId, productId, newQuantity);

        // Then
        assertNotNull(result);
        verify(basketItemRepository).delete(testBasketItem);
        verify(basketItemRepository, never()).save(any(BasketItem.class));
    }

    @Test
    void clearBasket_WhenBasketExists_ShouldClearAllItems() {
        // Given
        Long userId = 1L;
        testBasket.getBasketItems().add(testBasketItem);

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));

        // When
        basketService.clearBasket(userId);

        // Then
        assertTrue(testBasket.getBasketItems().isEmpty());
        verify(basketRepository).save(testBasket);
    }

    @Test
    void clearBasket_WhenBasketNotActive_ShouldThrowException() {
        // Given
        Long userId = 1L;
        testBasket.setStatus(BasketStatus.CHECKED_OUT);

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));

        // When & Then
        assertThrows(RuntimeException.class, () -> basketService.clearBasket(userId));
        verify(basketRepository, never()).save(any(Basket.class));
    }

    @Test
    void clearBasket_WhenBasketNotExists_ShouldThrowException() {
        // Given
        Long userId = 1L;

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BasketNotFoundException.class, () -> basketService.clearBasket(userId));
        verify(basketRepository, never()).save(any(Basket.class));
    }

    @Test
    void checkoutBasket_WhenBasketHasSufficientStock_ShouldCheckoutSuccessfully() {
        // Given
        Long userId = 1L;
        testBasket.getBasketItems().add(testBasketItem);

        when(basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(productService.hasStock(testProduct.getId(), testBasketItem.getQuantity()))
                .thenReturn(true);
        when(basketRepository.save(any(Basket.class))).thenReturn(testBasket);

        // When
        Basket result = basketService.checkoutBasket(userId);

        // Then
        assertNotNull(result);
        assertEquals(BasketStatus.CHECKED_OUT, result.getStatus());
        verify(productService).commitStockReduction(anyList());
        verify(basketRepository).save(testBasket);
    }

    @Test
    void checkoutBasket_WhenInsufficientStock_ShouldThrowException() {
        // Given
        Long userId = 1L;
        testBasket.getBasketItems().add(testBasketItem);

        when(basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));
        when(productService.hasStock(testProduct.getId(), testBasketItem.getQuantity()))
                .thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> basketService.checkoutBasket(userId));
        verify(productService, never()).commitStockReduction(anyList());
        verify(basketRepository, never()).save(any(Basket.class));
    }

    @Test
    void checkoutBasket_WhenBasketNotActive_ShouldThrowException() {
        // Given
        Long userId = 1L;
        testBasket.setStatus(BasketStatus.CHECKED_OUT);

        when(basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(testBasket));

        // When & Then
        assertThrows(RuntimeException.class, () -> basketService.checkoutBasket(userId));
        verify(productService, never()).commitStockReduction(anyList());
    }

    @Test
    void checkoutBasket_WhenBasketNotExists_ShouldThrowException() {
        // Given
        Long userId = 1L;

        when(basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BasketNotFoundException.class, () -> basketService.checkoutBasket(userId));
        verify(productService, never()).commitStockReduction(anyList());
    }

    @Test
    void createNewBasket_ShouldCreateBasketForUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(basketRepository.save(any(Basket.class))).thenReturn(testBasket);

        // When
        Basket result = basketService.createNewBasket(userId);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(BasketStatus.ACTIVE, result.getStatus());
        verify(basketRepository).save(any(Basket.class));
    }

    @Test
    void createNewBasket_WhenActiveBasketExists_ShouldExpireOldBasket() {
        // Given
        Long userId = 1L;
        Basket oldBasket = new Basket();
        oldBasket.setId(2L);
        oldBasket.setStatus(BasketStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(oldBasket));
        when(basketRepository.save(any(Basket.class))).thenReturn(testBasket);

        // When
        Basket result = basketService.createNewBasket(userId);

        // Then
        assertNotNull(result);
        assertEquals(BasketStatus.EXPIRED, oldBasket.getStatus());
        verify(basketRepository, times(2)).save(any(Basket.class)); // Once for expiring old, once for new
    }
}
