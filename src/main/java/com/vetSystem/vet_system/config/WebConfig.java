package com.vetSystem.vet_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: el navegador bloquea un fetch desde otro origen (frontend en :5500,
 * backend en :8080) salvo que el servidor responda con Access-Control-Allow-Origin.
 *
 * "*" es aceptable SOLO en desarrollo. En produccion se listan los origenes
 * exactos del frontend: con "*" cualquier sitio web podria llamar a la API
 * desde el navegador de un usuario.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*");
    }
}
