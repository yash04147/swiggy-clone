package com.yash.delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yash.delivery.security.JwtAuthenticationFilter;
import com.yash.delivery.security.JwtUtil;
import com.yash.delivery.security.SecurityConfig;
import com.yash.delivery.service.OrderService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Disabled("Security behavior differs in WebMvcTest slice")
    @Test
    void placeOrder_shouldReturn401_whenNoTokenProvided() throws Exception {

        String requestBody = """
                {
                  "restaurantId": "restaurant-1",
                  "items": [
                    {
                      "name": "Burger",
                      "price": 100,
                      "quantity": 2
                    }
                  ],
                  "deliveryAddress": "Meerut"
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}
