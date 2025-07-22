package com.altech.electronicstore.service;

import com.altech.electronicstore.dto.product.ProductDto;
import com.altech.electronicstore.dto.product.StockValidationResult;
import com.altech.electronicstore.entity.BasketItem;
import com.altech.electronicstore.entity.Product;
import com.altech.electronicstore.exception.InsufficientStockException;
import com.altech.electronicstore.exception.ProductNotFoundException;
import com.altech.electronicstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByAvailabilityTrue(pageable);
    }

    public Page<Product> getFilteredProducts(String category, BigDecimal minPrice, BigDecimal maxPrice, Boolean availability, Pageable pageable) {
        return productRepository.findFilteredProducts(category, minPrice, maxPrice, availability, pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    @Transactional
    public Product createProduct(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setCategory(productDto.getCategory());
        product.setAvailability(productDto.getAvailability());
        
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDto productDto) {
        Product product = getProductById(id);
        
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setCategory(productDto.getCategory());
        product.setAvailability(productDto.getAvailability());
        
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional
    public void decrementStock(Long productId, int quantity) {
        Product product = getProductById(productId);
        
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    product.getName(), quantity, product.getStock());
        }
        
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    public boolean canReserveStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }
        return product.getAvailability() && product.getStock() >= quantity;
    }

    public StockValidationResult validateAndGetStock(Long productId, Integer requiredQuantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return new StockValidationResult(false, 0, false);
        }
        
        boolean hasStock = product.getAvailability() && product.getStock() >= requiredQuantity;
        return new StockValidationResult(hasStock, product.getStock(), product.getAvailability());
    }

    @Transactional
    public void commitStockReduction(List<BasketItem> basketItems) {
        for (BasketItem item : basketItems) {
            decrementStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }

    public boolean hasStock(Long productId, Integer requiredQuantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }
        return product.getAvailability() && product.getStock() >= requiredQuantity;
    }

    public Integer getCurrentStock(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getStock() : 0;
    }
}
