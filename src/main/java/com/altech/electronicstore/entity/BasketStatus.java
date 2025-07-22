package com.altech.electronicstore.entity;

/**
 * Enum representing the different states of a basket
 */
public enum BasketStatus {
    /**
     * Basket is active and can be modified by the user
     */
    ACTIVE,
    
    /**
     * Basket has been checked out and converted to an order
     */
    CHECKED_OUT,
    
    /**
     * Basket has expired and is no longer valid
     */
    EXPIRED
}
