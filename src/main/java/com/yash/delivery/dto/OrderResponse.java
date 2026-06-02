package com.yash.delivery.dto;

import com.yash.delivery.model.OrderItem;
import com.yash.delivery.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private String orderId;

    private String restaurantId;

    private List<OrderItem> items;

    private Double totalAmount;

    private String deliveryAddress;

    private OrderStatus status;

    private LocalDateTime createdAt;
}
