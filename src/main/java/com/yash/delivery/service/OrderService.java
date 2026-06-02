package com.yash.delivery.service;

import com.yash.delivery.dto.OrderResponse;
import com.yash.delivery.exception.ApiException;
import com.yash.delivery.model.*;
import com.yash.delivery.repository.DeliveryPartnerRepository;
import com.yash.delivery.repository.OrderRepository;
import com.yash.delivery.repository.RestaurantRepository;
import com.yash.delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Place order, only by a customer
    public OrderResponse placeOrder(Order order){

        // Get authenticated user ID
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        // Validate role
        if(user.getRole() != UserRole.CUSTOMER){
            throw new ApiException("Only Customers can place orders");
        }

        // Validate restaurant exists
        restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new ApiException("Restaurant not found"));

        // Calculate total amount
        double total = order.getItems()
                .stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Set system-controlled fields
        order.setCustomerId(userId);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        // Save order
        Order savedOrder = orderRepository.save(order);

        // send order update over STOMP
        sendOrderUpdate(savedOrder);

        // return DTO
        return mapToOrderResponse(savedOrder);
    }

    // Get orders for current customer
    public List<OrderResponse> getMyOrders(){

        // Get authenticated user ID
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        // Validate role
        if (user.getRole() != UserRole.CUSTOMER){
            throw new ApiException("Only customers can see order history");
        }

        // Fetch orders
        return orderRepository.findByCustomerId(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    // Get orders for a restaurant (OWNER only)
    public List<OrderResponse> getOrdersForRestaurant(String restaurantId){

        // 1️⃣ Get authenticated user ID
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // 2️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        // 3️⃣ Validate role
        if (user.getRole() != UserRole.RESTAURANT_OWNER){
            throw new ApiException("Only restaurant owners can view restaurant orders");
        }

        // 4️⃣ Fetch restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ApiException("Restaurant not found"));

        // 5️⃣ Verify ownership
        if (!restaurant.getOwnerId().equals(userId)){
            throw new ApiException("You can only view orders for your own restaurant");
        }

        // 6️⃣ Return orders
        return orderRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    // Update order status (OWNER only)
    public Order updateOrderStatus(String orderId, OrderStatus newStatus){

        // Fetch authenticated user ID
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        // Validate User role
        if (user.getRole() != UserRole.RESTAURANT_OWNER){
            throw new ApiException("Only restaurant owner can update order status");
        }

        // Fetch Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found"));

        // Fetch restaurant
        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new ApiException("Restaurant not found"));

        // Validate ownership
        if (!restaurant.getOwnerId().equals(userId)){
            throw new ApiException("You can only update orders for your own restaurant");
        }

        // Validate status transition
        OrderStatus current = order.getStatus();

        boolean validTransition =
                (current == OrderStatus.PLACED && newStatus == OrderStatus.ACCEPTED) ||
                        (current == OrderStatus.ACCEPTED && newStatus == OrderStatus.PREPARING) ||
                        (current == OrderStatus.PREPARING && newStatus == OrderStatus.READY_FOR_PICKUP);

        if (!validTransition){
            throw new ApiException(
                    "Invalid status transition from " + current + " to " + newStatus
            );
        }

        // If order is ready, auto assign delivery partner
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {

            order.setStatus(OrderStatus.READY_FOR_PICKUP);

            autoAssignDeliveryPartner(order);

        } else {
            order.setStatus(newStatus);
        }

        // send order update over STOMP
        sendOrderUpdate(order);

        return orderRepository.save(order);

    }

    // 🚴 Delivery partner picks up order
    public Order pickUpOrder(String orderId){

        // Get authenticated user ID
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        // Validate role
        if (user.getRole() != UserRole.DELIVERY_PARTNER){
            throw new ApiException("Only Delivery partners can pickup orders");
        }

        // Fetch Delivery partner profile
        DeliveryPartner partner = deliveryPartnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Delivery partner profile not found"));

        // Fetch Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found"));

        // Validate Order state
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP){
            throw new ApiException("Only READY_FOR_PICKUP orders can be picked up");
        }

        // Assign delivery partner and update status
        order.setDeliveryPartnerId(partner.getId());
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        // send order update over STOMP
        sendOrderUpdate(order);

        return orderRepository.save(order);
    }

    // 📦 Delivery partner marks order as delivered
    public Order markOrderDelivered(String orderId){

        // get authenticated user ID
        String userId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        // Validate role
        if (user.getRole() != UserRole.DELIVERY_PARTNER){
            throw new ApiException("Only Delivery Partner can mark order delivered");
        }

        // Fetch Delivery Partner profile
        DeliveryPartner partner = deliveryPartnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Delivery Partner profile not found"));

        // Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found"));

        // Ensure this partner owns the order
        if (!order.getDeliveryPartnerId().equals(partner.getId())){
            throw new ApiException("You can only deliver orders assigned to you");
        }

        // Ensure correct current status
        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new ApiException(
                    "Only OUT_FOR_DELIVERY orders can be marked as delivered"
            );
        }

        // mark delivered and save
        order.setStatus(OrderStatus.DELIVERED);

        // send order update over STOMP
        sendOrderUpdate(order);

        return orderRepository.save(order);
    }


    // 🚴 Auto assign nearest available delivery partner
    private void autoAssignDeliveryPartner(Order order) {

        System.out.println("AUTO ASSIGN STARTED");

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new ApiException("Restaurant not found"));

        System.out.println("Restaurant found");

        Double[] coordinates = restaurant.getLocation().getCoordinates();

        System.out.println("Coordinates: " + Arrays.toString(coordinates));

        Point point = new Point(coordinates[0], coordinates[1]);

        List<DeliveryPartner> nearbyPartners =
                deliveryPartnerRepository.findByAvailableTrueAndLocationNear(
                        point,
                        new Distance(5, Metrics.KILOMETERS)
                );

        System.out.println("Nearby partners found: " + nearbyPartners.size());

        // nearest partner = first result
        DeliveryPartner partner = nearbyPartners.getFirst();

        // assign partner
        order.setDeliveryPartnerId(partner.getId());

        // update status
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        // mark partner unavailable
        partner.setAvailable(false);

        deliveryPartnerRepository.save(partner);
    }

    // 📡 Send real-time order update
    private void sendOrderUpdate(Order order) {

        String destination = "/topic/orders/" + order.getId();

        System.out.println("Sending WS update for order: " + order.getId());

        messagingTemplate.convertAndSend(destination, (Object) Map.of(
                "orderId", order.getId(),
                "status", order.getStatus()
        ));
    }

    // 🔄 Convert Order entity to response DTO
    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .restaurantId(order.getRestaurantId())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
