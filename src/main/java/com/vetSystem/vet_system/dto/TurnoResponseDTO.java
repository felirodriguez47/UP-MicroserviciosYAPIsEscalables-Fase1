package com.vetSystem.vet_system.dto;

import com.vetSystem.vet_system.model.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Lo que el servidor DEVUELVE de un turno.
 *
 * Trae los nombres de mascota y veterinario ademas de sus ids, para que el
 * cliente pueda mostrar la agenda sin pedir cada entidad por separado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponseDTO {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoTurno estado;
    private String observaciones;
    private Long mascotaId;
    private String mascotaNombre;
    private Long veterinarioId;
    private String veterinarioNombre;
}
