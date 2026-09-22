package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.MedicamentoRequestDTO;
import com.vetSystem.vet_system.dto.MedicamentoResponseDTO;
import com.vetSystem.vet_system.service.MedicamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Medicamentos", description = "Catálogo y stock de medicamentos de la clínica")
@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @GetMapping
    public ResponseEntity<List<MedicamentoResponseDTO>> getAllMedicamentos() {
        return ResponseEntity.ok(medicamentoService.getAllMedicamentos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> getMedicamentoById(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.getMedicamentoById(id));
    }

    @PostMapping
    public ResponseEntity<MedicamentoResponseDTO> createMedicamento(@Valid @RequestBody MedicamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoService.createMedicamento(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> updateMedicamento(@PathVariable Long id,
                                                                    @Valid @RequestBody MedicamentoRequestDTO dto) {
        return ResponseEntity.ok(medicamentoService.updateMedicamento(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicamento(@PathVariable Long id) {
        medicamentoService.deleteMedicamento(id);
        return ResponseEntity.noContent().build();
    }
}
