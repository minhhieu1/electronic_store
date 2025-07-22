package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.basket.BasketDto;
import com.altech.electronicstore.entity.Basket;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.mapper.BasketMapper;
import com.altech.electronicstore.service.AuthService;
import com.altech.electronicstore.service.BasketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerBasketControllerTest {

    @Mock
    private BasketService basketService;

    @Mock
    private AuthService authService;

    @Mock
    private BasketMapper basketMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomerBasketController customerBasketController;

    @Test
    void getBasket_ShouldReturnCurrentUserBasket() {
        // Given
        String username = "testuser";
        User user = createUser(1L, username);
        Basket basket = createBasket(1L, user.getId());
        BasketDto basketDto = createBasketDto(1L, BigDecimal.valueOf(150.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(basketService.getBasketByUserId(user.getId())).thenReturn(basket);
        when(basketMapper.toBasketDto(basket)).thenReturn(basketDto);

        // When
        ResponseEntity<BasketDto> response = customerBasketController.getBasket(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(BigDecimal.valueOf(150.00), response.getBody().getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(basketService).getBasketByUserId(user.getId());
        verify(basketMapper).toBasketDto(basket);
    }

    @Test
    void addItemToBasket_WithValidProductAndQuantity_ShouldReturnUpdatedBasket() {
        // Given
        String username = "testuser";
        Long productId = 1L;
        Integer quantity = 2;
        User user = createUser(1L, username);
        Basket updatedBasket = createBasket(1L, user.getId());
        BasketDto basketDto = createBasketDto(1L, BigDecimal.valueOf(200.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(basketService.addItemToBasket(user.getId(), productId, quantity)).thenReturn(updatedBasket);
        when(basketMapper.toBasketDto(updatedBasket)).thenReturn(basketDto);

        // When
        ResponseEntity<BasketDto> response = customerBasketController.addItemToBasket(productId, quantity, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(BigDecimal.valueOf(200.00), response.getBody().getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(basketService).addItemToBasket(user.getId(), productId, quantity);
        verify(basketMapper).toBasketDto(updatedBasket);
    }

    @Test
    void updateItemQuantity_WithValidProductAndQuantity_ShouldReturnUpdatedBasket() {
        // Given
        String username = "testuser";
        Long productId = 1L;
        Integer newQuantity = 3;
        User user = createUser(1L, username);
        Basket updatedBasket = createBasket(1L, user.getId());
        BasketDto basketDto = createBasketDto(1L, BigDecimal.valueOf(300.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(basketService.updateItemQuantity(user.getId(), productId, newQuantity)).thenReturn(updatedBasket);
        when(basketMapper.toBasketDto(updatedBasket)).thenReturn(basketDto);

        // When
        ResponseEntity<BasketDto> response = customerBasketController.updateItemQuantity(productId, newQuantity, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(BigDecimal.valueOf(300.00), response.getBody().getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(basketService).updateItemQuantity(user.getId(), productId, newQuantity);
        verify(basketMapper).toBasketDto(updatedBasket);
    }

    @Test
    void updateItemQuantity_WithZeroQuantity_ShouldRemoveItem() {
        // Given
        String username = "testuser";
        Long productId = 1L;
        Integer zeroQuantity = 0;
        User user = createUser(1L, username);
        Basket updatedBasket = createBasket(1L, user.getId());
        BasketDto basketDto = createBasketDto(1L, BigDecimal.valueOf(0.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(basketService.updateItemQuantity(user.getId(), productId, zeroQuantity)).thenReturn(updatedBasket);
        when(basketMapper.toBasketDto(updatedBasket)).thenReturn(basketDto);

        // When
        ResponseEntity<BasketDto> response = customerBasketController.updateItemQuantity(productId, zeroQuantity, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(0.00), response.getBody().getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(basketService).updateItemQuantity(user.getId(), productId, zeroQuantity);
        verify(basketMapper).toBasketDto(updatedBasket);
    }

    @Test
    void removeItemFromBasket_WithValidProductId_ShouldReturnUpdatedBasket() {
        // Given
        String username = "testuser";
        Long productId = 1L;
        User user = createUser(1L, username);
        Basket updatedBasket = createBasket(1L, user.getId());
        BasketDto basketDto = createBasketDto(1L, BigDecimal.valueOf(50.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(basketService.removeItemFromBasket(user.getId(), productId)).thenReturn(updatedBasket);
        when(basketMapper.toBasketDto(updatedBasket)).thenReturn(basketDto);

        // When
        ResponseEntity<BasketDto> response = customerBasketController.removeItemFromBasket(productId, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(BigDecimal.valueOf(50.00), response.getBody().getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(basketService).removeItemFromBasket(user.getId(), productId);
        verify(basketMapper).toBasketDto(updatedBasket);
    }

    @Test
    void clearBasket_ShouldReturnNoContent() {
        // Given
        String username = "testuser";
        User user = createUser(1L, username);

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        doNothing().when(basketService).clearBasket(user.getId());

        // When
        ResponseEntity<Void> response = customerBasketController.clearBasket(authentication);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(authService).getCurrentUser(username);
        verify(basketService).clearBasket(user.getId());
    }

    @Test
    void getBasket_WhenUserNotFound_ShouldThrowException() {
        // Given
        String username = "nonexistent";

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username))
                .thenThrow(new RuntimeException("User not found: " + username));

        // When & Then
        try {
            customerBasketController.getBasket(authentication);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("User not found: " + username, e.getMessage());
        }

        verify(authService).getCurrentUser(username);
        verify(basketService, never()).getBasketByUserId(anyLong());
        verify(basketMapper, never()).toBasketDto(any());
    }

    @Test
    void addItemToBasket_WhenProductNotFound_ShouldThrowException() {
        // Given
        String username = "testuser";
        Long productId = 999L;
        Integer quantity = 1;
        User user = createUser(1L, username);

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(basketService.addItemToBasket(user.getId(), productId, quantity))
                .thenThrow(new RuntimeException("Product not found with id: " + productId));

        // When & Then
        try {
            customerBasketController.addItemToBasket(productId, quantity, authentication);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Product not found with id: " + productId, e.getMessage());
        }

        verify(authService).getCurrentUser(username);
        verify(basketService).addItemToBasket(user.getId(), productId, quantity);
        verify(basketMapper, never()).toBasketDto(any());
    }

    // Helper methods
    private User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }

    private Basket createBasket(Long id, Long userId) {
        Basket basket = new Basket();
        basket.setId(id);
        basket.setCreatedAt(LocalDateTime.now());
        return basket;
    }

    private BasketDto createBasketDto(Long id, BigDecimal totalAmount) {
        BasketDto basketDto = new BasketDto();
        basketDto.setId(id);
        basketDto.setTotalAmount(totalAmount);
        basketDto.setTotalItems(1);
        basketDto.setStatus("ACTIVE");
        basketDto.setItems(Collections.emptyList());
        return basketDto;
    }
}
