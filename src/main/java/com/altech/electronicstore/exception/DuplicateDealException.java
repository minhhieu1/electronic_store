package com.altech.electronicstore.exception;

public class DuplicateDealException extends RuntimeException {
    public DuplicateDealException(Long productId, Long dealTypeId) {
        super("Deal already exists for product ID: " + productId + " with deal type ID: " + dealTypeId);
    }
    
    public DuplicateDealException(String productName, String dealTypeName) {
        super("Deal already exists for product '" + productName + "' with deal type '" + dealTypeName + "'");
    }
}
