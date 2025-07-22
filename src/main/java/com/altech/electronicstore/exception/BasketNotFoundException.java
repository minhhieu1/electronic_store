package com.altech.electronicstore.exception;

public class BasketNotFoundException extends RuntimeException {
    public BasketNotFoundException(String message) {
        super(message);
    }
    
    public BasketNotFoundException(Long userId) {
        super("Basket not found for user with id: " + userId);
    }
}
