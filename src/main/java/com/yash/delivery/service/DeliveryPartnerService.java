package com.yash.delivery.service;

import com.yash.delivery.exception.ApiException;
import com.yash.delivery.model.DeliveryPartner;
import com.yash.delivery.model.User;
import com.yash.delivery.model.UserRole;
import com.yash.delivery.repository.DeliveryPartnerRepository;
import com.yash.delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserRepository userRepository;

    // Create delivery partner profile
    public DeliveryPartner createProfile(DeliveryPartner deliveryPartner){

        // Get authenticated user ID
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        // Validate role
        if (user.getRole() != UserRole.DELIVERY_PARTNER){
            throw new ApiException("Only delivery partners can create delivery profiles");
        }

        // Prevent duplicate profiles
        if (deliveryPartnerRepository.findByUserId(userId).isPresent()){
            throw new ApiException("Delivery profile already exists");
        }

        // Assign authenticated user
        deliveryPartner.setUserId(userId);

        // Set available=True if not provided
        if (deliveryPartner.getAvailable() == null){
            deliveryPartner.setAvailable(true);
        }

        // save profile
        return deliveryPartnerRepository.save(deliveryPartner);
    }
}
