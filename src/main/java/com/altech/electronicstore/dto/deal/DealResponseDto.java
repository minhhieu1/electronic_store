package com.altech.electronicstore.dto.deal;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealResponseDto {
    private Long id;
    private String dealTypeName;
    private String dealTypeDescription;
    private LocalDateTime expirationDate;
    private LocalDateTime createdAt;
    private boolean expired;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private Integer minimumQuantity;
    private Long productId;
    private String productName;
}
