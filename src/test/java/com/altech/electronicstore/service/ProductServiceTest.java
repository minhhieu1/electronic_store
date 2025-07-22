package com.altech.electronicstore.service;

import com.altech.electronicstore.dto.product.ProductDto;
import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.exception.ProductNotFoundException;
import com.altech.electronicstore.repository.ProductRepository;
import com.altech.electronicstore.dto.product.StockValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(10);
        testProduct.setCategory("Electronics");
        testProduct.setAvailability(true);
        testProduct.setCreatedAt(LocalDateTime.now());

        testProductDto = new ProductDto();
        testProductDto.setName("Test Product");
        testProductDto.setDescription("Test Description");
        testProductDto.setPrice(new BigDecimal("99.99"));
        testProductDto.setStock(10);
        testProductDto.setCategory("Electronics");
        testProductDto.setAvailability(true);
    }

    @Test
    void getAllProducts_ShouldReturnPageOfAvailableProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productRepository.findByAvailabilityTrue(pageable)).thenReturn(productPage);

        // When
        Page<Product> result = productService.getAllProducts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getName(), result.getContent().get(0).getName());
        verify(productRepository).findByAvailabilityTrue(pageable);
    }

    @Test
    void getFilteredProducts_ShouldReturnFilteredProducts() {
        // Given
        String category = "Electronics";
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");
        Boolean availability = true;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productRepository.findFilteredProducts(category, minPrice, maxPrice, availability, pageable))
                .thenReturn(productPage);

        // When
        Page<Product> result = productService.getFilteredProducts(category, minPrice, maxPrice, availability, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findFilteredProducts(category, minPrice, maxPrice, availability, pageable);
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Product result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.createProduct(testProductDto);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateProduct() {
        // Given
        Long productId = 1L;
        ProductDto updateDto = new ProductDto();
        updateDto.setName("Updated Product");
        updateDto.setPrice(new BigDecimal("149.99"));
        updateDto.setStock(15);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.updateProduct(productId, updateDto);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductNotExists_ShouldThrowException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productId, testProductDto));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).delete(testProduct);
    }

    @Test
    void deleteProduct_WhenProductNotExists_ShouldThrowException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(productId));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void canReserveStock_WhenProductAvailableWithSufficientStock_ShouldReturnTrue() {
        // Given
        Long productId = 1L;
        Integer quantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.canReserveStock(productId, quantity);

        // Then
        assertTrue(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void canReserveStock_WhenProductNotAvailable_ShouldReturnFalse() {
        // Given
        Long productId = 1L;
        Integer quantity = 5;
        testProduct.setAvailability(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.canReserveStock(productId, quantity);

        // Then
        assertFalse(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void canReserveStock_WhenInsufficientStock_ShouldReturnFalse() {
        // Given
        Long productId = 1L;
        Integer quantity = 15; // More than available stock (10)
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.canReserveStock(productId, quantity);

        // Then
        assertFalse(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void canReserveStock_WhenProductNotExists_ShouldReturnFalse() {
        // Given
        Long productId = 999L;
        Integer quantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        boolean result = productService.canReserveStock(productId, quantity);

        // Then
        assertFalse(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void validateAndGetStock_WhenProductExistsWithSufficientStock_ShouldReturnValidResult() {
        // Given
        Long productId = 1L;
        Integer requiredQuantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        StockValidationResult result = productService.validateAndGetStock(productId, requiredQuantity);

        // Then
        assertNotNull(result);
        assertTrue(result.isHasStock());
        assertEquals(10, result.getCurrentStock());
        assertTrue(result.isAvailable());
        verify(productRepository).findById(productId);
    }

    @Test
    void validateAndGetStock_WhenProductNotExists_ShouldReturnInvalidResult() {
        // Given
        Long productId = 999L;
        Integer requiredQuantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        StockValidationResult result = productService.validateAndGetStock(productId, requiredQuantity);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasStock());
        assertEquals(0, result.getCurrentStock());
        assertFalse(result.isAvailable());
        verify(productRepository).findById(productId);
    }

    @Test
    void commitStockReduction_ShouldReduceStockForAllItems() {
        // Given
        Product product1 = new Product();
        product1.setId(1L);
        product1.setStock(10);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setStock(15);

        BasketItem item1 = new BasketItem();
        item1.setProduct(product1);
        item1.setQuantity(3);

        BasketItem item2 = new BasketItem();
        item2.setProduct(product2);
        item2.setQuantity(5);

        List<BasketItem> basketItems = Arrays.asList(item1, item2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

        // When
        productService.commitStockReduction(basketItems);

        // Then
        assertEquals(7, product1.getStock()); // 10 - 3
        assertEquals(10, product2.getStock()); // 15 - 5
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void releaseStock_ShouldIncreaseStock() {
        // Given
        Long productId = 1L;
        Integer quantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        productService.releaseStock(productId, quantity);

        // Then
        assertEquals(15, testProduct.getStock()); // 10 + 5
        verify(productRepository).save(testProduct);
    }

    @Test
    void hasStock_WhenProductHasSufficientStock_ShouldReturnTrue() {
        // Given
        Long productId = 1L;
        Integer requiredQuantity = 5;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.hasStock(productId, requiredQuantity);

        // Then
        assertTrue(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void hasStock_WhenProductHasInsufficientStock_ShouldReturnFalse() {
        // Given
        Long productId = 1L;
        Integer requiredQuantity = 15; // More than available
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.hasStock(productId, requiredQuantity);

        // Then
        assertFalse(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void getCurrentStock_WhenProductExists_ShouldReturnStock() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Integer result = productService.getCurrentStock(productId);

        // Then
        assertEquals(10, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void getCurrentStock_WhenProductNotExists_ShouldReturnZero() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        Integer result = productService.getCurrentStock(productId);

        // Then
        assertEquals(0, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void getCategories_ShouldReturnDistinctCategories() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Clothing", "Books");
        when(productRepository.findDistinctCategories()).thenReturn(categories);

        // When
        List<String> result = productService.getCategories();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Electronics"));
        assertTrue(result.contains("Clothing"));
        assertTrue(result.contains("Books"));
        verify(productRepository).findDistinctCategories();
    }
}
