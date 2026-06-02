package com.yash.delivery.repository;

import com.yash.delivery.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order,String> {

    // Customer order history
    List<Order> findByCustomerId(String customerId);

    // Restaurant order management
    List<Order> findByRestaurantId(String restaurantId);
}
