package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.product.ProductResponseDto;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.exception.ProductNotFoundException;
import com.altech.electronicstore.mapper.ProductMapper;
import com.altech.electronicstore.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductController productController;

    @Test
    void testGetAllProductsWithAllParams() {
        // Input parameters
        int page = 0;
        int size = 2;
        String sortBy = "name";
        String sortDir = "asc";
        String category = "Laptop";
        BigDecimal minPrice = new BigDecimal("500.00");
        BigDecimal maxPrice = new BigDecimal("1500.00");
        Boolean availability = true;

        // Prepare mock data
        Product product1 = new Product();
        Product product2 = new Product();

        List<Product> products = List.of(product1, product2);
        Page<Product> productPage = new PageImpl<>(products);
        ProductResponseDto dto1 = new ProductResponseDto();
        ProductResponseDto dto2 = new ProductResponseDto();

        // Mocking
        when(productService.getFilteredProducts(eq(category), eq(minPrice), eq(maxPrice), eq(availability), any()))
            .thenReturn(productPage);
        when(productMapper.toProductResponseDto(product1)).thenReturn(dto1);
        when(productMapper.toProductResponseDto(product2)).thenReturn(dto2);

        // Call controller method
        ResponseEntity<Page<ProductResponseDto>> response = productController.getAllProducts(
            page, size, sortBy, sortDir, category, minPrice, maxPrice, availability
        );

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    void getAllProducts_WithDefaultParameters_ShouldReturnPagedProducts() {
        // Given
        Product product = createProduct(1L, "Test Product", "Electronics", BigDecimal.valueOf(100.00));
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        ProductResponseDto productDto = createProductResponseDto(1L, "Test Product", "Electronics", BigDecimal.valueOf(100.00));
        
        when(productService.getFilteredProducts(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toProductResponseDto(any(Product.class))).thenReturn(productDto);

        // When
        ResponseEntity<Page<ProductResponseDto>> response = productController.getAllProducts(
                0, 10, "name", "asc", null, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Test Product", response.getBody().getContent().get(0).getName());
        assertEquals("Electronics", response.getBody().getContent().get(0).getCategory());
        assertEquals(BigDecimal.valueOf(100.00), response.getBody().getContent().get(0).getPrice());

        verify(productService).getFilteredProducts(any(), any(), any(), any(), any(Pageable.class));
        verify(productMapper).toProductResponseDto(any(Product.class));
    }

    @Test
    void getAllProducts_WithCustomParameters_ShouldReturnFilteredProducts() {
        // Given
        Product product = createProduct(1L, "Laptop", "Electronics", BigDecimal.valueOf(1200.00));
        ProductResponseDto productDto = createProductResponseDto(1L, "Laptop", "Electronics", BigDecimal.valueOf(1200.00));
        
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        
        when(productService.getFilteredProducts(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toProductResponseDto(any(Product.class))).thenReturn(productDto);

        // When
        ResponseEntity<Page<ProductResponseDto>> response = productController.getAllProducts(
                0, 5, "price", "desc", "Electronics", 
                BigDecimal.valueOf(1000), BigDecimal.valueOf(2000), true);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Laptop", response.getBody().getContent().get(0).getName());
        assertEquals("Electronics", response.getBody().getContent().get(0).getCategory());
        assertEquals(BigDecimal.valueOf(1200.00), response.getBody().getContent().get(0).getPrice());

        verify(productService).getFilteredProducts(any(), any(), any(), any(), any(Pageable.class));
        verify(productMapper).toProductResponseDto(any(Product.class));
    }

    @Test
    void getAllProducts_WithEmptyResult_ShouldReturnEmptyPage() {
        // Given
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        
        when(productService.getFilteredProducts(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<Page<ProductResponseDto>> response = productController.getAllProducts(
                0, 10, "name", "asc", null, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getContent().size());
        assertEquals(0, response.getBody().getTotalElements());

        verify(productService).getFilteredProducts(any(), any(), any(), any(), any(Pageable.class));
        verify(productMapper, never()).toProductResponseDto(any());
    }

    @Test
    void getAllProducts_WithMultipleProducts_ShouldReturnAllProducts() {
        // Given
        Product product1 = createProduct(1L, "Laptop", "Electronics", BigDecimal.valueOf(1200.00));
        Product product2 = createProduct(2L, "Mouse", "Electronics", BigDecimal.valueOf(50.00));
        
        ProductResponseDto productDto1 = createProductResponseDto(1L, "Laptop", "Electronics", BigDecimal.valueOf(1200.00));
        ProductResponseDto productDto2 = createProductResponseDto(2L, "Mouse", "Electronics", BigDecimal.valueOf(50.00));
        
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1, product2));
        
        when(productService.getFilteredProducts(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toProductResponseDto(product1)).thenReturn(productDto1);
        when(productMapper.toProductResponseDto(product2)).thenReturn(productDto2);

        // When
        ResponseEntity<Page<ProductResponseDto>> response = productController.getAllProducts(
                0, 10, "name", "asc", null, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("Laptop", response.getBody().getContent().get(0).getName());
        assertEquals("Mouse", response.getBody().getContent().get(1).getName());
        assertEquals(2, response.getBody().getTotalElements());

        verify(productService).getFilteredProducts(any(), any(), any(), any(), any(Pageable.class));
        verify(productMapper).toProductResponseDto(product1);
        verify(productMapper).toProductResponseDto(product2);
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Given
        Long productId = 1L;
        Product product = createProduct(productId, "Test Product", "Electronics", BigDecimal.valueOf(100.00));
        ProductResponseDto productDto = createProductResponseDto(productId, "Test Product", "Electronics", BigDecimal.valueOf(100.00));
        
        when(productService.getProductById(productId)).thenReturn(product);
        when(productMapper.toProductResponseDto(product)).thenReturn(productDto);

        // When
        ResponseEntity<ProductResponseDto> response = productController.getProductById(productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Product", response.getBody().getName());
        assertEquals("Electronics", response.getBody().getCategory());
        assertEquals(BigDecimal.valueOf(100.00), response.getBody().getPrice());
        assertEquals(true, response.getBody().getAvailability());

        verify(productService).getProductById(productId);
        verify(productMapper).toProductResponseDto(product);
    }

    @Test
    void getProductById_WithInvalidId_ShouldReturnNotFound() {
        // Given
        Long productId = 999L;
        
        when(productService.getProductById(productId))
                .thenThrow(new ProductNotFoundException("Product not found with id: " + productId));

        // When & Then
        try {
            productController.getProductById(productId);
            // Should not reach here
            assertEquals(true, false, "Expected ProductNotFoundException to be thrown");
        } catch (ProductNotFoundException e) {
            assertEquals("Product not found with id: " + productId, e.getMessage());
        }

        verify(productService).getProductById(productId);
        verify(productMapper, never()).toProductResponseDto(any());
    }

    @Test
    void getCategories_ShouldReturnAllCategories() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Books", "Clothing", "Home");
        
        when(productService.getCategories()).thenReturn(categories);

        // When
        ResponseEntity<List<String>> response = productController.getCategories();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().size());
        assertEquals("Electronics", response.getBody().get(0));
        assertEquals("Books", response.getBody().get(1));
        assertEquals("Clothing", response.getBody().get(2));
        assertEquals("Home", response.getBody().get(3));

        verify(productService).getCategories();
    }

    @Test
    void getCategories_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        when(productService.getCategories()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<String>> response = productController.getCategories();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(productService).getCategories();
    }

    // Helper methods
    private Product createProduct(Long id, String name, String category, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(10);
        product.setAvailability(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private ProductResponseDto createProductResponseDto(Long id, String name, String category, BigDecimal price) {
        return ProductResponseDto.builder()
                .id(id)
                .name(name)
                .category(category)
                .price(price)
                .stock(10)
                .availability(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deals(Collections.emptyList())
                .build();
    }
}