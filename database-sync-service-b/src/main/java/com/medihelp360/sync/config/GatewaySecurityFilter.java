package com.medihelp360.sync.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro de seguridad que restringe el acceso directo al servicio.
 * Solo permite requests que vengan del API Gateway o endpoints específicos.
 */
@Slf4j
@Component
@Order(1)
public class GatewaySecurityFilter extends OncePerRequestFilter {

    @Value("${security.development.allow-direct-access:false}")
    private boolean allowDirectAccess;

    @Value("${security.gateway.required-header.name:X-Gateway-Request}")
    private String requiredHeaderName;

    @Value("${security.gateway.required-header.value:medihelp360-gateway}")
    private String requiredHeaderValue;

    // Endpoints que siempre están permitidos (health checks, actuator, etc.)
    private static final List<String> ALLOWED_ENDPOINTS = Arrays.asList(
        "/actuator/health",
        "/actuator/info",
        "/actuator/metrics",
        "/actuator/prometheus",
        "/api/actuator/health",
        "/api/actuator/info",
        "/api/actuator/metrics",
        "/api/actuator/prometheus"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        log.debug("Processing request: {} {} from {} with User-Agent: {}", 
                 method, requestURI, remoteAddr, userAgent);

        // Permitir endpoints de monitoreo y health checks
        if (isAllowedEndpoint(requestURI)) {
            log.debug("Allowing access to monitoring endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // En modo desarrollo, permitir acceso directo
        if (allowDirectAccess) {
            log.debug("Development mode: allowing direct access to {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar header del API Gateway
        String gatewayHeader = request.getHeader(requiredHeaderName);
        if (gatewayHeader == null || !requiredHeaderValue.equals(gatewayHeader)) {
            log.warn("Unauthorized direct access attempt to {} from {} - Missing or invalid gateway header", 
                    requestURI, remoteAddr);
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "error": "Direct access forbidden",
                    "message": "This service can only be accessed through the API Gateway",
                    "timestamp": "%s",
                    "path": "%s",
                    "service": "database-sync-service-b"
                }
                """.formatted(java.time.Instant.now().toString(), requestURI));
            return;
        }

        log.debug("Valid gateway request to {}", requestURI);
        filterChain.doFilter(request, response);
    }

    /**
     * Verifica si el endpoint está en la lista de endpoints permitidos
     */
    private boolean isAllowedEndpoint(String requestURI) {
        return ALLOWED_ENDPOINTS.stream()
                .anyMatch(endpoint -> requestURI.startsWith(endpoint) || requestURI.endsWith(endpoint));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Aplicar filtro a todas las requests excepto recursos estáticos
        String path = request.getRequestURI();
        return path.startsWith("/static/") || 
               path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/");
    }
} 