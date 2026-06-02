package com.yash.delivery.service;

import com.yash.delivery.dto.OrderResponse;
import com.yash.delivery.exception.ApiException;
import com.yash.delivery.model.*;
import com.yash.delivery.repository.DeliveryPartnerRepository;
import com.yash.delivery.repository.OrderRepository;
import com.yash.delivery.repository.RestaurantRepository;
import com.yash.delivery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("customer-1", null)
        );
    }

    @Test
    void placeOrder_shouldSucceed_whenUserIsCustomer() {

        User customer = User.builder()
                .id("customer-1")
                .role(UserRole.CUSTOMER)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id("restaurant-1")
                .build();

        Order order = Order.builder()
                .restaurantId("restaurant-1")
                .items(List.of(
                        OrderItem.builder()
                                .name("Burger")
                                .price(100.0)
                                .quantity(2)
                                .build()
                ))
                .deliveryAddress("Meerut")
                .build();

        when(userRepository.findById("customer-1"))
                .thenReturn(Optional.of(customer));

        when(restaurantRepository.findById("restaurant-1"))
                .thenReturn(Optional.of(restaurant));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order saved = invocation.getArgument(0);
                    saved.setId("order-1");
                    return saved;
                });

        OrderResponse response = orderService.placeOrder(order);

        assertEquals("order-1", response.getOrderId());
        assertEquals(200.0, response.getTotalAmount());
        assertEquals(OrderStatus.PLACED, response.getStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_shouldThrow_whenUserIsNotCustomer() {

        User owner = User.builder()
                .id("customer-1")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        Order order = Order.builder()
                .restaurantId("restaurant-1")
                .items(List.of())
                .build();

        when(userRepository.findById("customer-1"))
                .thenReturn(Optional.of(owner));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> orderService.placeOrder(order)
        );

        assertEquals("Only Customers can place orders", ex.getMessage());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrdersForRestaurant_shouldThrow_whenOwnerDoesNotOwnRestaurant() {

        // Pretend logged in owner = owner-1
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner-1", null)
        );

        User owner = User.builder()
                .id("owner-1")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id("restaurant-1")
                .ownerId("another-owner")
                .build();

        when(userRepository.findById("owner-1"))
                .thenReturn(Optional.of(owner));

        when(restaurantRepository.findById("restaurant-1"))
                .thenReturn(Optional.of(restaurant));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> orderService.getOrdersForRestaurant("restaurant-1")
        );

        assertEquals(
                "You can only view orders for your own restaurant",
                ex.getMessage()
        );

        verify(orderRepository, never()).findByRestaurantId(any());
    }

    @Test
    void updateOrderStatus_shouldThrow_whenTransitionIsInvalid() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner-1",null)
        );

        User user = User.builder()
                .id("owner-1")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id("restaurant-1")
                .ownerId("owner-1")
                .build();

        Order order = Order.builder()
                .id("order-1")
                .restaurantId("restaurant-1")
                .status(OrderStatus.PLACED)
                .build();

        when(userRepository.findById("owner-1"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById("order-1"))
                .thenReturn(Optional.of(order));

        when(restaurantRepository.findById(order.getRestaurantId()))
                .thenReturn(Optional.of(restaurant));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> orderService.updateOrderStatus("order-1", OrderStatus.READY_FOR_PICKUP)
        );

        assertEquals(
                "Invalid status transition from PLACED to READY_FOR_PICKUP",
                ex.getMessage()
        );

        verify(orderRepository, never()).save(any());
    }
}
