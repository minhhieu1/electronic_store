package com.altech.electronicstore.dto.product;

import com.altech.electronicstore.dto.deal.DealResponseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Boolean availability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DealResponseDto> deals;
}
