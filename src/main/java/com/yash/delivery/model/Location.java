package com.yash.delivery.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location implements Serializable {

    @Builder.Default
    private String type = "Point";

    // [longitude, latitude]
    private Double[] coordinates;
}
