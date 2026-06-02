package com.yash.delivery.repository;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import com.yash.delivery.model.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant,String> {

    // Find restaurants owned by a specific user
    List<Restaurant> findByOwnerId(String ownerId);

    List<Restaurant> findByIsOpenTrue();

    List<Restaurant> findByLocationNear(Point point, Distance distance);
}
