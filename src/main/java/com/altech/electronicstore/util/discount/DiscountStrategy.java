package com.altech.electronicstore.util.discount;

import java.math.BigDecimal;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;

public interface DiscountStrategy {
        BigDecimal apply(BasketItem item, Deal deal);
}
