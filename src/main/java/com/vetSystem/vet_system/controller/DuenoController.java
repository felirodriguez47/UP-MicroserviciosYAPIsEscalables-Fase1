package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.DuenoDTO;
import com.vetSystem.vet_system.dto.MascotaDTO;
import com.vetSystem.vet_system.service.DuenoService;
import com.vetSystem.vet_system.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sin try-catch desde el Sprint 5: las excepciones suben hasta el
 * GlobalExceptionHandler, que las traduce a HTTP con formato uniforme.
 * El Controller quedo con su unica responsabilidad: recibir, delegar, responder.
 */
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
    public ResponseEntity<List<MascotaDTO>> getMascotasByDueno(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.getMascotasByDueno(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuenoDTO> getDuenoById(@PathVariable Long id) {
        return ResponseEntity.ok(duenoService.getDuenoById(id));
    }

    // @Valid dispara Bean Validation ANTES de entrar al metodo: si el DTO no
    // cumple, este codigo nunca se ejecuta y el Service nunca se llama.
    @PostMapping
    public ResponseEntity<DuenoDTO> createDueno(@Valid @RequestBody DuenoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(duenoService.createDueno(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DuenoDTO> updateDueno(@PathVariable Long id, @Valid @RequestBody DuenoDTO dto) {
        return ResponseEntity.ok(duenoService.updateDueno(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDueno(@PathVariable Long id) {
        duenoService.deleteDueno(id);
        return ResponseEntity.noContent().build();
    }
}
