package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.order.OrderDto;
import com.altech.electronicstore.entity.Order;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.mapper.OrderMapper;
import com.altech.electronicstore.service.AuthService;
import com.altech.electronicstore.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerOrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private AuthService authService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomerOrderController customerOrderController;

    @Test
    void checkout_WithValidUser_ShouldCreateOrderAndReturnOrderDto() {
        // Given
        String username = "testuser";
        User user = createUser(1L, username);
        Order createdOrder = createOrder(1L, user.getId(), BigDecimal.valueOf(250.00));
        OrderDto orderDto = createOrderDto(1L, BigDecimal.valueOf(250.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(orderService.checkout(user.getId())).thenReturn(createdOrder);
        when(orderMapper.toOrderDto(createdOrder)).thenReturn(orderDto);

        // When
        ResponseEntity<OrderDto> response = customerOrderController.checkout(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(BigDecimal.valueOf(250.00), response.getBody().getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(orderService).checkout(user.getId());
        verify(orderMapper).toOrderDto(createdOrder);
    }

    @Test
    void checkout_WithEmptyBasket_ShouldThrowException() {
        // Given
        String username = "testuser";
        User user = createUser(1L, username);

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(orderService.checkout(user.getId()))
                .thenThrow(new RuntimeException("Cannot checkout empty basket"));

        // When & Then
        try {
            customerOrderController.checkout(authentication);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Cannot checkout empty basket", e.getMessage());
        }

        verify(authService).getCurrentUser(username);
        verify(orderService).checkout(user.getId());
        verify(orderMapper, never()).toOrderDto(any());
    }

    @Test
    void getOrderHistory_WithDefaultParameters_ShouldReturnPagedOrders() {
        // Given
        String username = "testuser";
        User user = createUser(1L, username);
        Order order1 = createOrder(1L, user.getId(), BigDecimal.valueOf(150.00));
        Order order2 = createOrder(2L, user.getId(), BigDecimal.valueOf(300.00));
        List<Order> orders = Arrays.asList(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orders);

        OrderDto orderDto1 = createOrderDto(1L, BigDecimal.valueOf(150.00));
        OrderDto orderDto2 = createOrderDto(2L, BigDecimal.valueOf(300.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(orderService.getOrdersByUserId(eq(user.getId()), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toOrderDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toOrderDto(order2)).thenReturn(orderDto2);

        // When
        ResponseEntity<Page<OrderDto>> response = customerOrderController.getOrderHistory(0, 10, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getContent().get(0).getId());
        assertEquals(2L, response.getBody().getContent().get(1).getId());
        assertEquals(BigDecimal.valueOf(150.00), response.getBody().getContent().get(0).getTotalAmount());
        assertEquals(BigDecimal.valueOf(300.00), response.getBody().getContent().get(1).getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(orderService).getOrdersByUserId(eq(user.getId()), any(Pageable.class));
        verify(orderMapper).toOrderDto(order1);
        verify(orderMapper).toOrderDto(order2);
    }

    @Test
    void getOrderHistory_WithCustomParameters_ShouldReturnPagedOrders() {
        // Given
        String username = "testuser";
        User user = createUser(1L, username);
        Order order = createOrder(1L, user.getId(), BigDecimal.valueOf(500.00));
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));
        OrderDto orderDto = createOrderDto(1L, BigDecimal.valueOf(500.00));

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(orderService.getOrdersByUserId(eq(user.getId()), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toOrderDto(order)).thenReturn(orderDto);

        // When
        ResponseEntity<Page<OrderDto>> response = customerOrderController.getOrderHistory(0, 5, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getContent().get(0).getId());
        assertEquals(BigDecimal.valueOf(500.00), response.getBody().getContent().get(0).getTotalAmount());

        verify(authService).getCurrentUser(username);
        verify(orderService).getOrdersByUserId(eq(user.getId()), any(Pageable.class));
        verify(orderMapper).toOrderDto(order);
    }

    @Test
    void getOrderHistory_WithEmptyResult_ShouldReturnEmptyPage() {
        // Given
        String username = "testuser";
        User user = createUser(1L, username);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList());

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username)).thenReturn(user);
        when(orderService.getOrdersByUserId(eq(user.getId()), any(Pageable.class))).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<OrderDto>> response = customerOrderController.getOrderHistory(0, 10, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getContent().size());
        assertEquals(0, response.getBody().getTotalElements());

        verify(authService).getCurrentUser(username);
        verify(orderService).getOrdersByUserId(eq(user.getId()), any(Pageable.class));
        verify(orderMapper, never()).toOrderDto(any());
    }

    @Test
    void getOrderHistory_WhenUserNotFound_ShouldThrowException() {
        // Given
        String username = "nonexistent";

        when(authentication.getName()).thenReturn(username);
        when(authService.getCurrentUser(username))
                .thenThrow(new RuntimeException("User not found: " + username));

        // When & Then
        try {
            customerOrderController.getOrderHistory(0, 10, authentication);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("User not found: " + username, e.getMessage());
        }

        verify(authService).getCurrentUser(username);
        verify(orderService, never()).getOrdersByUserId(any(), any());
        verify(orderMapper, never()).toOrderDto(any());
    }

    // Helper methods
    private User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }

    private Order createOrder(Long id, Long userId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(id);
        order.setTotalAmount(totalAmount);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    private OrderDto createOrderDto(Long id, BigDecimal totalAmount) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(id);
        orderDto.setTotalAmount(totalAmount);
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setItems(Collections.emptyList());
        return orderDto;
    }
}
