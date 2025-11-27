package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway Application
 *
 * This is the main class for the API Gateway which serves as the single entry point
 * for all client requests and provides routing, load balancing, security, and monitoring.
 *
 * @author E-Commerce Team
 * @version 1.0.0
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * Configure custom routes for different services
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-auth", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/auth/(?<path>.*)", "/api/v1/auth/${path}")
                                .addRequestHeader("X-Gateway", "api-gateway"))
                        .uri("lb://user-service"))
                .route("user-service-users", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/users/(?<path>.*)", "/api/v1/users/${path}")
                                .addRequestHeader("X-Gateway", "api-gateway"))
                        .uri("lb://user-service"))

                // Product Service Routes
                .route("product-service", r -> r
                        .path("/api/v1/products/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/products/(?<path>.*)", "/api/v1/products/${path}")
                                .addRequestHeader("X-Gateway", "api-gateway"))
                        .uri("lb://product-service"))

                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/orders/(?<path>.*)", "/api/v1/orders/${path}")
                                .addRequestHeader("X-Gateway", "api-gateway"))
                        .uri("lb://order-service"))

                // Inventory Service Routes
                .route("inventory-service", r -> r
                        .path("/api/v1/inventory/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/inventory/(?<path>.*)", "/api/v1/inventory/${path}")
                                .addRequestHeader("X-Gateway", "api-gateway"))
                        .uri("lb://inventory-service"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/notifications/(?<path>.*)", "/api/v1/notifications/${path}")
                                .addRequestHeader("X-Gateway", "api-gateway"))
                        .uri("lb://notification-service"))

                // Health Check Routes (bypass authentication)
                .route("health-checks", r -> r
                        .path("/actuator/health/**", "/actuator/info/**")
                        .uri("lb://user-service"))

                .build();
    }
}