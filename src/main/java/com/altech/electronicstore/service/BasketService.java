package com.altech.electronicstore.service;

import com.altech.electronicstore.dto.product.StockValidationResult;
import com.altech.electronicstore.entity.*;
import com.altech.electronicstore.exception.BasketNotFoundException;
import com.altech.electronicstore.exception.InsufficientStockException;
import com.altech.electronicstore.exception.ProductNotFoundException;
import com.altech.electronicstore.exception.ProductOutOfStockException;
import com.altech.electronicstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;
    private final BasketItemRepository basketItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public Basket getBasketByUserId(Long userId) {
        Basket basket = basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE)
                .orElseGet(() -> createBasketForUser(userId));

        return basket;
    }

    /**
     * Get all baskets for a user (including ACTIVE, CHECKED_OUT, EXPIRED)
     */
    public List<Basket> getAllBasketsByUserId(Long userId) {
        return basketRepository.findByUserIdWithItemsOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Basket createNewBasket(Long userId) {
        return createBasketForUser(userId);
    }

    @Transactional
    public Basket addItemToBasket(Long userId, Long productId, Integer quantity) {
        Basket basket = basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)
                .orElseGet(() -> createBasketForUser(userId));

        if (basket.getStatus() != BasketStatus.ACTIVE) {
            throw new RuntimeException("Cannot modify basket with status: " + basket.getStatus());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        BasketItem existingItem = basketItemRepository
                .findByBasketIdAndProductId(basket.getId(), productId)
                .orElse(null);

        int newTotalQuantity = quantity;
        if (existingItem != null) {
            newTotalQuantity = existingItem.getQuantity() + quantity;
        }

        StockValidationResult stockResult = productService.validateAndGetStock(productId, newTotalQuantity);
        
        if (!stockResult.isAvailable()) {
            throw new RuntimeException("Product is not available: " + product.getName());
        }
        
        if (!stockResult.isHasStock()) {
            if (stockResult.getCurrentStock() <= 0) {
                throw new ProductOutOfStockException(product.getName());
            } else {
                throw new InsufficientStockException(product.getName(), newTotalQuantity, stockResult.getCurrentStock());
            }
        }

        if (existingItem != null) {
            existingItem.setQuantity(newTotalQuantity);
            basketItemRepository.save(existingItem);
        } else {
            BasketItem newItem = new BasketItem();
            newItem.setBasket(basket);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            basketItemRepository.save(newItem);
        }

        basket = basketRepository.findById(basket.getId()).orElseThrow();
        return basket;
    }

    @Transactional
    public Basket removeItemFromBasket(Long userId, Long productId) {
        Basket basket = basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)
                .orElseThrow(() -> new BasketNotFoundException(userId));

        if (basket.getStatus() != BasketStatus.ACTIVE) {
            throw new RuntimeException("Cannot modify basket with status: " + basket.getStatus());
        }

        int deletedCount = basketItemRepository.deleteByBasketIdAndProductIdWithCount(basket.getId(), productId);
        
        if (deletedCount == 0) {
            throw new RuntimeException("Item not found in basket");
        }

        // Refresh basket to get updated items
        basket = basketRepository.findById(basket.getId()).orElseThrow();
        return basket;
    }

    @Transactional
    public Basket updateItemQuantity(Long userId, Long productId, Integer quantity) {
        Basket basket = basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)
                .orElseThrow(() -> new BasketNotFoundException(userId));

        if (basket.getStatus() != BasketStatus.ACTIVE) {
            throw new RuntimeException("Cannot modify basket with status: " + basket.getStatus());
        }

        BasketItem item = basketItemRepository
                .findByBasketIdAndProductId(basket.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in basket"));

        if (quantity <= 0) {
            basketItemRepository.delete(item);
        } else {
            Product product = item.getProduct();
            StockValidationResult stockResult = productService.validateAndGetStock(productId, quantity);
            
            if (!stockResult.isAvailable()) {
                throw new RuntimeException("Product is not available: " + product.getName());
            }
            
            if (!stockResult.isHasStock()) {
                throw new InsufficientStockException(product.getName(), quantity, stockResult.getCurrentStock());
            }

            item.setQuantity(quantity);
            basketItemRepository.save(item);
        }

        basket = basketRepository.findById(basket.getId()).orElseThrow();
        return basket;
    }

    @Transactional
    public void clearBasket(Long userId) {
        Basket basket = basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)
                .orElseThrow(() -> new BasketNotFoundException(userId));

        if (basket.getStatus() != BasketStatus.ACTIVE) {
            throw new RuntimeException("Cannot modify basket with status: " + basket.getStatus());
        }

        if (basket.getBasketItems().isEmpty()) {
            return; // Nothing to clear
        }

        for (BasketItem item : basket.getBasketItems()) {
            item.setBasket(null); // break bidirectional link
        }

        basket.getBasketItems().clear();
        basketRepository.save(basket);
    }

    @Transactional
    private Basket createBasketForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)
                .ifPresent(existingBasket -> {
                    existingBasket.setStatus(BasketStatus.EXPIRED);
                    basketRepository.save(existingBasket);
                });

        Basket basket = new Basket();
        basket.setUser(user);
        basket.setStatus(BasketStatus.ACTIVE);
        return basketRepository.save(basket);
    }

    @Transactional
    public Basket checkoutBasket(Long userId) {
        Basket basket = basketRepository.findByUserIdAndStatusWithItems(userId, BasketStatus.ACTIVE)
                .orElseThrow(() -> new BasketNotFoundException(userId));

        if (basket.getStatus() != BasketStatus.ACTIVE) {
            throw new RuntimeException("Cannot checkout basket with status: " + basket.getStatus());
        }

        List<BasketItem> basketItems = basket.getBasketItems().stream().toList();
        boolean hasInsufficientStock = basketItems.parallelStream()
                .anyMatch(item -> !productService.hasStock(item.getProduct().getId(), item.getQuantity()));
        
        if (hasInsufficientStock) {
            throw new RuntimeException("Some items in basket have insufficient stock");
        }

        productService.commitStockReduction(basketItems);

        basket.setStatus(BasketStatus.CHECKED_OUT);
        basketRepository.save(basket);

        return basket;
    }
}
