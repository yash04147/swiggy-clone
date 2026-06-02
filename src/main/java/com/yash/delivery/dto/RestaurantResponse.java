package com.yash.delivery.dto;

import com.yash.delivery.model.Location;
import com.yash.delivery.model.MenuItem;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class RestaurantResponse implements Serializable {

    private String restaurantId;

    private String name;

    private Location location;

    private List<MenuItem> menu;
}