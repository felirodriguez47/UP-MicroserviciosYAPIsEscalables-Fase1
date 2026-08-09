package com.vetSystem.vet_system.model;

public enum EstadoTurno {
    PENDIENTE,      // El turno fue agendado pero aún no ocurrió
    EN_CURSO,       // El veterinario está atendiendo
    FINALIZADO,     // La consulta terminó
    CANCELADO       // El turno fue cancelado
}
