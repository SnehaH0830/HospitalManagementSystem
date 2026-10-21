package com.hospital.appointment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class to enable and configure WebSocket message brokering.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory broker. Topics prefix represents broadcasting routes.
        config.enableSimpleBroker("/topic");
        
        // Routes destined for @MessageMapping controller endpoints
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the STOMP socket connection endpoint
        registry.addEndpoint("/ws-queue-tracker")
                .setAllowedOriginPatterns("*") // Allow all origins for dev
                .withSockJS(); // Enable SockJS fallback options
    }
}
