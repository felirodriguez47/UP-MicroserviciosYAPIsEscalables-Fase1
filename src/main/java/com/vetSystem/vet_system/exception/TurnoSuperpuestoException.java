package com.vetSystem.vet_system.exception;

/**
 * Se lanza cuando un veterinario ya tiene un turno en esa fecha y hora.
 * El handler la traduce a HTTP 409 Conflict.
 *
 * Es un tipo propio y no un DuplicateResourceException porque no es un
 * duplicado de recurso: el conflicto es de agenda. Tenerla separada permite
 * darle su propio mensaje y, mas adelante, su propio tratamiento.
 */
public class TurnoSuperpuestoException extends RuntimeException {

    public TurnoSuperpuestoException(String mensaje) {
        super(mensaje);
    }
}
