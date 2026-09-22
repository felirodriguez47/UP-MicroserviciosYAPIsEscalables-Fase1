package com.vetSystem.vet_system.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Estructura unica de TODAS las respuestas de error de la API.
 *
 * Que el error siempre tenga la misma forma es parte del contrato: el cliente
 * puede escribir un solo bloque de manejo de errores en vez de uno por endpoint.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;        // 404
    private String error;      // "Not Found"
    private String mensaje;    // "Dueno con id 99 no fue encontrado"
    private String path;       // "/api/duenos/99"
}
