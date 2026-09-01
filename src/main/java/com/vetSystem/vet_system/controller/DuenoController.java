package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.DuenoDTO;
import com.vetSystem.vet_system.dto.MascotaDTO;
import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.service.DuenoService;
import com.vetSystem.vet_system.service.MascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duenos")
@RequiredArgsConstructor
public class DuenoController {

    private final DuenoService duenoService;
    private final MascotaService mascotaService;

    @GetMapping
    public ResponseEntity<List<DuenoDTO>> getAllDuenos() {
        return ResponseEntity.ok(duenoService.getAllDuenos());
    }

    /** Endpoint anidado: la URL expresa la pertenencia mascota -> dueno. */
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<?> getMascotasByDueno(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mascotaService.getMascotasByDueno(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDuenoById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(duenoService.getDuenoById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDueno(@RequestBody DuenoDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(duenoService.createDueno(dto));
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDueno(@PathVariable Long id, @RequestBody DuenoDTO dto) {
        try {
            return ResponseEntity.ok(duenoService.updateDueno(id, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDueno(@PathVariable Long id) {
        try {
            duenoService.deleteDueno(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
