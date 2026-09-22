package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.MascotaDTO;
import com.vetSystem.vet_system.service.MascotaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mascotas", description = "Mascotas y su relación con el dueño")
@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @GetMapping
    public ResponseEntity<List<MascotaDTO>> getAllMascotas() {
        return ResponseEntity.ok(mascotaService.getAllMascotas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> getMascotaById(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.getMascotaById(id));
    }

    /** POST /api/mascotas?duenoId=1 */
    @PostMapping
    public ResponseEntity<MascotaDTO> createMascota(@RequestParam Long duenoId,
                                                    @Valid @RequestBody MascotaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaService.createMascota(duenoId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> updateMascota(@PathVariable Long id,
                                                    @Valid @RequestBody MascotaDTO dto) {
        return ResponseEntity.ok(mascotaService.updateMascota(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id) {
        mascotaService.deleteMascota(id);
        return ResponseEntity.noContent().build();
    }
}
