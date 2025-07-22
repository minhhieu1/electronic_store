package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.deal.DealResponseDto;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.DealType;
import com.altech.electronicstore.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DealMapperTest {

    @InjectMocks
    private DealMapper dealMapper;

    @Test
    void toDealResponseDto_ShouldMapDealToDto() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusDays(30);
        
        Product product = createProduct(1L, "Test Product");
        DealType dealType = createDealType(1L, "Flash Sale", "Limited time offer");
        Deal deal = createDeal(1L, dealType, product, expirationDate, now, 
                             BigDecimal.valueOf(20.00), BigDecimal.valueOf(10.00), 2);

        // When
        DealResponseDto result = dealMapper.toDealResponseDto(deal);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Flash Sale", result.getDealTypeName());
        assertEquals("Limited time offer", result.getDealTypeDescription());
        assertEquals(expirationDate, result.getExpirationDate());
        assertEquals(now, result.getCreatedAt());
        assertFalse(result.isExpired());
        assertEquals(BigDecimal.valueOf(20.00), result.getDiscountPercent());
        assertEquals(BigDecimal.valueOf(10.00), result.getDiscountAmount());
        assertEquals(2, result.getMinimumQuantity());
        assertEquals(1L, result.getProductId());
        assertEquals("Test Product", result.getProductName());
    }

    @Test
    void toDealResponseDto_WithExpiredDeal_ShouldMarkAsExpired() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastExpirationDate = now.minusDays(1); // Expired yesterday
        
        Product product = createProduct(2L, "Expired Product");
        DealType dealType = createDealType(1L, "Expired Sale", "Old offer");
        Deal deal = createDeal(1L, dealType, product, pastExpirationDate, now.minusDays(10), 
                             BigDecimal.valueOf(15.00), BigDecimal.valueOf(5.00), 1);

        // When
        DealResponseDto result = dealMapper.toDealResponseDto(deal);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Expired Sale", result.getDealTypeName());
        assertEquals(pastExpirationDate, result.getExpirationDate());
        assertTrue(result.isExpired());
        assertEquals(2L, result.getProductId());
        assertEquals("Expired Product", result.getProductName());
    }

    @Test
    void toDealResponseDtoList_ShouldMapListOfDealsToListOfDtos() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(15);
        LocalDateTime pastDate = now.minusDays(5);
        
        Product product1 = createProduct(1L, "Product 1");
        Product product2 = createProduct(2L, "Product 2");
        DealType dealType1 = createDealType(1L, "Deal 1", "Description 1");
        DealType dealType2 = createDealType(2L, "Deal 2", "Description 2");
        
        Deal deal1 = createDeal(1L, dealType1, product1, futureDate, now, 
                              BigDecimal.valueOf(25.00), BigDecimal.valueOf(15.00), 1);
        Deal deal2 = createDeal(2L, dealType2, product2, pastDate, now, 
                              BigDecimal.valueOf(50.00), BigDecimal.valueOf(25.00), 3);
        
        List<Deal> deals = Arrays.asList(deal1, deal2);

        // When
        List<DealResponseDto> result = dealMapper.toDealResponseDtoList(deals);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        DealResponseDto dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Deal 1", dto1.getDealTypeName());
        assertFalse(dto1.isExpired());
        assertEquals(1L, dto1.getProductId());

        DealResponseDto dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Deal 2", dto2.getDealTypeName());
        assertTrue(dto2.isExpired());
        assertEquals(2L, dto2.getProductId());
    }

    @Test
    void toDealResponseDtoList_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        List<Deal> emptyList = Collections.emptyList();

        // When
        List<DealResponseDto> result = dealMapper.toDealResponseDtoList(emptyList);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void toDealResponseDto_WithZeroDiscountValue_ShouldMapCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Product product = createProduct(3L, "Free Shipping Product");
        DealType dealType = createDealType(1L, "Free Shipping", "No discount on price");
        Deal deal = createDeal(1L, dealType, product, now.plusDays(7), now, 
                             BigDecimal.ZERO, BigDecimal.ZERO, 1);

        // When
        DealResponseDto result = dealMapper.toDealResponseDto(deal);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Free Shipping", result.getDealTypeName());
        assertFalse(result.isExpired());
        assertEquals(BigDecimal.ZERO, result.getDiscountPercent());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals(3L, result.getProductId());
    }

    @Test
    void toDealResponseDto_WithHighDiscountPercentage_ShouldMapCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Product product = createProduct(4L, "Luxury Product");
        DealType dealType = createDealType(1L, "Mega Sale", "Up to 90% off");
        Deal deal = createDeal(1L, dealType, product, now.plusHours(12), now, 
                             BigDecimal.valueOf(90.00), BigDecimal.valueOf(100.00), 5);

        // When
        DealResponseDto result = dealMapper.toDealResponseDto(deal);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Mega Sale", result.getDealTypeName());
        assertFalse(result.isExpired());
        assertEquals(BigDecimal.valueOf(90.00), result.getDiscountPercent());
        assertEquals(BigDecimal.valueOf(100.00), result.getDiscountAmount());
        assertEquals(5, result.getMinimumQuantity());
        assertEquals(4L, result.getProductId());
    }

    // Helper methods
    private Deal createDeal(Long id, DealType dealType, Product product, LocalDateTime expirationDate,
                           LocalDateTime createdAt, BigDecimal discountPercent, BigDecimal discountAmount,
                           Integer minimumQuantity) {
        Deal deal = new Deal();
        deal.setId(id);
        deal.setDealType(dealType);
        deal.setProduct(product);
        deal.setExpirationDate(expirationDate);
        deal.setCreatedAt(createdAt);
        deal.setDiscountPercent(discountPercent);
        deal.setDiscountAmount(discountAmount);
        deal.setMinimumQuantity(minimumQuantity);
        return deal;
    }

    private DealType createDealType(Long id, String name, String description) {
        DealType dealType = new DealType();
        dealType.setId(id);
        dealType.setName(name);
        dealType.setDescription(description);
        return dealType;
    }

    private Product createProduct(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }
}
