package com.medihelp360.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.Arrays;

// @Configuration  // Comentado para evitar conflicto con CorsFilter
public class CorsConfig implements WebFluxConfigurer {

    // @Override  // Comentado para evitar conflicto con CorsFilter
    // public void addCorsMappings(CorsRegistry registry) {
    //     // Comentado para evitar conflicto con CorsFilter
    // }

    // @Bean  // Comentado para evitar conflicto con CorsFilter
    // public CorsWebFilter corsWebFilter() {
    //     // Comentado para evitar conflicto con CorsFilter
    // }
}
