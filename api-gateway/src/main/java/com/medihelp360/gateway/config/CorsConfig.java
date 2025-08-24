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
        // DESHABILITAR CORS COMPLETAMENTE a nivel de WebFlux
        // No agregar ningún mapping de CORS
        // registry.addMapping("/**") - COMENTADO
    }

    // NO crear CorsWebFilter para evitar cualquier manejo automático de CORS
    // @Bean
    // public CorsWebFilter corsWebFilter() {
    //     return null;
    // }
    
    // DESHABILITAR CORS a nivel de configuración de WebFlux
    @Override
    public void configureHttpMessageCodecs(org.springframework.http.codec.ServerCodecConfigurer configurer) {
        // Configuración para deshabilitar CORS automático
    }
}
