package com.medihelp360.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;

/**
 * Aplicación principal del API Gateway para MediHelp360
 * Punto de entrada único para todos los microservicios
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Configuración de rutas del API Gateway
     * COMENTADO: Usar configuración en application.yml en su lugar
     */
    /*
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta para User Management Service
                .route("user-management", r -> r
                        .path("/api/users/**")
                        .uri("http://localhost:8081"))
                
                // Ruta para Database Sync Service A (PostgreSQL)
                .route("sync-service-a", r -> r
                        .path("/api/sync-a/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8082"))
                
                // Ruta para Database Sync Service B (MySQL)
                .route("sync-service-b", r -> r
                        .path("/api/sync-b/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8083"))
                
                // Ruta para Database Sync Service C (MongoDB)
                .route("sync-service-c", r -> r
                        .path("/api/sync-c/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8084"))
                
                // Ruta para health checks
                .route("health-checks", r -> r
                        .path("/health/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8081"))
                
                .build();
    }
    */
} 