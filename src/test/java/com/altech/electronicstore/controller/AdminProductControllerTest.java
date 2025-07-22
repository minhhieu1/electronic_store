package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.product.ProductDto;
import com.altech.electronicstore.dto.product.ProductResponseDto;
import com.altech.electronicstore.entity.Product;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private AdminProductController adminProductController;

    @Test
    void getAllProducts_WithDefaultParameters_ShouldReturnPagedProducts() {
        // Given
        Product product1 = createProduct(1L, "Laptop", "Electronics", BigDecimal.valueOf(1200.00));
        Product product2 = createProduct(2L, "Mouse", "Electronics", BigDecimal.valueOf(50.00));
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products);

        ProductResponseDto productDto1 = createProductResponseDto(1L, "Laptop", "Electronics", BigDecimal.valueOf(1200.00));
        ProductResponseDto productDto2 = createProductResponseDto(2L, "Mouse", "Electronics", BigDecimal.valueOf(50.00));
        
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toProductResponseDto(product1)).thenReturn(productDto1);
        when(productMapper.toProductResponseDto(product2)).thenReturn(productDto2);

        // When
        ResponseEntity<Page<ProductResponseDto>> response = adminProductController.getAllProducts(0, 10, "name", "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("Laptop", response.getBody().getContent().get(0).getName());
        assertEquals("Mouse", response.getBody().getContent().get(1).getName());

        verify(productService).getAllProducts(any(Pageable.class));
        verify(productMapper).toProductResponseDto(product1);
        verify(productMapper).toProductResponseDto(product2);
    }

    @Test
    void getAllProducts_WithCustomParameters_ShouldReturnPagedProducts() {
        // Given
        Product product = createProduct(1L, "Gaming Laptop", "Electronics", BigDecimal.valueOf(2500.00));
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        ProductResponseDto productDto = createProductResponseDto(1L, "Gaming Laptop", "Electronics", BigDecimal.valueOf(2500.00));

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toProductResponseDto(product)).thenReturn(productDto);

        // When
        ResponseEntity<Page<ProductResponseDto>> response = adminProductController.getAllProducts(0, 5, "price", "desc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Gaming Laptop", response.getBody().getContent().get(0).getName());
        assertEquals(BigDecimal.valueOf(2500.00), response.getBody().getContent().get(0).getPrice());

        verify(productService).getAllProducts(any(Pageable.class));
        verify(productMapper).toProductResponseDto(product);
    }

    @Test
    void getAllProducts_WithEmptyResult_ShouldReturnEmptyPage() {
        // Given
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<ProductResponseDto>> response = adminProductController.getAllProducts(0, 10, "name", "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getContent().size());
        assertEquals(0, response.getBody().getTotalElements());

        verify(productService).getAllProducts(any(Pageable.class));
        verify(productMapper, never()).toProductResponseDto(any());
    }

    @Test
    void createProduct_WithValidProductDto_ShouldReturnCreatedProduct() {
        // Given
        ProductDto productDto = createProductDto("New Laptop", "Electronics", BigDecimal.valueOf(1500.00));
        Product createdProduct = createProduct(1L, "New Laptop", "Electronics", BigDecimal.valueOf(1500.00));
        ProductResponseDto productResponseDto = createProductResponseDto(1L, "New Laptop", "Electronics", BigDecimal.valueOf(1500.00));

        when(productService.createProduct(any(ProductDto.class))).thenReturn(createdProduct);
        when(productMapper.toProductResponseDto(createdProduct)).thenReturn(productResponseDto);

        // When
        ResponseEntity<ProductResponseDto> response = adminProductController.createProduct(productDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Laptop", response.getBody().getName());
        assertEquals("Electronics", response.getBody().getCategory());
        assertEquals(BigDecimal.valueOf(1500.00), response.getBody().getPrice());

        verify(productService).createProduct(any(ProductDto.class));
        verify(productMapper).toProductResponseDto(createdProduct);
    }

    @Test
    void updateProduct_WithValidIdAndDto_ShouldReturnUpdatedProduct() {
        // Given
        Long productId = 1L;
        ProductDto productDto = createProductDto("Updated Laptop", "Electronics", BigDecimal.valueOf(1800.00));
        Product updatedProduct = createProduct(productId, "Updated Laptop", "Electronics", BigDecimal.valueOf(1800.00));
        ProductResponseDto productResponseDto = createProductResponseDto(productId, "Updated Laptop", "Electronics", BigDecimal.valueOf(1800.00));

        when(productService.updateProduct(eq(productId), any(ProductDto.class))).thenReturn(updatedProduct);
        when(productMapper.toProductResponseDto(updatedProduct)).thenReturn(productResponseDto);

        // When
        ResponseEntity<ProductResponseDto> response = adminProductController.updateProduct(productId, productDto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Laptop", response.getBody().getName());
        assertEquals("Electronics", response.getBody().getCategory());
        assertEquals(BigDecimal.valueOf(1800.00), response.getBody().getPrice());

        verify(productService).updateProduct(eq(productId), any(ProductDto.class));
        verify(productMapper).toProductResponseDto(updatedProduct);
    }

    @Test
    void updateProduct_WithInvalidId_ShouldThrowException() {
        // Given
        Long productId = 999L;
        ProductDto productDto = createProductDto("Updated Laptop", "Electronics", BigDecimal.valueOf(1800.00));

        when(productService.updateProduct(eq(productId), any(ProductDto.class)))
                .thenThrow(new RuntimeException("Product not found with id: " + productId));

        // When & Then
        try {
            adminProductController.updateProduct(productId, productDto);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Product not found with id: " + productId, e.getMessage());
        }

        verify(productService).updateProduct(eq(productId), any(ProductDto.class));
        verify(productMapper, never()).toProductResponseDto(any());
    }

    @Test
    void deleteProduct_WithValidId_ShouldReturnNoContent() {
        // Given
        Long productId = 1L;
        doNothing().when(productService).deleteProduct(productId);

        // When
        ResponseEntity<Void> response = adminProductController.deleteProduct(productId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void deleteProduct_WithInvalidId_ShouldThrowException() {
        // Given
        Long productId = 999L;
        doThrow(new RuntimeException("Product not found with id: " + productId))
                .when(productService).deleteProduct(productId);

        // When & Then
        try {
            adminProductController.deleteProduct(productId);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Product not found with id: " + productId, e.getMessage());
        }

        verify(productService).deleteProduct(productId);
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

    private ProductDto createProductDto(String name, String category, BigDecimal price) {
        ProductDto productDto = new ProductDto();
        productDto.setName(name);
        productDto.setCategory(category);
        productDto.setPrice(price);
        productDto.setStock(10);
        productDto.setAvailability(true);
        return productDto;
    }
}
