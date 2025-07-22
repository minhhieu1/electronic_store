package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.deal.DealResponseDto;
import com.altech.electronicstore.dto.deal.DealTypeResponseDto;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.DealType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DealMapper {

    public DealResponseDto toDealResponseDto(Deal deal) {
        return DealResponseDto.builder()
                .id(deal.getId())
                .dealTypeName(deal.getDealType().getName())
                .dealTypeDescription(deal.getDealType().getDescription())
                .expirationDate(deal.getExpirationDate())
                .createdAt(deal.getCreatedAt())
                .expired(deal.getExpirationDate().isBefore(LocalDateTime.now()))
                .discountPercent(deal.getDiscountPercent())
                .discountAmount(deal.getDiscountAmount())
                .minimumQuantity(deal.getMinimumQuantity())
                .productId(deal.getProduct().getId())
                .productName(deal.getProduct().getName())
                .build();
    }

    public DealTypeResponseDto toDealTypeResponseDto(DealType dealType) {
        return DealTypeResponseDto.builder()
                .id(dealType.getId())
                .name(dealType.getName())
                .description(dealType.getDescription())
                .strategyClass(dealType.getStrategyClass())
                .build();
    }

    public List<DealResponseDto> toDealResponseDtoList(List<Deal> deals) {
        return deals.stream()
                .map(this::toDealResponseDto)
                .collect(Collectors.toList());
    }

    public List<DealTypeResponseDto> toDealTypeResponseDtoList(List<DealType> dealTypes) {
        return dealTypes.stream()
                .map(this::toDealTypeResponseDto)
                .collect(Collectors.toList());
    }
}
