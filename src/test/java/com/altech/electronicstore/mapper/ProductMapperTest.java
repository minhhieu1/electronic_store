package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.deal.DealResponseDto;
import com.altech.electronicstore.dto.product.ProductResponseDto;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock
    private DealMapper dealMapper;

    @InjectMocks
    private ProductMapper productMapper;

    @Test
    void toProductResponseDto_ShouldMapProductToDto() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Deal deal = createDeal(1L, "Test Deal");
        Product product = createProduct(1L, "Test Product", "Electronics", 
                                      BigDecimal.valueOf(100.00), 10, true, now, now);
        product.setDeals(new HashSet<>(Collections.singletonList(deal)));

        DealResponseDto dealDto = createDealResponseDto(1L, "Test Deal");
        when(dealMapper.toDealResponseDto(deal)).thenReturn(dealDto);

        // When
        ProductResponseDto result = productMapper.toProductResponseDto(product);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(BigDecimal.valueOf(100.00), result.getPrice());
        assertEquals(10, result.getStock());
        assertEquals("Electronics", result.getCategory());
        assertEquals(true, result.getAvailability());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
        assertEquals(1, result.getDeals().size());
        assertEquals("Test Deal", result.getDeals().get(0).getDealTypeName());
    }

    @Test
    void toProductResponseDto_WithoutDeals_ShouldMapProductToDtoWithEmptyDeals() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Product product = createProduct(1L, "Test Product", "Electronics", 
                                      BigDecimal.valueOf(50.00), 5, false, now, now);
        product.setDeals(new HashSet<>());

        // When
        ProductResponseDto result = productMapper.toProductResponseDto(product);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals(BigDecimal.valueOf(50.00), result.getPrice());
        assertEquals(5, result.getStock());
        assertEquals("Electronics", result.getCategory());
        assertEquals(false, result.getAvailability());
        assertEquals(0, result.getDeals().size());
    }

    @Test
    void toProductResponseDtoList_ShouldMapListOfProductsToListOfDtos() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Product product1 = createProduct(1L, "Product 1", "Electronics", 
                                       BigDecimal.valueOf(100.00), 10, true, now, now);
        Product product2 = createProduct(2L, "Product 2", "Books", 
                                       BigDecimal.valueOf(25.00), 20, true, now, now);
        product1.setDeals(new HashSet<>());
        product2.setDeals(new HashSet<>());

        List<Product> products = Arrays.asList(product1, product2);

        // When
        List<ProductResponseDto> result = productMapper.toProductResponseDtoList(products);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        ProductResponseDto dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Product 1", dto1.getName());
        assertEquals("Electronics", dto1.getCategory());
        assertEquals(BigDecimal.valueOf(100.00), dto1.getPrice());

        ProductResponseDto dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Product 2", dto2.getName());
        assertEquals("Books", dto2.getCategory());
        assertEquals(BigDecimal.valueOf(25.00), dto2.getPrice());
    }

    @Test
    void toProductResponseDtoList_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        List<Product> emptyList = Collections.emptyList();

        // When
        List<ProductResponseDto> result = productMapper.toProductResponseDtoList(emptyList);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void toProductResponseDto_WithMultipleDeals_ShouldMapAllDeals() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Deal deal1 = createDeal(1L, "Deal 1");
        Deal deal2 = createDeal(2L, "Deal 2");
        Product product = createProduct(1L, "Test Product", "Electronics", 
                                      BigDecimal.valueOf(200.00), 15, true, now, now);
        product.setDeals(new HashSet<>(Arrays.asList(deal1, deal2)));

        DealResponseDto dealDto1 = createDealResponseDto(1L, "Deal 1");
        DealResponseDto dealDto2 = createDealResponseDto(2L, "Deal 2");
        when(dealMapper.toDealResponseDto(deal1)).thenReturn(dealDto1);
        when(dealMapper.toDealResponseDto(deal2)).thenReturn(dealDto2);

        // When
        ProductResponseDto result = productMapper.toProductResponseDto(product);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals(2, result.getDeals().size());
    }

    // Helper methods
    private Product createProduct(Long id, String name, String category, 
                                BigDecimal price, Integer stock, Boolean availability,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Test Description");
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setAvailability(availability);
        product.setCreatedAt(createdAt);
        product.setUpdatedAt(updatedAt);
        return product;
    }

    private Deal createDeal(Long id, String name) {
        Deal deal = new Deal();
        deal.setId(id);
        return deal;
    }

    private DealResponseDto createDealResponseDto(Long id, String dealTypeName) {
        return DealResponseDto.builder()
                .id(id)
                .dealTypeName(dealTypeName)
                .build();
    }
}
