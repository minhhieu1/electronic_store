package com.altech.electronicstore.service;

import com.altech.electronicstore.entity.*;
import com.altech.electronicstore.repository.*;
import com.altech.electronicstore.util.discount.DiscountEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final BasketService basketService;
    private final UserRepository userRepository;
    private final DiscountEngine discountEngine;

    @Transactional
    public Order checkout(Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting checkout for user {}", userId);
        
        try {
            Basket basket = basketService.checkoutBasket(userId);
            Order result = createOrderFromBasket(basket, userId);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Checkout completed for user {} in {}ms", userId, duration);
            
            return result;
        } catch (Exception e) {
            log.error("Checkout failed for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private Order createOrderFromBasket(Basket basket, Long userId) {
        if (basket.getBasketItems().isEmpty()) {
            throw new RuntimeException("Cannot create order from empty basket");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<String> appliedDeals = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        Map<Long, List<Deal>> productDealsMap = discountEngine.getDealsForProducts(
            basket.getBasketItems().stream().collect(Collectors.toList())
        );

        Map<Long, BigDecimal> discounts = discountEngine.calculateDiscountsForBasketItems(
            basket.getBasketItems().stream().collect(Collectors.toList()),
            productDealsMap
        );


        // Process each basket item (stock already committed by BasketService)
        for (BasketItem basketItem : basket.getBasketItems()) {
            Product product = basketItem.getProduct();
            int quantity = basketItem.getQuantity();

            // Calculate prices (no need to check/decrement stock - already done by BasketService)
            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal itemDiscount = discounts.getOrDefault(product.getId(), BigDecimal.ZERO);

            // Apply deals - use pre-loaded deals map
            List<Deal> activeDeals = productDealsMap.getOrDefault(product.getId(), List.of());
            for (Deal deal : activeDeals) {
                if (!deal.isExpired()) {
                    appliedDeals.add(deal.getDealType().getName() + " on " + product.getName());
                }
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(itemTotal.subtract(itemDiscount));
            orderItem.setDiscountApplied(itemDiscount);

            orderItems.add(orderItem);

            totalAmount = totalAmount.add(itemTotal);
            totalDiscount = totalDiscount.add(itemDiscount);
        }

        order.getOrderItems().addAll(orderItems);

        order.setTotalAmount(totalAmount);
        order.setTotalDiscount(totalDiscount);
        order.setFinalAmount(totalAmount.subtract(totalDiscount));

        // Save applied deals information to note field
        if (!appliedDeals.isEmpty()) {
            String dealsNote = "Applied Deals: " + String.join("; ", appliedDeals);
            order.setNote(dealsNote);
        } else {
            order.setNote("No deals applied");
        }

        order = orderRepository.save(order);

        return order;
    }

    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
        return orders;
    }
}
