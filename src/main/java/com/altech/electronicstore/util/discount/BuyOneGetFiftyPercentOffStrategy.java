package com.altech.electronicstore.util.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;

public class BuyOneGetFiftyPercentOffStrategy implements DiscountStrategy {

    @Override
    public BigDecimal apply(BasketItem item, Deal deal) {
        int minQuantity = deal.getMinimumQuantity() != null ? deal.getMinimumQuantity() : 2;
        
        if (item.getQuantity() < minQuantity) {
            return BigDecimal.ZERO;
        }
        
        int discountedItems = item.getQuantity() / minQuantity;
        
        BigDecimal discountPercent = deal.getDiscountPercent() != null 
            ? deal.getDiscountPercent() 
            : new BigDecimal("50.00");
        
        // Calculate discount amount per item
        BigDecimal itemPrice = item.getProduct().getPrice();
        BigDecimal discountPerItem = itemPrice
            .multiply(discountPercent)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // Total discount = discount per item * number of discounted items
        BigDecimal totalDiscount = discountPerItem.multiply(new BigDecimal(discountedItems));
        
        return totalDiscount;
    }
}
