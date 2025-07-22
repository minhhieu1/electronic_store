package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.product.ProductDto;
import com.altech.electronicstore.dto.product.ProductResponseDto;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.mapper.ProductMapper;
import com.altech.electronicstore.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Products", description = "Product management endpoints (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    @Operation(summary = "Get all products (Admin)", description = "Get paginated list of all products including unavailable ones")
    @PreAuthorize("@permissionChecker.hasPermission('PRODUCT', 'READ')")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.getAllProducts(pageable);
        Page<ProductResponseDto> productDtos = products.map(productMapper::toProductResponseDto);
        return ResponseEntity.ok(productDtos);
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product")
    @PreAuthorize("@permissionChecker.hasPermission('PRODUCT', 'CREATE')")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product product = productService.createProduct(productDto);
        ProductResponseDto productResponseDto = productMapper.toProductResponseDto(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    @PreAuthorize("@permissionChecker.hasPermission('PRODUCT', 'UPDATE')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        Product product = productService.updateProduct(id, productDto);
        ProductResponseDto productResponseDto = productMapper.toProductResponseDto(product);
        return ResponseEntity.ok(productResponseDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product")
    @PreAuthorize("@permissionChecker.hasPermission('PRODUCT', 'DELETE')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
