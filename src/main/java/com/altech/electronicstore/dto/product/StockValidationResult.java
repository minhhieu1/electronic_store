package com.altech.electronicstore.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockValidationResult {
    private boolean hasStock;
    private Integer currentStock;
    private boolean isAvailable;
}
