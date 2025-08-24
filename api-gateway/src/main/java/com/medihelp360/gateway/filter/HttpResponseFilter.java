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
 * Filtro de respuesta HTTP que intercepta a nivel más bajo
 * para limpiar headers CORS duplicados
 */
@Slf4j
@Component
@Order(Integer.MAX_VALUE - 1) // Ejecutar DESPUÉS de FinalCorsFilter pero ANTES del final
public class HttpResponseFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String origin = request.getHeaders().getFirst("Origin");
        
        log.debug("HttpResponseFilter: Intercepting HTTP response for origin: {}", origin);
        
        // Continuar con la cadena de filtros
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            
            log.debug("HttpResponseFilter: Processing HTTP response - aggressive CORS cleaning");
            
            // LIMPIEZA AGRESIVA A NIVEL HTTP: Remover TODOS los headers CORS
            response.getHeaders().remove("Access-Control-Allow-Origin");
            response.getHeaders().remove("Access-Control-Allow-Methods");
            response.getHeaders().remove("Access-Control-Allow-Headers");
            response.getHeaders().remove("Access-Control-Allow-Credentials");
            response.getHeaders().remove("Access-Control-Expose-Headers");
            response.getHeaders().remove("Access-Control-Max-Age");
            response.getHeaders().remove("Vary");
            response.getHeaders().remove("Access-Control-Request-Method");
            response.getHeaders().remove("Access-Control-Request-Headers");
            
            // Agregar headers CORS correctos DESPUÉS de la limpieza completa
            if (isAllowedOrigin(origin)) {
                addCorsHeaders(response, origin);
                log.debug("HttpResponseFilter: HTTP CORS headers added for origin: {}", origin);
            } else {
                log.warn("HttpResponseFilter: Blocked request from unauthorized origin: {}", origin);
            }
        }));
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
