package com.yash.delivery.controller;

import com.yash.delivery.model.DeliveryPartner;
import com.yash.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    // Create delivery partner profile
    @PostMapping
    public DeliveryPartner createProfile(@RequestBody DeliveryPartner deliveryPartner){
        return deliveryPartnerService.createProfile(deliveryPartner);
    }
}
