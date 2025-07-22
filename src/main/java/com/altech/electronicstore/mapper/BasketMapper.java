package com.altech.electronicstore.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.altech.electronicstore.dto.basket.BasketDto;
import com.altech.electronicstore.dto.basket.BasketItemDto;
import com.altech.electronicstore.entity.Basket;
import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.util.discount.DiscountEngine;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BasketMapper {

    private final DiscountEngine discountEngine;

    public BasketDto toBasketDto(Basket basket) {
        BasketDto dto = new BasketDto();
        dto.setId(basket.getId());
        dto.setStatus(basket.getStatus().name());
        Map<Long, BigDecimal> discounts = discountEngine.calculateDiscountsForBasketItems(
            basket.getBasketItems().stream().collect(Collectors.toList())
        );


        List<BasketItemDto> itemDtos = basket.getBasketItems().stream()
                .map(item -> convertItemToDto(item, discounts.get(item.getProduct().getId())))
                .collect(Collectors.toList());

        dto.setItems(itemDtos);
        dto.setTotalItems(itemDtos.stream().mapToInt(BasketItemDto::getQuantity).sum());

        BigDecimal totalDiscount = discounts.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = itemDtos.stream()
            .map(BasketItemDto::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(totalPrice.subtract(totalDiscount));
        
        return dto;
    }

    private BasketItemDto convertItemToDto(BasketItem item, BigDecimal discounts) {
        BasketItemDto dto = new BasketItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setUnitPrice(item.getProduct().getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setTotalPrice(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        dto.setDiscountApplied(discounts);
        return dto;
    }

}
