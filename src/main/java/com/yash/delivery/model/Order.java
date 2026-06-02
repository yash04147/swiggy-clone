package com.yash.delivery.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    private String customerId;

    private String restaurantId;

    private String deliveryPartnerId;

    private List<OrderItem> items;

    private Double totalAmount;

    private String deliveryAddress;

    private OrderStatus status;

    private LocalDateTime createdAt;
}
