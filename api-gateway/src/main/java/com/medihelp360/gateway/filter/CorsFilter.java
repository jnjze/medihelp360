package com.medihelp360.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Filtro global para manejar CORS que se ejecuta antes que otros filtros
 */
@Slf4j
@Component
@Order(0) // Ejecutar antes que GatewayHeaderFilter (Order 1)
public class CorsFilter implements GlobalFilter {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:4040",  // Frontend Vite
        "http://localhost:3000",  // Frontend CRA
        "http://127.0.0.1:4040", // Frontend Vite (alternativo)
        "http://127.0.0.1:3000"  // Frontend CRA (alternativo)
    );

    private static final List<String> ALLOWED_METHODS = Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS"
    );

    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
        "Origin", "Content-Type", "Accept", "Authorization", 
        "X-Requested-With", "Cache-Control", "Pragma"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        String method = request.getMethod().name();
        
        log.debug("CORS Filter: Processing request from origin: {} with method: {}", origin, method);

        // Manejar preflight OPTIONS request
        if ("OPTIONS".equals(method)) {
            return handlePreflightRequest(exchange);
        }

        // Agregar headers CORS para requests normales
        if (isAllowedOrigin(origin)) {
            addCorsHeaders(response, origin);
            log.debug("CORS Filter: Added headers for origin: {}", origin);
        } else {
            log.warn("CORS Filter: Blocked request from unauthorized origin: {}", origin);
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    private Mono<Void> handlePreflightRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        String requestMethod = request.getHeaders().getFirst("Access-Control-Request-Method");
        String requestHeaders = request.getHeaders().getFirst("Access-Control-Request-Headers");

        log.debug("CORS Filter: Handling preflight request from origin: {} for method: {} with headers: {}", 
                 origin, requestMethod, requestHeaders);

        if (isAllowedOrigin(origin)) {
            addCorsHeaders(response, origin);
            
            // Agregar headers específicos para preflight
            response.getHeaders().add("Access-Control-Allow-Methods", String.join(",", ALLOWED_METHODS));
            response.getHeaders().add("Access-Control-Allow-Headers", String.join(",", ALLOWED_HEADERS));
            response.getHeaders().add("Access-Control-Max-Age", "3600");
            
            response.setStatusCode(HttpStatus.OK);
            log.debug("CORS Filter: Preflight request allowed for origin: {}", origin);
        } else {
            log.warn("CORS Filter: Preflight request blocked for unauthorized origin: {}", origin);
            response.setStatusCode(HttpStatus.FORBIDDEN);
        }

        return response.setComplete();
    }

    private void addCorsHeaders(ServerHttpResponse response, String origin) {
        HttpHeaders headers = response.getHeaders();
        headers.add("Access-Control-Allow-Origin", origin);
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Expose-Headers", "Authorization, X-Total-Count");
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        return ALLOWED_ORIGINS.contains(origin);
    }
}
