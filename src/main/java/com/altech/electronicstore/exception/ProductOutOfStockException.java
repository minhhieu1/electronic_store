package com.altech.electronicstore.exception;

public class ProductOutOfStockException extends RuntimeException {
    
    public ProductOutOfStockException(Long productId) {
        super(String.format("Product with ID %d is out of stock", productId));
    }
    
    public ProductOutOfStockException(String productName) {
        super(String.format("Product '%s' is out of stock", productName));
    }
}
