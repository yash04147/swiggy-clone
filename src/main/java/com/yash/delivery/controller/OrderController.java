package com.yash.delivery.controller;

import com.yash.delivery.dto.OrderResponse;
import com.yash.delivery.dto.UpdateOrderStatusRequest;
import com.yash.delivery.model.Order;
import com.yash.delivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Place order (CUSTOMER only)
    @PostMapping
    public OrderResponse placeOrder(@RequestBody Order order){
        return orderService.placeOrder(order);
    }

    // Get current customer's orders
    @GetMapping("/my")
    public List<OrderResponse> getMyOrders(){
        return  orderService.getMyOrders();
    }

    // Get orders for a restaurant (OWNER only)
    @GetMapping("/restaurant/{restaurantId}")
    public List<OrderResponse> getOrdersForRestaurant(@PathVariable String restaurantId){
        return  orderService.getOrdersForRestaurant(restaurantId);
    }

    // Update order status (OWNER only)
    @PatchMapping("/{orderId}/status")
    public Order updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateOrderStatus(orderId, request.getStatus());
    }

    // Delivery partner picks up an order
    @PatchMapping("/{orderId}/pickup")
    public Order pickUpOrder(@PathVariable String orderId) {
        return orderService.pickUpOrder(orderId);
    }

    // Delivery partner marks order as delivered
    @PatchMapping("/{orderId}/deliver")
    public Order markOrderDelivered(@PathVariable String orderId) {
        return orderService.markOrderDelivered(orderId);
    }
}
