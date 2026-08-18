package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.model.Dueno;
import com.vetSystem.vet_system.service.DuenoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Capa de presentacion: traduce HTTP <-> dominio.
 *
 * No tiene logica de negocio. Recibe la peticion, delega en el Service, y
 * convierte el resultado (o la excepcion) en una respuesta HTTP con el codigo
 * correcto. Toda la validacion de reglas vive en DuenoService.
 */
@RestController
@RequestMapping("/api/duenos")
@RequiredArgsConstructor
public class DuenoController {

    private final DuenoService duenoService;

    /** GET /api/duenos -> 200 OK con la lista completa (puede venir vacia). */
    @GetMapping
    public ResponseEntity<List<Dueno>> getAllDuenos() {
        return ResponseEntity.ok(duenoService.getAllDuenos());
    }

    /** GET /api/duenos/{id} -> 200 OK, o 404 Not Found si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDuenoById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(duenoService.getDuenoById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /** POST /api/duenos -> 201 Created, o 409 Conflict si el DNI ya existe. */
    @PostMapping
    public ResponseEntity<?> createDueno(@RequestBody Dueno dueno) {
        try {
            Dueno nuevo = duenoService.createDueno(dueno);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (DuplicateResourceException e) {
            // Se atrapa el tipo concreto y no RuntimeException: si la base se cae
            // durante el save, ese fallo debe salir como 500, no disfrazado de 409.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** PUT /api/duenos/{id} -> 200 OK con el recurso actualizado, o 404. */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDueno(@PathVariable Long id, @RequestBody Dueno dueno) {
        try {
            return ResponseEntity.ok(duenoService.updateDueno(id, dueno));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /** DELETE /api/duenos/{id} -> 204 No Content, o 404 si no existe. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDueno(@PathVariable Long id) {
        try {
            duenoService.deleteDueno(id);
            // 204 y no 200: la operacion salio bien pero no hay cuerpo que devolver.
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
