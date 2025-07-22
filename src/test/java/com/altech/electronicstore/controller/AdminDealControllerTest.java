package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.deal.DealDto;
import com.altech.electronicstore.dto.deal.DealResponseDto;
import com.altech.electronicstore.dto.deal.DealTypeResponseDto;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.DealType;
import com.altech.electronicstore.mapper.DealMapper;
import com.altech.electronicstore.service.DealService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDealControllerTest {

    @Mock
    private DealService dealService;

    @Mock
    private DealMapper dealMapper;

    @InjectMocks
    private AdminDealController adminDealController;

    @Test
    void getAllActiveDeals_ShouldReturnListOfDeals() {
        // Given
        Deal deal1 = createDeal(1L, BigDecimal.valueOf(20.0));
        Deal deal2 = createDeal(2L, BigDecimal.valueOf(15.0));
        List<Deal> deals = Arrays.asList(deal1, deal2);

        DealResponseDto dealDto1 = createDealResponseDto(1L, "Electronics Deal", BigDecimal.valueOf(20.0));
        DealResponseDto dealDto2 = createDealResponseDto(2L, "Books Deal", BigDecimal.valueOf(15.0));
        List<DealResponseDto> dealDtos = Arrays.asList(dealDto1, dealDto2);

        when(dealService.getAllActiveDeals()).thenReturn(deals);
        when(dealMapper.toDealResponseDtoList(deals)).thenReturn(dealDtos);

        // When
        ResponseEntity<List<DealResponseDto>> response = adminDealController.getAllActiveDeals();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Electronics Deal", response.getBody().get(0).getProductName());
        assertEquals("Books Deal", response.getBody().get(1).getProductName());

        verify(dealService).getAllActiveDeals();
        verify(dealMapper).toDealResponseDtoList(deals);
    }

    @Test
    void getAllActiveDeals_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        when(dealService.getAllActiveDeals()).thenReturn(Collections.emptyList());
        when(dealMapper.toDealResponseDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<DealResponseDto>> response = adminDealController.getAllActiveDeals();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(dealService).getAllActiveDeals();
        verify(dealMapper).toDealResponseDtoList(Collections.emptyList());
    }

    @Test
    void getAllDealTypes_ShouldReturnListOfDealTypes() {
        // Given
        DealType dealType1 = createDealType(1L, "PERCENTAGE");
        DealType dealType2 = createDealType(2L, "FIXED_AMOUNT");
        List<DealType> dealTypes = Arrays.asList(dealType1, dealType2);

        DealTypeResponseDto dealTypeDto1 = createDealTypeResponseDto(1L, "PERCENTAGE");
        DealTypeResponseDto dealTypeDto2 = createDealTypeResponseDto(2L, "FIXED_AMOUNT");
        List<DealTypeResponseDto> dealTypeDtos = Arrays.asList(dealTypeDto1, dealTypeDto2);

        when(dealService.getAllDealTypes()).thenReturn(dealTypes);
        when(dealMapper.toDealTypeResponseDtoList(dealTypes)).thenReturn(dealTypeDtos);

        // When
        ResponseEntity<List<DealTypeResponseDto>> response = adminDealController.getAllDealTypes();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("PERCENTAGE", response.getBody().get(0).getName());
        assertEquals("FIXED_AMOUNT", response.getBody().get(1).getName());

        verify(dealService).getAllDealTypes();
        verify(dealMapper).toDealTypeResponseDtoList(dealTypes);
    }

    @Test
    void getDealsForProduct_WithValidProductId_ShouldReturnDeals() {
        // Given
        Long productId = 1L;
        Deal deal = createDeal(1L, BigDecimal.valueOf(25.0));
        List<Deal> deals = Collections.singletonList(deal);

        DealResponseDto dealDto = createDealResponseDto(1L, "Product Deal", BigDecimal.valueOf(25.0));
        List<DealResponseDto> dealDtos = Collections.singletonList(dealDto);

        when(dealService.getActiveDealsForProduct(productId)).thenReturn(deals);
        when(dealMapper.toDealResponseDtoList(deals)).thenReturn(dealDtos);

        // When
        ResponseEntity<List<DealResponseDto>> response = adminDealController.getDealsForProduct(productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Product Deal", response.getBody().get(0).getProductName());

        verify(dealService).getActiveDealsForProduct(productId);
        verify(dealMapper).toDealResponseDtoList(deals);
    }

    @Test
    void getDealsForProduct_WithNoDeals_ShouldReturnEmptyList() {
        // Given
        Long productId = 1L;

        when(dealService.getActiveDealsForProduct(productId)).thenReturn(Collections.emptyList());
        when(dealMapper.toDealResponseDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<DealResponseDto>> response = adminDealController.getDealsForProduct(productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(dealService).getActiveDealsForProduct(productId);
        verify(dealMapper).toDealResponseDtoList(Collections.emptyList());
    }

    @Test
    void createDeal_WithValidDealDto_ShouldReturnCreatedDeal() {
        // Given
        DealDto dealDto = createDealDto(1L, BigDecimal.valueOf(30.0));
        Deal createdDeal = createDeal(1L, BigDecimal.valueOf(30.0));
        DealResponseDto dealResponseDto = createDealResponseDto(1L, "New Deal", BigDecimal.valueOf(30.0));

        when(dealService.createDeal(any(DealDto.class))).thenReturn(createdDeal);
        when(dealMapper.toDealResponseDto(createdDeal)).thenReturn(dealResponseDto);

        // When
        ResponseEntity<DealResponseDto> response = adminDealController.createDeal(dealDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Deal", response.getBody().getProductName());
        assertEquals(BigDecimal.valueOf(30.0), response.getBody().getDiscountPercent());

        verify(dealService).createDeal(any(DealDto.class));
        verify(dealMapper).toDealResponseDto(createdDeal);
    }

    @Test
    void updateDeal_WithValidIdAndDto_ShouldReturnUpdatedDeal() {
        // Given
        Long dealId = 1L;
        DealDto dealDto = createDealDto(1L, BigDecimal.valueOf(35.0));
        Deal updatedDeal = createDeal(dealId, BigDecimal.valueOf(35.0));
        DealResponseDto dealResponseDto = createDealResponseDto(dealId, "Updated Deal", BigDecimal.valueOf(35.0));

        when(dealService.updateDeal(eq(dealId), any(DealDto.class))).thenReturn(updatedDeal);
        when(dealMapper.toDealResponseDto(updatedDeal)).thenReturn(dealResponseDto);

        // When
        ResponseEntity<DealResponseDto> response = adminDealController.updateDeal(dealId, dealDto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Deal", response.getBody().getProductName());
        assertEquals(BigDecimal.valueOf(35.0), response.getBody().getDiscountPercent());

        verify(dealService).updateDeal(eq(dealId), any(DealDto.class));
        verify(dealMapper).toDealResponseDto(updatedDeal);
    }

    @Test
    void updateDeal_WithInvalidId_ShouldThrowException() {
        // Given
        Long dealId = 999L;
        DealDto dealDto = createDealDto(1L, BigDecimal.valueOf(35.0));

        when(dealService.updateDeal(eq(dealId), any(DealDto.class)))
                .thenThrow(new RuntimeException("Deal not found with id: " + dealId));

        // When & Then
        try {
            adminDealController.updateDeal(dealId, dealDto);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Deal not found with id: " + dealId, e.getMessage());
        }

        verify(dealService).updateDeal(eq(dealId), any(DealDto.class));
        verify(dealMapper, never()).toDealResponseDto(any());
    }

    @Test
    void deleteDeal_WithValidId_ShouldReturnNoContent() {
        // Given
        Long dealId = 1L;
        doNothing().when(dealService).deleteDeal(dealId);

        // When
        ResponseEntity<Void> response = adminDealController.deleteDeal(dealId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(dealService).deleteDeal(dealId);
    }

    @Test
    void deleteDeal_WithInvalidId_ShouldThrowException() {
        // Given
        Long dealId = 999L;
        doThrow(new RuntimeException("Deal not found with id: " + dealId))
                .when(dealService).deleteDeal(dealId);

        // When & Then
        try {
            adminDealController.deleteDeal(dealId);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Deal not found with id: " + dealId, e.getMessage());
        }

        verify(dealService).deleteDeal(dealId);
    }

    // Helper methods
    private Deal createDeal(Long id, BigDecimal discountPercent) {
        Deal deal = new Deal();
        deal.setId(id);
        deal.setDiscountPercent(discountPercent);
        deal.setExpirationDate(LocalDateTime.now().plusDays(30));
        deal.setCreatedAt(LocalDateTime.now());
        return deal;
    }

    private DealResponseDto createDealResponseDto(Long id, String productName, BigDecimal discountPercent) {
        return DealResponseDto.builder()
                .id(id)
                .productName(productName)
                .discountPercent(discountPercent)
                .expirationDate(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .expired(false)
                .build();
    }

    private DealDto createDealDto(Long productId, BigDecimal discountPercent) {
        DealDto dealDto = new DealDto();
        dealDto.setProductId(productId);
        dealDto.setDealTypeId(1L);
        dealDto.setDiscountPercent(discountPercent);
        dealDto.setExpirationDate(LocalDateTime.now().plusDays(30));
        return dealDto;
    }

    private DealType createDealType(Long id, String name) {
        DealType dealType = new DealType();
        dealType.setId(id);
        dealType.setName(name);
        return dealType;
    }

    private DealTypeResponseDto createDealTypeResponseDto(Long id, String name) {
        return DealTypeResponseDto.builder()
                .id(id)
                .name(name)
                .build();
    }
}
