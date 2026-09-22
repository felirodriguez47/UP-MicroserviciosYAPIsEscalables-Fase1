package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.TurnoRequestDTO;
import com.vetSystem.vet_system.dto.TurnoResponseDTO;
import com.vetSystem.vet_system.exception.ErrorResponse;
import com.vetSystem.vet_system.model.EstadoTurno;
import com.vetSystem.vet_system.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Turnos", description = "Agenda de la clínica: alta de turnos, consulta y cambio de estado")
@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @Operation(summary = "Listar todos los turnos")
    @ApiResponse(responseCode = "200", description = "Lista de turnos")
    @GetMapping
    public ResponseEntity<List<TurnoResponseDTO>> getAllTurnos() {
        return ResponseEntity.ok(turnoService.getAllTurnos());
    }

    /** Spring resuelve rutas literales antes que variables: /agenda nunca se confunde con /{id}. */
    @Operation(summary = "Agenda de un veterinario", description = "Turnos de un veterinario en una fecha.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turnos del día (puede estar vacía)"),
            @ApiResponse(responseCode = "400", description = "Fecha con formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/agenda")
    public ResponseEntity<List<TurnoResponseDTO>> getAgenda(
            @Parameter(description = "ID del veterinario", example = "1") @RequestParam Long veterinarioId,
            @Parameter(description = "Fecha en formato yyyy-MM-dd", example = "2027-07-10")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.getAgenda(veterinarioId, fecha));
    }

    @Operation(summary = "Obtener un turno por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> getTurnoById(
            @Parameter(description = "ID del turno", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(turnoService.getTurnoById(id));
    }

    @Operation(summary = "Agendar un turno",
            description = "El turno nace en estado PENDIENTE. Un veterinario no puede tener dos turnos la misma fecha y hora.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (fecha pasada, campos faltantes, IDs no positivos)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La mascota o el veterinario no existen",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El veterinario ya tiene un turno en ese horario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TurnoResponseDTO> createTurno(@Valid @RequestBody TurnoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.createTurno(request));
    }

    /** PATCH y no PUT: se actualiza solo una parte del turno, no el recurso entero. */
    @Operation(summary = "Cambiar el estado de un turno",
            description = "Un turno no se borra: se cancela con estado=CANCELADO para conservar el historial clínico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inexistente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TurnoResponseDTO> actualizarEstado(
            @Parameter(description = "ID del turno", example = "1") @PathVariable Long id,
            @Parameter(description = "Nuevo estado", example = "FINALIZADO") @RequestParam EstadoTurno estado,
            @Parameter(description = "Notas de la consulta", example = "Control sin novedades")
            @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(turnoService.actualizarEstado(id, estado, observaciones));
    }
}
