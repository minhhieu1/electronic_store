package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.product.ProductResponseDto;
import com.altech.electronicstore.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final DealMapper dealMapper;

    public ProductResponseDto toProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .availability(product.getAvailability())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .deals(product.getDeals().stream()
                        .map(dealMapper::toDealResponseDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<ProductResponseDto> toProductResponseDtoList(List<Product> products) {
        return products.stream()
                .map(this::toProductResponseDto)
                .collect(Collectors.toList());
    }
}
