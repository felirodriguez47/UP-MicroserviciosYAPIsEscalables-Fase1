package com.vetSystem.vet_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Lo que el cliente ENVIA para pedir un turno.
 *
 * No incluye "estado": un turno nuevo siempre nace PENDIENTE, y dejar que el
 * cliente lo mande permitiria crear un turno ya FINALIZADO. La entrada y la
 * salida son DTOs distintos justamente por casos como este.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequestDTO {
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private Long mascotaId;
    private Long veterinarioId;
}
