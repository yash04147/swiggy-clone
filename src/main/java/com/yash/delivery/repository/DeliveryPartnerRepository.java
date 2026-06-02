package com.yash.delivery.repository;

import com.yash.delivery.model.DeliveryPartner;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository extends MongoRepository<DeliveryPartner, String> {

    Optional<DeliveryPartner> findByUserId(String userId);

    List<DeliveryPartner> findByAvailableTrue();

    List<DeliveryPartner> findByAvailableTrueAndLocationNear(
            Point point,
            Distance distance
    );
}
