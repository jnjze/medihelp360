package com.medihelp360.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de fallback para manejar respuestas cuando los servicios no están disponibles
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-management")
    public ResponseEntity<Map<String, Object>> userManagementFallback() {
        log.warn("User Management Service is currently unavailable - Circuit breaker activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Temporarily Unavailable");
        response.put("message", "User Management Service is currently experiencing issues. Please try again later.");
        response.put("service", "user-management-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/sync-a")
    public ResponseEntity<Map<String, Object>> syncServiceAFallback() {
        log.warn("Database Sync Service A is currently unavailable - Circuit breaker activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Temporarily Unavailable");
        response.put("message", "Database Sync Service A (PostgreSQL) is currently experiencing issues. Please try again later.");
        response.put("service", "database-sync-service-a");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/sync-b")
    public ResponseEntity<Map<String, Object>> syncServiceBFallback() {
        log.warn("Database Sync Service B is currently unavailable - Circuit breaker activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Temporarily Unavailable");
        response.put("message", "Database Sync Service B (MySQL) is currently experiencing issues. Please try again later.");
        response.put("service", "database-sync-service-b");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/sync-c")
    public ResponseEntity<Map<String, Object>> syncServiceCFallback() {
        log.warn("Database Sync Service C is currently unavailable - Circuit breaker activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Temporarily Unavailable");
        response.put("message", "Database Sync Service C (MongoDB) is currently experiencing issues. Please try again later.");
        response.put("service", "database-sync-service-c");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Fallback genérico para cualquier servicio no especificado
     */
    @GetMapping("/**")
    public ResponseEntity<Map<String, Object>> genericFallback() {
        log.warn("Generic fallback activated - Service unavailable");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Temporarily Unavailable");
        response.put("message", "The requested service is currently experiencing issues. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
} 