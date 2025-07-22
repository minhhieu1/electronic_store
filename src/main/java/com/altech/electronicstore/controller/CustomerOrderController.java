package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.order.OrderDto;
import com.altech.electronicstore.entity.Order;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.mapper.OrderMapper;
import com.altech.electronicstore.service.AuthService;
import com.altech.electronicstore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Tag(name = "Customer - Orders", description = "Order management endpoints (Customer only)")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerOrderController {

    private final OrderService orderService;
    private final AuthService authService;
    private final OrderMapper orderMapper;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout active basket", description = "Process checkout and create order from current active basket")
    @PreAuthorize("@permissionChecker.hasPermission('ORDER', 'CREATE')")
    public ResponseEntity<OrderDto> checkout(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Order order = orderService.checkout(user.getId());
        OrderDto orderDto = orderMapper.toOrderDto(order);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping
    @Operation(summary = "Get order history", description = "Get paginated list of user's orders")
    @PreAuthorize("@permissionChecker.hasPermission('ORDER', 'READ')")
    public ResponseEntity<Page<OrderDto>> getOrderHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.getOrdersByUserId(user.getId(), pageable);
        Page<OrderDto> orderDtos = orders.map(orderMapper::toOrderDto);
        return ResponseEntity.ok(orderDtos);
    }
}
