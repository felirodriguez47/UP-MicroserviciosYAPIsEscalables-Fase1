package com.vetSystem.vet_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la API que aparecen en el encabezado de Swagger UI.
 * Los endpoints NO se registran aca: springdoc los descubre solo leyendo los
 * @RestController. Esta clase solo personaliza titulo, descripcion y version.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI vetSystemOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("vet-system API — Clínica Veterinaria Patitas Felices")
                .description("API REST para gestionar dueños, mascotas, veterinarios y turnos.")
                .version("1.0 (Fase 1 — monolito)"));
    }
}
