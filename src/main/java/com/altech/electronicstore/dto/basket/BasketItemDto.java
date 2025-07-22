package com.altech.electronicstore.dto.basket;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

@Data
public class BasketItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private BigDecimal totalPrice;

    private BigDecimal discountApplied;

}
