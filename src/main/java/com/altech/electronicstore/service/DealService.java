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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final DealTypeRepository dealTypeRepository;
    private final ProductRepository productRepository;

    public List<Deal> getActiveDealsForProduct(Long productId) {
        return dealRepository.findActiveDealsForProduct(productId, LocalDateTime.now());
    }

    public Map<Long, List<Deal>> getActiveDealsForProducts(Set<Long> productIds) {
        List<Deal> allDeals = dealRepository.findActiveDealsForProducts(productIds, LocalDateTime.now());
        return allDeals.stream()
                .collect(Collectors.groupingBy(deal -> deal.getProduct().getId()));
    }

    public List<Deal> getAllActiveDeals() {
        return dealRepository.findAllActiveDeals(LocalDateTime.now());
    }

    public List<DealType> getAllDealTypes() {
        return dealTypeRepository.findAll();
    }

    @Transactional
    public Deal createDeal(DealDto dealDto) {
        Product product = productRepository.findById(dealDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(dealDto.getProductId()));
        
        DealType dealType = dealTypeRepository.findById(dealDto.getDealTypeId())
                .orElseThrow(() -> new RuntimeException("Deal type not found with id: " + dealDto.getDealTypeId()));

        boolean dealExists = dealRepository.existsByProductIdAndDealTypeIdAndNotExpired(
                dealDto.getProductId(), 
                dealDto.getDealTypeId(), 
                LocalDateTime.now(),
                null
        );
        
        if (dealExists) {
            throw new DuplicateDealException(product.getName(), dealType.getName());
        }

        Deal deal = new Deal();
        deal.setProduct(product);
        deal.setDealType(dealType);
        deal.setDiscountPercent(dealDto.getDiscountPercent());
        deal.setDiscountAmount(dealDto.getDiscountAmount());
        deal.setMinimumQuantity(dealDto.getMinimumQuantity());
        deal.setExpirationDate(dealDto.getExpirationDate());

        return dealRepository.save(deal);
    }

    @Transactional
    public Deal updateDeal(Long id, DealDto dealDto) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found with id: " + id));

        Product newProduct = deal.getProduct();
        DealType newDealType = deal.getDealType();

        if (dealDto.getProductId() != null) {
            newProduct = productRepository.findById(dealDto.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(dealDto.getProductId()));
        }

        if (dealDto.getDealTypeId() != null) {
            newDealType = dealTypeRepository.findById(dealDto.getDealTypeId())
                    .orElseThrow(() -> new RuntimeException("Deal type not found with id: " + dealDto.getDealTypeId()));
        }

        if (dealDto.getProductId() != null || dealDto.getDealTypeId() != null) {
            boolean dealExists = dealRepository.existsByProductIdAndDealTypeIdAndNotExpired(
                    newProduct.getId(), 
                    newDealType.getId(), 
                    LocalDateTime.now(),
                    id
            );
            
            if (dealExists) {
                throw new DuplicateDealException(newProduct.getName(), newDealType.getName());
            }
        }

        deal.setProduct(newProduct);
        deal.setDealType(newDealType);

        if (dealDto.getDiscountPercent() != null) {
            deal.setDiscountPercent(dealDto.getDiscountPercent());
        }
        
        if (dealDto.getDiscountAmount() != null) {
            deal.setDiscountAmount(dealDto.getDiscountAmount());
        }
        
        if (dealDto.getMinimumQuantity() != null) {
            deal.setMinimumQuantity(dealDto.getMinimumQuantity());
        }

        if (dealDto.getExpirationDate() != null) {
            deal.setExpirationDate(dealDto.getExpirationDate());
        }

        return dealRepository.save(deal);
    }

    @Transactional
    public void deleteDeal(Long id) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found with id: " + id));
        dealRepository.delete(deal);
    }
}
