package com.vetSystem.vet_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    // @FutureOrPresent: se puede agendar para hoy, no para ayer.
    @Schema(description = "Fecha del turno (hoy o futura), formato yyyy-MM-dd", example = "2027-07-10")
    @NotNull(message = "La fecha del turno es obligatoria")
    @FutureOrPresent(message = "No se puede agendar un turno en una fecha pasada")
    private LocalDate fecha;

    @Schema(description = "Hora del turno, formato HH:mm:ss", example = "10:30:00", type = "string")
    @NotNull(message = "La hora del turno es obligatoria")
    private LocalTime hora;

    @Schema(description = "Motivo de la consulta", example = "Control anual")
    @NotBlank(message = "El motivo de la consulta es obligatorio")
    private String motivo;

    // @Positive ademas de @NotNull: un id 0 o negativo nunca puede existir,
    // asi se rechaza antes de ir a la base.
    @Schema(description = "ID de la mascota que se atiende", example = "1")
    @NotNull(message = "El id de la mascota es obligatorio")
    @Positive(message = "El id de la mascota debe ser positivo")
    private Long mascotaId;

    @Schema(description = "ID del veterinario que atiende", example = "1")
    @NotNull(message = "El id del veterinario es obligatorio")
    @Positive(message = "El id del veterinario debe ser positivo")
    private Long veterinarioId;
}
