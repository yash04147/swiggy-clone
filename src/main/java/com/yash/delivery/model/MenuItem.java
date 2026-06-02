package com.yash.delivery.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem implements Serializable {

    private String name;

    private double price;

    private boolean available;
}
