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
 * Filtro ULTIMATE de CORS que se ejecuta en el momento más bajo posible
 * Usa beforeCommit() para interceptar la respuesta justo antes de enviarla
 */
@Slf4j
@Component
@Order(Integer.MAX_VALUE) // Ejecutar DESPUÉS de todos los demás filtros
public class UltimateCorsFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String origin = request.getHeaders().getFirst("Origin");
        
        log.debug("UltimateCorsFilter: Intercepting request for origin: {}", origin);
        
        // Usar beforeCommit() para interceptar JUSTO antes de enviar la respuesta
        exchange.getResponse().beforeCommit(() -> {
            ServerHttpResponse response = exchange.getResponse();
            
            log.debug("UltimateCorsFilter: beforeCommit() - cleaning ALL CORS headers");
            
            // LIMPIEZA ULTIMATE: Remover TODOS los headers CORS
            response.getHeaders().remove("Access-Control-Allow-Origin");
            response.getHeaders().remove("Access-Control-Allow-Methods");
            response.getHeaders().remove("Access-Control-Allow-Headers");
            response.getHeaders().remove("Access-Control-Allow-Credentials");
            response.getHeaders().remove("Access-Control-Expose-Headers");
            response.getHeaders().remove("Access-Control-Max-Age");
            response.getHeaders().remove("Vary");
            response.getHeaders().remove("Access-Control-Request-Method");
            response.getHeaders().remove("Access-Control-Request-Headers");
            
            // Agregar headers CORS correctos DESPUÉS de la limpieza ultimate
            if (isAllowedOrigin(origin)) {
                addCorsHeaders(response, origin);
                log.debug("UltimateCorsFilter: Final CORS headers added for origin: {}", origin);
            } else {
                log.warn("UltimateCorsFilter: Blocked request from unauthorized origin: {}", origin);
            }
            
            return Mono.empty();
        });
        
        return chain.filter(exchange);
    }
    
    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        return origin.equals("http://localhost:4040") ||
               origin.equals("http://localhost:3000") ||
               origin.equals("http://127.0.0.1:4040") ||
               origin.equals("http://127.0.0.1:3000");
    }
    
    private void addCorsHeaders(ServerHttpResponse response, String origin) {
        response.getHeaders().add("Access-Control-Allow-Origin", origin);
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Expose-Headers", "Authorization, X-Total-Count");
    }
}
