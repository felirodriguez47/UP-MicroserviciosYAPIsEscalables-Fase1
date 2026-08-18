package com.vetSystem.vet_system.exception;

/**
 * Se lanza cuando se intenta crear un recurso que viola una restriccion de
 * unicidad de negocio (ej: DNI ya registrado).
 * El Controller la traduce a HTTP 409 Conflict.
 *
 * Existe como tipo propio para no tener que atrapar RuntimeException a secas
 * en el Controller: si atrapamos RuntimeException, cualquier fallo inesperado
 * (base caida, NullPointerException) se devolveria como 409, que es una
 * respuesta mentirosa. Un 409 debe significar exactamente "conflicto de datos".
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }
}
