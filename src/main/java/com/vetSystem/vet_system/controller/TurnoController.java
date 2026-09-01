package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.TurnoRequestDTO;
import com.vetSystem.vet_system.dto.TurnoResponseDTO;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.exception.TurnoSuperpuestoException;
import com.vetSystem.vet_system.model.EstadoTurno;
import com.vetSystem.vet_system.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<List<TurnoResponseDTO>> getAllTurnos() {
        return ResponseEntity.ok(turnoService.getAllTurnos());
    }

    /**
     * GET /api/turnos/agenda?veterinarioId=1&fecha=2026-07-10
     *
     * @DateTimeFormat le dice a Spring como parsear la fecha de la query string;
     * sin esa anotacion no sabe convertir el String "2026-07-10" a LocalDate.
     * Va antes de /{id} en el archivo, pero Spring resuelve rutas literales
     * antes que variables, asi que /agenda nunca se confunde con un id.
     */
    @GetMapping("/agenda")
    public ResponseEntity<List<TurnoResponseDTO>> getAgenda(
            @RequestParam Long veterinarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.getAgenda(veterinarioId, fecha));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTurnoById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(turnoService.getTurnoById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createTurno(@RequestBody TurnoRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.createTurno(request));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (TurnoSuperpuestoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** PATCH y no PUT: se actualiza solo una parte del turno, no el recurso entero. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                              @RequestParam EstadoTurno estado,
                                              @RequestParam(required = false) String observaciones) {
        try {
            return ResponseEntity.ok(turnoService.actualizarEstado(id, estado, observaciones));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
