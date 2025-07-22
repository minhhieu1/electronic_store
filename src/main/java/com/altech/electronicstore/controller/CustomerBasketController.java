package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.basket.BasketDto;
import com.altech.electronicstore.entity.Basket;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.mapper.BasketMapper;
import com.altech.electronicstore.service.AuthService;
import com.altech.electronicstore.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/customer/basket")
@RequiredArgsConstructor
@Tag(name = "Customer - Basket", description = "Shopping basket endpoints (Customer only)")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerBasketController {

    private final BasketService basketService;
    private final AuthService authService;
    private final BasketMapper basketMapper;

    @GetMapping
    @Operation(summary = "Get active basket", description = "Get current user's active shopping basket")
    @PreAuthorize("@permissionChecker.hasPermission('BASKET', 'READ')")
    public ResponseEntity<BasketDto> getBasket(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Basket basket = basketService.getBasketByUserId(user.getId());
        return ResponseEntity.ok(basketMapper.toBasketDto(basket));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to active basket", description = "Add a product to the active shopping basket")
    @PreAuthorize("@permissionChecker.hasPermission('BASKET', 'UPDATE')")
    public ResponseEntity<BasketDto> addItemToBasket(
            @Parameter(description = "Product ID") @RequestParam Long productId,
            @Parameter(description = "Quantity") @RequestParam @Min(1) Integer quantity,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Basket basket = basketService.addItemToBasket(user.getId(), productId, quantity);
        return ResponseEntity.ok(basketMapper.toBasketDto(basket));
    }

    @PutMapping("/items")
    @Operation(summary = "Update item quantity in active basket", description = "Update the quantity of an item in the active basket")
    @PreAuthorize("@permissionChecker.hasPermission('BASKET', 'UPDATE')")
    public ResponseEntity<BasketDto> updateItemQuantity(
            @Parameter(description = "Product ID") @RequestParam Long productId,
            @Parameter(description = "New quantity") @RequestParam @Min(0) Integer quantity,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Basket basket = basketService.updateItemQuantity(user.getId(), productId, quantity);
        return ResponseEntity.ok(basketMapper.toBasketDto(basket));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from active basket", description = "Remove a product from the active shopping basket")
    @PreAuthorize("@permissionChecker.hasPermission('BASKET', 'UPDATE')")
    public ResponseEntity<BasketDto> removeItemFromBasket(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Basket basket = basketService.removeItemFromBasket(user.getId(), productId);
        return ResponseEntity.ok(basketMapper.toBasketDto(basket));
    }

    @DeleteMapping
    @Operation(summary = "Clear active basket", description = "Remove all items from the active shopping basket")
    @PreAuthorize("@permissionChecker.hasPermission('BASKET', 'DELETE')")
    public ResponseEntity<Void> clearBasket(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        basketService.clearBasket(user.getId());
        return ResponseEntity.noContent().build();
    }
}
