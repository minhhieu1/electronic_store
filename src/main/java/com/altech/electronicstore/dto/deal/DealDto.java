package com.altech.electronicstore.dto.deal;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DealDto {
    private Long id;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Deal type ID is required")
    private Long dealTypeId;
    
    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expirationDate;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount percent must be greater than 0")
    @DecimalMax(value = "100.0", message = "Discount percent cannot exceed 100")
    private BigDecimal discountPercent;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount amount must be greater than 0")
    private BigDecimal discountAmount;
    
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minimumQuantity;
}
