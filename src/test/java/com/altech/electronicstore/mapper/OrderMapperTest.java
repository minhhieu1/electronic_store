package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.order.OrderDto;
import com.altech.electronicstore.dto.order.OrderItemDto;
import com.altech.electronicstore.entity.Order;
import com.altech.electronicstore.entity.OrderItem;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    @InjectMocks
    private OrderMapper orderMapper;

    @Test
    void toOrderDto_ShouldMapOrderWithItemsToDto() {
        // Given
        LocalDateTime orderDate = LocalDateTime.now();
        User user = createUser(1L, "customer@example.com");
        
        Product product1 = createProduct(1L, "Product 1", BigDecimal.valueOf(100.00));
        Product product2 = createProduct(2L, "Product 2", BigDecimal.valueOf(50.00));
        
        OrderItem item1 = createOrderItem(1L, product1, 2, BigDecimal.valueOf(100.00), 
                                        BigDecimal.valueOf(200.00), BigDecimal.valueOf(20.00));
        OrderItem item2 = createOrderItem(2L, product2, 1, BigDecimal.valueOf(50.00), 
                                        BigDecimal.valueOf(50.00), BigDecimal.valueOf(5.00));
        
        Order order = createOrder(1L, user, BigDecimal.valueOf(250.00), BigDecimal.valueOf(25.00), 
                                BigDecimal.valueOf(225.00), orderDate, "Test order");
        order.setOrderItems(new HashSet<>(Arrays.asList(item1, item2)));

        // When
        OrderDto result = orderMapper.toOrderDto(order);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(250.00), result.getTotalAmount());
        assertEquals(BigDecimal.valueOf(25.00), result.getTotalDiscount());
        assertEquals(BigDecimal.valueOf(225.00), result.getFinalAmount());
        assertEquals(orderDate, result.getOrderDate());
        assertEquals("Test order", result.getNote());
        assertEquals(2, result.getItems().size());
        
        // Check items are mapped correctly
        OrderItemDto mappedItem1 = result.getItems().stream()
                .filter(item -> item.getProductId().equals(1L))
                .findFirst().orElse(null);
        assertNotNull(mappedItem1);
        assertEquals("Product 1", mappedItem1.getProductName());
        assertEquals(2, mappedItem1.getQuantity());
        assertEquals(BigDecimal.valueOf(100.00), mappedItem1.getUnitPrice());
        assertEquals(BigDecimal.valueOf(200.00), mappedItem1.getTotalPrice());
        assertEquals(BigDecimal.valueOf(20.00), mappedItem1.getDiscountApplied());
        
        OrderItemDto mappedItem2 = result.getItems().stream()
                .filter(item -> item.getProductId().equals(2L))
                .findFirst().orElse(null);
        assertNotNull(mappedItem2);
        assertEquals("Product 2", mappedItem2.getProductName());
        assertEquals(1, mappedItem2.getQuantity());
        assertEquals(BigDecimal.valueOf(50.00), mappedItem2.getUnitPrice());
        assertEquals(BigDecimal.valueOf(50.00), mappedItem2.getTotalPrice());
        assertEquals(BigDecimal.valueOf(5.00), mappedItem2.getDiscountApplied());
    }

    @Test
    void toOrderDto_WithEmptyItems_ShouldMapOrderWithEmptyItemsList() {
        // Given
        LocalDateTime orderDate = LocalDateTime.now();
        User user = createUser(1L, "empty@example.com");
        
        Order order = createOrder(1L, user, BigDecimal.ZERO, BigDecimal.ZERO, 
                                BigDecimal.ZERO, orderDate, "Empty order");
        order.setOrderItems(new HashSet<>());

        // When
        OrderDto result = orderMapper.toOrderDto(order);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.getTotalDiscount());
        assertEquals(BigDecimal.ZERO, result.getFinalAmount());
        assertEquals(orderDate, result.getOrderDate());
        assertEquals("Empty order", result.getNote());
        assertEquals(0, result.getItems().size());
    }

    @Test
    void toOrderItemDto_ShouldMapOrderItemToDto() {
        // Given
        Product product = createProduct(1L, "Test Product", BigDecimal.valueOf(75.00));
        OrderItem item = createOrderItem(1L, product, 3, BigDecimal.valueOf(75.00), 
                                       BigDecimal.valueOf(225.00), BigDecimal.valueOf(15.00));

        // When
        OrderItemDto result = orderMapper.toOrderItemDto(item);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getProductId());
        assertEquals("Test Product", result.getProductName());
        assertEquals(3, result.getQuantity());
        assertEquals(BigDecimal.valueOf(75.00), result.getUnitPrice());
        assertEquals(BigDecimal.valueOf(225.00), result.getTotalPrice());
        assertEquals(BigDecimal.valueOf(15.00), result.getDiscountApplied());
    }

    @Test
    void toOrderItemDto_WithZeroDiscount_ShouldMapCorrectly() {
        // Given
        Product product = createProduct(2L, "No Discount Product", BigDecimal.valueOf(30.00));
        OrderItem item = createOrderItem(2L, product, 1, BigDecimal.valueOf(30.00), 
                                       BigDecimal.valueOf(30.00), BigDecimal.ZERO);

        // When
        OrderItemDto result = orderMapper.toOrderItemDto(item);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(2L, result.getProductId());
        assertEquals("No Discount Product", result.getProductName());
        assertEquals(1, result.getQuantity());
        assertEquals(BigDecimal.valueOf(30.00), result.getUnitPrice());
        assertEquals(BigDecimal.valueOf(30.00), result.getTotalPrice());
        assertEquals(BigDecimal.ZERO, result.getDiscountApplied());
    }

    @Test
    void toOrderDtoList_ShouldMapListOfOrdersToListOfDtos() {
        // Given
        LocalDateTime orderDate1 = LocalDateTime.now().minusDays(1);
        LocalDateTime orderDate2 = LocalDateTime.now();
        
        User user1 = createUser(1L, "user1@example.com");
        User user2 = createUser(2L, "user2@example.com");
        
        Order order1 = createOrder(1L, user1, BigDecimal.valueOf(100.00), BigDecimal.valueOf(10.00), 
                                 BigDecimal.valueOf(90.00), orderDate1, "First order");
        order1.setOrderItems(new HashSet<>());
        
        Order order2 = createOrder(2L, user2, BigDecimal.valueOf(200.00), BigDecimal.valueOf(20.00), 
                                 BigDecimal.valueOf(180.00), orderDate2, "Second order");
        order2.setOrderItems(new HashSet<>());
        
        List<Order> orders = Arrays.asList(order1, order2);

        // When
        List<OrderDto> result = orderMapper.toOrderDtoList(orders);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        OrderDto dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals(BigDecimal.valueOf(100.00), dto1.getTotalAmount());
        assertEquals(BigDecimal.valueOf(10.00), dto1.getTotalDiscount());
        assertEquals(BigDecimal.valueOf(90.00), dto1.getFinalAmount());
        assertEquals(orderDate1, dto1.getOrderDate());
        assertEquals("First order", dto1.getNote());

        OrderDto dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals(BigDecimal.valueOf(200.00), dto2.getTotalAmount());
        assertEquals(BigDecimal.valueOf(20.00), dto2.getTotalDiscount());
        assertEquals(BigDecimal.valueOf(180.00), dto2.getFinalAmount());
        assertEquals(orderDate2, dto2.getOrderDate());
        assertEquals("Second order", dto2.getNote());
    }

    @Test
    void toOrderDtoList_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        List<Order> emptyList = Collections.emptyList();

        // When
        List<OrderDto> result = orderMapper.toOrderDtoList(emptyList);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void toOrderDto_WithHighValueOrder_ShouldMapCorrectly() {
        // Given
        LocalDateTime orderDate = LocalDateTime.now();
        User user = createUser(1L, "highvalue@example.com");
        
        Product expensiveProduct = createProduct(1L, "Luxury Item", BigDecimal.valueOf(5000.00));
        OrderItem expensiveItem = createOrderItem(1L, expensiveProduct, 2, BigDecimal.valueOf(5000.00), 
                                                BigDecimal.valueOf(10000.00), BigDecimal.valueOf(500.00));
        
        Order order = createOrder(1L, user, BigDecimal.valueOf(10000.00), BigDecimal.valueOf(500.00), 
                                BigDecimal.valueOf(9500.00), orderDate, "High value order");
        order.setOrderItems(new HashSet<>(Collections.singletonList(expensiveItem)));

        // When
        OrderDto result = orderMapper.toOrderDto(order);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(10000.00), result.getTotalAmount());
        assertEquals(BigDecimal.valueOf(500.00), result.getTotalDiscount());
        assertEquals(BigDecimal.valueOf(9500.00), result.getFinalAmount());
        assertEquals(1, result.getItems().size());
        
        OrderItemDto itemDto = result.getItems().get(0);
        assertEquals("Luxury Item", itemDto.getProductName());
        assertEquals(2, itemDto.getQuantity());
        assertEquals(BigDecimal.valueOf(5000.00), itemDto.getUnitPrice());
        assertEquals(BigDecimal.valueOf(10000.00), itemDto.getTotalPrice());
        assertEquals(BigDecimal.valueOf(500.00), itemDto.getDiscountApplied());
    }

    @Test
    void toOrderDto_WithNullNote_ShouldMapCorrectly() {
        // Given
        LocalDateTime orderDate = LocalDateTime.now();
        User user = createUser(1L, "nonote@example.com");
        
        Order order = createOrder(1L, user, BigDecimal.valueOf(50.00), BigDecimal.ZERO, 
                                BigDecimal.valueOf(50.00), orderDate, null);
        order.setOrderItems(new HashSet<>());

        // When
        OrderDto result = orderMapper.toOrderDto(order);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getNote());
        assertEquals(BigDecimal.valueOf(50.00), result.getTotalAmount());
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

    private OrderItem createOrderItem(Long id, Product product, Integer quantity, 
                                    BigDecimal unitPrice, BigDecimal totalPrice, 
                                    BigDecimal discountApplied) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(totalPrice);
        item.setDiscountApplied(discountApplied);
        return item;
    }

    private Order createOrder(Long id, User user, BigDecimal totalAmount, 
                            BigDecimal totalDiscount, BigDecimal finalAmount, 
                            LocalDateTime orderDate, String note) {
        Order order = new Order();
        order.setId(id);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setTotalDiscount(totalDiscount);
        order.setFinalAmount(finalAmount);
        order.setOrderDate(orderDate);
        order.setNote(note);
        return order;
    }
}
