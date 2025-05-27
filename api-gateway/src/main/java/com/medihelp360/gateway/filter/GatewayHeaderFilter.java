package com.medihelp360.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro global que agrega headers de identificación del API Gateway
 * a todas las requests hacia los microservicios.
 */
@Slf4j
@Component
@Order(1)
public class GatewayHeaderFilter implements GlobalFilter {

    private static final String GATEWAY_HEADER_NAME = "X-Gateway-Request";
    private static final String GATEWAY_HEADER_VALUE = "medihelp360-gateway";
    private static final String GATEWAY_SOURCE_HEADER = "X-Gateway-Source";
    private static final String GATEWAY_TIMESTAMP_HEADER = "X-Gateway-Timestamp";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        log.debug("Processing gateway request: {} {}", method, path);

        // Agregar headers de identificación del gateway
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(GATEWAY_HEADER_NAME, GATEWAY_HEADER_VALUE)
                .header(GATEWAY_SOURCE_HEADER, "api-gateway")
                .header(GATEWAY_TIMESTAMP_HEADER, String.valueOf(System.currentTimeMillis()))
                .build();

        // Crear nuevo exchange con la request modificada
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        log.debug("Added gateway headers to request: {} {}", method, path);

        return chain.filter(modifiedExchange);
    }
} 