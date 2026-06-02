package com.yash.delivery.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private String name;

    private Double price;

    private Integer quantity;
}
