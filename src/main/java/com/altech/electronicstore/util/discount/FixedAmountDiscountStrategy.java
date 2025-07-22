package com.altech.electronicstore.util.discount;

import java.math.BigDecimal;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;

public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal apply(BasketItem item, Deal deal) {
        int minQuantity = deal.getMinimumQuantity() != null ? deal.getMinimumQuantity() : 1;
        if (item.getQuantity() < minQuantity) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountAmount = deal.getDiscountAmount();
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate total price for the item
        BigDecimal totalPrice = item.getProduct().getPrice()
            .multiply(new BigDecimal(item.getQuantity()));
        
        // Apply fixed discount, but don't exceed the total price
        BigDecimal finalDiscount = discountAmount.min(totalPrice);
        
        return finalDiscount;
    }
}
