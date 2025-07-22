package com.altech.electronicstore.exception;

public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableStock) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d", 
              productId, requestedQuantity, availableStock));
    }
    
    public InsufficientStockException(String productName, Integer requestedQuantity, Integer availableStock) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d", 
              productName, requestedQuantity, availableStock));
    }
}
