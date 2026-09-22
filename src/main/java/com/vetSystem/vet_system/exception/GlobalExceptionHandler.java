package com.vetSystem.vet_system.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejo centralizado de errores de toda la API.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody: intercepta las
 * excepciones que salen de CUALQUIER Controller y devuelve el cuerpo como JSON.
 *
 * Antes de esto cada Controller repetia el mismo try-catch. Ahora el formato
 * del error se cambia en un solo lugar.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Falla @Valid sobre un @RequestBody -> 400 con el detalle de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST, detalle, req);
    }

    /** Falla una validacion sobre @RequestParam / @PathVariable -> 400. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidacionParametros(HandlerMethodValidationException ex,
                                                                     HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "Parametros de la peticion invalidos", req);
    }

    /**
     * JSON mal formado, o un tipo que no se puede parsear (ej: "hora":"25:99").
     * Es un error del CLIENTE, asi que 400 y no 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonIlegible(HttpMessageNotReadableException ex,
                                                             HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no se pudo leer: JSON mal formado o tipo de dato invalido", req);
    }

    /** Un parametro de la URL con el tipo equivocado (ej: ?estado=VOLANDO) -> 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue(), req);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(ResourceNotFoundException ex,
                                                             HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicado(DuplicateResourceException ex,
                                                          HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(TurnoSuperpuestoException.class)
    public ResponseEntity<ErrorResponse> handleTurnoSuperpuesto(TurnoSuperpuestoException ex,
                                                                 HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /** Datos validos pero la regla de negocio no se puede cumplir -> 422. */
    @ExceptionHandler({StockInsuficienteException.class, CupoMascotasExcedidoException.class})
    public ResponseEntity<ErrorResponse> handleReglaDeNegocio(RuntimeException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), req);
    }

    /** Ruta que no existe (ej: GET /api/inexistente) -> 404, no 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRutaInexistente(NoResourceFoundException ex,
                                                                HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "El endpoint solicitado no existe", req);
    }

    /**
     * Verbo HTTP no soportado por el endpoint -> 405, no 500.
     * Caso real: DELETE /api/turnos/{id}. Un turno no se borra, se cancela con
     * PATCH /api/turnos/{id}/estado?estado=CANCELADO, para no perder el
     * historial clinico de la mascota.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoSoportado(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest req) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta soportado en este endpoint", req);
    }

    /**
     * Violacion de una FK en la base -> 409, no 500.
     *
     * Caso real: borrar un Dueno que tiene Mascotas con Turnos agendados.
     * Dueno.mascotas tiene cascade=ALL, asi que Hibernate intenta borrar las
     * mascotas, y MySQL lo rechaza porque turnos.mascota_id las referencia.
     * La peticion es valida, pero choca con el estado actual de los datos:
     * eso es exactamente lo que significa 409 Conflict.
     *
     * No se resuelve con un cascade hasta turnos a proposito: borrar un dueno
     * no deberia borrar en silencio el historial clinico de sus mascotas.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridad(DataIntegrityViolationException ex,
                                                           HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de integridad: el recurso esta "
                        + "referenciado por otros registros (por ejemplo, turnos agendados)", req);
    }

    /**
     * Red de seguridad: cualquier cosa que no matcheo arriba es un fallo
     * inesperado del SERVIDOR, y ahi si corresponde 500.
     *
     * No se devuelve ex.getMessage() al cliente: podria filtrar detalles
     * internos (nombres de tablas, rutas de clases). Eso va al log del servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor", req);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje,
                                                     HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
