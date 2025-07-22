package com.altech.electronicstore.util.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;

public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal apply(BasketItem item, Deal deal) {
        int minQuantity = deal.getMinimumQuantity() != null ? deal.getMinimumQuantity() : 1;
        if (item.getQuantity() < minQuantity) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountPercent = deal.getDiscountPercent();
        if (discountPercent == null) {
            return BigDecimal.ZERO;
        }
        
        // For zero discount, return BigDecimal.ZERO to match test expectations
        if (discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // For negative discount, return BigDecimal.ZERO for PercentageDiscountStrategy
        // (negative discounts don't make sense for percentage-based discounts)
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate total price for the item
        BigDecimal totalPrice = item.getProduct().getPrice()
            .multiply(new BigDecimal(item.getQuantity()));
        
        // Calculate percentage discount
        BigDecimal discountAmount = totalPrice
            .multiply(discountPercent)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        return discountAmount;
    }
}
