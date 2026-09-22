package com.vetSystem.vet_system.exception;

/**
 * Se lanza cuando se pide un recurso por ID y no existe en la base.
 * El Controller la traduce a HTTP 404 Not Found.
 *
 * Extiende RuntimeException (unchecked): no obliga a declarar throws en cada
 * metodo de la cadena. Las excepciones de negocio suelen ser unchecked porque
 * el codigo intermedio no puede hacer nada util con ellas.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " con id " + id + " no fue encontrado");
    }
}
