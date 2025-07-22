package com.altech.electronicstore.util.discount;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.service.DealService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscountEngine {

    private final DealService dealService;

    public Map<Long, BigDecimal> calculateDiscountsForBasketItems(List<BasketItem> items) {
        Map<Long, List<Deal>> productDealsMap = getDealsForProducts(items);
        return calculateDiscountsForBasketItems(items, productDealsMap);
    }

    public Map<Long, List<Deal>> getDealsForProducts(List<BasketItem> items) {
        Set<Long> productIds = items.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());
        return dealService.getActiveDealsForProducts(productIds);
    }

    public Map<Long, BigDecimal> calculateDiscountsForBasketItems(List<BasketItem> items, Map<Long, List<Deal>> productDealsMap) {
        Map<Long, BigDecimal> discounts = new HashMap<>();

        for (BasketItem item : items) {
            Long productId = item.getProduct().getId();
            List<Deal> deals = productDealsMap.getOrDefault(productId, Collections.emptyList());
            BigDecimal discount = calculateDiscount(item, deals);
            discounts.put(productId, discount);
        }
        return discounts;
    }

    public BigDecimal calculateDiscount(BasketItem item, List<Deal> deals) {
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (Deal deal : deals) {
            if (deal.isExpired()) continue;
            DiscountStrategy strategy = getStrategy(deal.getDealType().getStrategyClass());
            maxDiscount = maxDiscount.add(strategy.apply(item, deal));
        }
        return maxDiscount;
    }

    private DiscountStrategy getStrategy(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (DiscountStrategy) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid strategy class: " + className);
        }
    }

}
