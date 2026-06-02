package com.yash.delivery.controller;

import com.yash.delivery.dto.RestaurantResponse;
import com.yash.delivery.model.Restaurant;
import com.yash.delivery.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public Restaurant createRestaurant(@RequestBody Restaurant restaurant){
        return restaurantService.createRestaurant(restaurant);
    }

    // Get restaurants owned by current user
    @GetMapping("/my")
    public List<Restaurant> getMyRestaurants(){
        return restaurantService.getMyRestaurants();
    }

    // Browse all open restaurants (public)
    @GetMapping
    public List<RestaurantResponse> getOpenRestaurants(){
        return restaurantService.getOpenRestaurants();
    }

    // Get nearby restaurants
    @GetMapping("/nearby")
    public List<RestaurantResponse> getNearbyRestaurants(
            @RequestParam double lat,
            @RequestParam double lng
            ) {
        return restaurantService.getNearbyRestaurants(lat, lng);
    }
}
