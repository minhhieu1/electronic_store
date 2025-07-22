package com.altech.electronicstore.service;

import com.altech.electronicstore.dto.deal.DealDto;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.DealType;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.exception.DuplicateDealException;
import com.altech.electronicstore.exception.ProductNotFoundException;
import com.altech.electronicstore.repository.DealRepository;
import com.altech.electronicstore.repository.DealTypeRepository;
import com.altech.electronicstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealTypeRepository dealTypeRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DealService dealService;

    private Product testProduct;
    private DealType testDealType;
    private Deal testDeal;
    private DealDto testDealDto;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setAvailability(true);

        testDealType = new DealType();
        testDealType.setId(1L);
        testDealType.setName("Percentage Discount");
        testDealType.setDescription("Percentage-based discount");

        testDeal = new Deal();
        testDeal.setId(1L);
        testDeal.setProduct(testProduct);
        testDeal.setDealType(testDealType);
        testDeal.setDiscountPercent(new BigDecimal("10.00"));
        testDeal.setExpirationDate(LocalDateTime.now().plusDays(30));
        testDeal.setMinimumQuantity(1);
        testDeal.setCreatedAt(LocalDateTime.now());

        testDealDto = new DealDto();
        testDealDto.setProductId(1L);
        testDealDto.setDealTypeId(1L);
        testDealDto.setDiscountPercent(new BigDecimal("10.00"));
        testDealDto.setExpirationDate(LocalDateTime.now().plusDays(30));
        testDealDto.setMinimumQuantity(1);
    }

    @Test
    void getActiveDealsForProduct_ShouldReturnActiveDeals() {
        // Given
        Long productId = 1L;
        List<Deal> deals = Arrays.asList(testDeal);
        when(dealRepository.findActiveDealsForProduct(eq(productId), any(LocalDateTime.class)))
                .thenReturn(deals);

        // When
        List<Deal> result = dealService.getActiveDealsForProduct(productId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDeal.getId(), result.get(0).getId());
        verify(dealRepository).findActiveDealsForProduct(eq(productId), any(LocalDateTime.class));
    }

    @Test
    void getActiveDealsForProducts_ShouldReturnGroupedDeals() {
        // Given
        Set<Long> productIds = Set.of(1L, 2L);
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        
        Deal deal2 = new Deal();
        deal2.setId(2L);
        deal2.setProduct(product2);
        deal2.setDealType(testDealType);
        
        List<Deal> allDeals = Arrays.asList(testDeal, deal2);
        when(dealRepository.findActiveDealsForProducts(eq(productIds), any(LocalDateTime.class)))
                .thenReturn(allDeals);

        // When
        Map<Long, List<Deal>> result = dealService.getActiveDealsForProducts(productIds);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
        assertEquals(1, result.get(1L).size());
        assertEquals(1, result.get(2L).size());
        verify(dealRepository).findActiveDealsForProducts(eq(productIds), any(LocalDateTime.class));
    }

    @Test
    void getAllActiveDeals_ShouldReturnAllActiveDeals() {
        // Given
        List<Deal> deals = Arrays.asList(testDeal);
        when(dealRepository.findAllActiveDeals(any(LocalDateTime.class))).thenReturn(deals);

        // When
        List<Deal> result = dealService.getAllActiveDeals();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDeal.getId(), result.get(0).getId());
        verify(dealRepository).findAllActiveDeals(any(LocalDateTime.class));
    }

    @Test
    void getAllDealTypes_ShouldReturnAllDealTypes() {
        // Given
        List<DealType> dealTypes = Arrays.asList(testDealType);
        when(dealTypeRepository.findAll()).thenReturn(dealTypes);

        // When
        List<DealType> result = dealService.getAllDealTypes();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDealType.getId(), result.get(0).getId());
        verify(dealTypeRepository).findAll();
    }

    @Test
    void createDeal_WhenValidData_ShouldCreateDeal() {
        // Given
        when(productRepository.findById(testDealDto.getProductId())).thenReturn(Optional.of(testProduct));
        when(dealTypeRepository.findById(testDealDto.getDealTypeId())).thenReturn(Optional.of(testDealType));
        when(dealRepository.existsByProductIdAndDealTypeIdAndNotExpired(
                eq(testDealDto.getProductId()),
                eq(testDealDto.getDealTypeId()),
                any(LocalDateTime.class),
                eq(null)
        )).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // When
        Deal result = dealService.createDeal(testDealDto);

        // Then
        assertNotNull(result);
        assertEquals(testDeal.getId(), result.getId());
        verify(productRepository).findById(testDealDto.getProductId());
        verify(dealTypeRepository).findById(testDealDto.getDealTypeId());
        verify(dealRepository).existsByProductIdAndDealTypeIdAndNotExpired(
                eq(testDealDto.getProductId()),
                eq(testDealDto.getDealTypeId()),
                any(LocalDateTime.class),
                eq(null)
        );
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void createDeal_WhenProductNotExists_ShouldThrowException() {
        // Given
        when(productRepository.findById(testDealDto.getProductId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> dealService.createDeal(testDealDto));
        verify(productRepository).findById(testDealDto.getProductId());
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void createDeal_WhenDealTypeNotExists_ShouldThrowException() {
        // Given
        when(productRepository.findById(testDealDto.getProductId())).thenReturn(Optional.of(testProduct));
        when(dealTypeRepository.findById(testDealDto.getDealTypeId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> dealService.createDeal(testDealDto));
        verify(dealTypeRepository).findById(testDealDto.getDealTypeId());
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void createDeal_WhenDuplicateDealExists_ShouldThrowDuplicateException() {
        // Given
        when(productRepository.findById(testDealDto.getProductId())).thenReturn(Optional.of(testProduct));
        when(dealTypeRepository.findById(testDealDto.getDealTypeId())).thenReturn(Optional.of(testDealType));
        when(dealRepository.existsByProductIdAndDealTypeIdAndNotExpired(
                eq(testDealDto.getProductId()),
                eq(testDealDto.getDealTypeId()),
                any(LocalDateTime.class),
                eq(null)
        )).thenReturn(true);

        // When & Then
        assertThrows(DuplicateDealException.class, () -> dealService.createDeal(testDealDto));
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void updateDeal_WhenValidData_ShouldUpdateDeal() {
        // Given
        Long dealId = 1L;
        DealDto updateDto = new DealDto();
        updateDto.setDiscountPercent(new BigDecimal("15.00"));
        updateDto.setMinimumQuantity(2);

        when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // When
        Deal result = dealService.updateDeal(dealId, updateDto);

        // Then
        assertNotNull(result);
        verify(dealRepository).findById(dealId);
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void updateDeal_WhenDealNotExists_ShouldThrowException() {
        // Given
        Long dealId = 999L;
        when(dealRepository.findById(dealId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> dealService.updateDeal(dealId, testDealDto));
        verify(dealRepository).findById(dealId);
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void updateDeal_WhenUpdatingToExistingCombination_ShouldThrowDuplicateException() {
        // Given
        Long dealId = 1L;
        Product newProduct = new Product();
        newProduct.setId(2L);
        newProduct.setName("New Product");

        DealDto updateDto = new DealDto();
        updateDto.setProductId(2L); // Change to different product

        when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));
        when(dealRepository.existsByProductIdAndDealTypeIdAndNotExpired(
                eq(2L),
                eq(testDealType.getId()),
                any(LocalDateTime.class),
                eq(dealId)
        )).thenReturn(true);

        // When & Then
        assertThrows(DuplicateDealException.class, () -> dealService.updateDeal(dealId, updateDto));
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void deleteDeal_WhenDealExists_ShouldDeleteDeal() {
        // Given
        Long dealId = 1L;
        when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));

        // When
        dealService.deleteDeal(dealId);

        // Then
        verify(dealRepository).findById(dealId);
        verify(dealRepository).delete(testDeal);
    }

    @Test
    void deleteDeal_WhenDealNotExists_ShouldThrowException() {
        // Given
        Long dealId = 999L;
        when(dealRepository.findById(dealId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> dealService.deleteDeal(dealId));
        verify(dealRepository).findById(dealId);
        verify(dealRepository, never()).delete(any(Deal.class));
    }
}
