package com.yash.delivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Client will subscribe to topics like: /topic/orders/{orderId}
        registry.enableSimpleBroker("/topic");

        // Prefix for sending messages from client → server (not needed now)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // WebSocket endpoint
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:63342")
                .withSockJS(); // fallback for browsers
    }
}