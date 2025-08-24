package com.medihelp360.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Configuración específica para desarrollo
        corsConfig.setAllowedOrigins(Arrays.asList(
            "http://localhost:4040",  // Frontend Vite
            "http://localhost:3000",  // Frontend CRA
            "http://127.0.0.1:4040", // Frontend Vite (alternativo)
            "http://127.0.0.1:3000"  // Frontend CRA (alternativo)
        ));
        
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization", 
            "X-Requested-With", "Cache-Control", "Pragma"
        ));
        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
