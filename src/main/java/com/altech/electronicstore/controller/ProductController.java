package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.product.ProductResponseDto;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.mapper.ProductMapper;
import com.altech.electronicstore.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product browsing endpoints (public)")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    @Operation(summary = "Get all products", description = "Get paginated list of available products with optional filters")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Availability") @RequestParam(required = false) Boolean availability) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.getFilteredProducts(category, minPrice, maxPrice, availability, pageable);
        Page<ProductResponseDto> productDtos = products.map(productMapper::toProductResponseDto);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get detailed information about a specific product")
    public ResponseEntity<ProductResponseDto> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        ProductResponseDto productDto = productMapper.toProductResponseDto(product);
        return ResponseEntity.ok(productDto);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Get list of all available product categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = productService.getCategories();
        return ResponseEntity.ok(categories);
    }
}
