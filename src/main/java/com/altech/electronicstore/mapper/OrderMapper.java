package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.order.OrderDto;
import com.altech.electronicstore.dto.order.OrderItemDto;
import com.altech.electronicstore.entity.Order;
import com.altech.electronicstore.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {

    public OrderDto toOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTotalDiscount(order.getTotalDiscount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setOrderDate(order.getOrderDate());
        dto.setNote(order.getNote());

        List<OrderItemDto> itemDtos = new ArrayList<>(order.getOrderItems().size());
        for (OrderItem item : order.getOrderItems()) {
            itemDtos.add(toOrderItemDto(item));
        }
        dto.setItems(itemDtos);

        return dto;
    }

    public OrderItemDto toOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setDiscountApplied(item.getDiscountApplied());
        return dto;
    }

    /**
     * Convert a list of Order entities to a list of OrderDto
     */
    public List<OrderDto> toOrderDtoList(List<Order> orders) {
        List<OrderDto> orderDtos = new ArrayList<>(orders.size());
        for (Order order : orders) {
            orderDtos.add(toOrderDto(order));
        }
        return orderDtos;
    }
}
