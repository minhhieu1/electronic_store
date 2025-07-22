package com.altech.electronicstore.dto.basket;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BasketDto {
    private Long id;
    private String status;
    private List<BasketItemDto> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
}
