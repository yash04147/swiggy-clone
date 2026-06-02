package com.yash.delivery.service;

import com.yash.delivery.dto.RestaurantResponse;
import com.yash.delivery.exception.ApiException;
import com.yash.delivery.model.Restaurant;
import com.yash.delivery.model.User;
import com.yash.delivery.model.UserRole;
import com.yash.delivery.repository.RestaurantRepository;
import com.yash.delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    // Create restaurant (OWNER only)
    @CacheEvict(value = "open-restaurants", allEntries = true)
    public Restaurant createRestaurant(Restaurant restaurant){

        // Get authenticated user ID from JWT
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch full user from DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("No user found"));

        // validate role
        if(user.getRole() != UserRole.RESTAURANT_OWNER){
            throw new ApiException("Only restaurant owners can create restaurants");
        }

        // Set location type
        if (restaurant.getLocation() != null
                && restaurant.getLocation().getType() == null) {
            restaurant.getLocation().setType("Point");
        }

        // Assign owner automatically
        restaurant.setOwnerId(userId);

        // Default state
        restaurant.setIsOpen(true);

        // Save restaurant
        return restaurantRepository.save(restaurant);
    }

    // View restaurants owned by current user
    public List<Restaurant> getMyRestaurants(){

        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return restaurantRepository.findByOwnerId(userId);
    }

    // View open restaurants
    @Cacheable("open-restaurants")
    public List<RestaurantResponse> getOpenRestaurants(){

        System.out.println("FETCHING FROM MONGODB");
        return restaurantRepository.findByIsOpenTrue()
                .stream()
                .map(this::mapToRestaurantResponse)
                .toList();
    }

    // 📍 Find nearby restaurants within 5 km
    public List<RestaurantResponse> getNearbyRestaurants(double latitude, double longitude){

        Point point = new Point(longitude, latitude);

        Distance distance = new Distance(5, Metrics.KILOMETERS);

        return restaurantRepository.findByLocationNear(point,distance)
                .stream()
                .filter(restaurant -> Boolean.TRUE.equals(restaurant.getIsOpen()))
                .map(this::mapToRestaurantResponse)
                .toList();
    }

    // 🔄 Convert Restaurant entity to response DTO
    private RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getId())
                .name(restaurant.getName())
                .location(restaurant.getLocation())
                .menu(restaurant.getMenu())
                .build();
    }
}
