package com.medihelp360.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro PRE-CORS que se ejecuta ANTES que cualquier otro filtro
 * Su único propósito es limpiar headers CORS que Spring Cloud Gateway pueda agregar
 */
@Slf4j
@Component
@Order(-1000) // Ejecutar ANTES que cualquier otro filtro
public class PreCorsFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String origin = request.getHeaders().getFirst("Origin");
        log.debug("PreCorsFilter: Intercepting request from origin: {}", origin);
        
        // LIMPIEZA PREVENTIVA: Remover cualquier header CORS que pueda existir
        // Esto se ejecuta ANTES que cualquier otro filtro
        response.getHeaders().remove("Access-Control-Allow-Origin");
        response.getHeaders().remove("Access-Control-Allow-Methods");
        response.getHeaders().remove("Access-Control-Allow-Headers");
        response.getHeaders().remove("Access-Control-Allow-Credentials");
        response.getHeaders().remove("Access-Control-Expose-Headers");
        response.getHeaders().remove("Access-Control-Max-Age");
        response.getHeaders().remove("Vary");
        response.getHeaders().remove("Access-Control-Request-Method");
        response.getHeaders().remove("Access-Control-Request-Headers");
        
        log.debug("PreCorsFilter: Cleaned all CORS headers, proceeding with chain");
        
        return chain.filter(exchange);
    }
}
