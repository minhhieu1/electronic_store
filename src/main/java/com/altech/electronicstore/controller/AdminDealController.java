package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.deal.DealDto;
import com.altech.electronicstore.dto.deal.DealResponseDto;
import com.altech.electronicstore.dto.deal.DealTypeResponseDto;
import com.altech.electronicstore.entity.Deal;
import com.altech.electronicstore.entity.DealType;
import com.altech.electronicstore.mapper.DealMapper;
import com.altech.electronicstore.service.DealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/deals")
@RequiredArgsConstructor
@Tag(name = "Admin - Deals", description = "Deal management endpoints (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminDealController {

    private final DealService dealService;
    private final DealMapper dealMapper;

    @GetMapping
    @Operation(summary = "Get all active deals", description = "Get list of all currently active deals")
    @PreAuthorize("@permissionChecker.hasPermission('DEAL', 'READ')")
    public ResponseEntity<List<DealResponseDto>> getAllActiveDeals() {
        List<Deal> deals = dealService.getAllActiveDeals();
        List<DealResponseDto> dealDtos = dealMapper.toDealResponseDtoList(deals);
        return ResponseEntity.ok(dealDtos);
    }

    @GetMapping("/types")
    @Operation(summary = "Get all deal types", description = "Get list of all available deal types")
    @PreAuthorize("@permissionChecker.hasPermission('DEAL', 'READ')")
    public ResponseEntity<List<DealTypeResponseDto>> getAllDealTypes() {
        List<DealType> dealTypes = dealService.getAllDealTypes();
        List<DealTypeResponseDto> dealTypeDtos = dealMapper.toDealTypeResponseDtoList(dealTypes);
        return ResponseEntity.ok(dealTypeDtos);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get deals for product", description = "Get all active deals for a specific product")
    @PreAuthorize("@permissionChecker.hasPermission('DEAL', 'READ')")
    public ResponseEntity<List<DealResponseDto>> getDealsForProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        List<Deal> deals = dealService.getActiveDealsForProduct(productId);
        List<DealResponseDto> dealDtos = dealMapper.toDealResponseDtoList(deals);
        return ResponseEntity.ok(dealDtos);
    }

    @PostMapping
    @Operation(summary = "Create deal", description = "Create a new deal for a product")
    @PreAuthorize("@permissionChecker.hasPermission('DEAL', 'CREATE')")
    public ResponseEntity<DealResponseDto> createDeal(@Valid @RequestBody DealDto dealDto) {
        Deal deal = dealService.createDeal(dealDto);
        DealResponseDto dealResponseDto = dealMapper.toDealResponseDto(deal);
        return ResponseEntity.status(HttpStatus.CREATED).body(dealResponseDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update deal", description = "Update an existing deal")
    @PreAuthorize("@permissionChecker.hasPermission('DEAL', 'UPDATE')")
    public ResponseEntity<DealResponseDto> updateDeal(
            @Parameter(description = "Deal ID") @PathVariable Long id,
            @Valid @RequestBody DealDto dealDto) {
        Deal deal = dealService.updateDeal(id, dealDto);
        DealResponseDto dealResponseDto = dealMapper.toDealResponseDto(deal);
        return ResponseEntity.ok(dealResponseDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete deal", description = "Delete a deal")
    @PreAuthorize("@permissionChecker.hasPermission('DEAL', 'DELETE')")
    public ResponseEntity<Void> deleteDeal(
            @Parameter(description = "Deal ID") @PathVariable Long id) {
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }
}
