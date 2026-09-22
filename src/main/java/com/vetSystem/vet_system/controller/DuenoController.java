package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.DuenoDTO;
import com.vetSystem.vet_system.dto.MascotaDTO;
import com.vetSystem.vet_system.exception.ErrorResponse;
import com.vetSystem.vet_system.service.DuenoService;
import com.vetSystem.vet_system.service.MascotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sin try-catch desde el Sprint 5: las excepciones suben hasta el
 * GlobalExceptionHandler. Desde el Sprint 7, las anotaciones OpenAPI documentan
 * cada endpoint en Swagger UI; no cambian el comportamiento.
 */
@Tag(name = "Dueños", description = "Alta, consulta, modificación y baja de dueños de mascotas")
@RestController
@RequestMapping("/api/duenos")
@RequiredArgsConstructor
public class DuenoController {

    private final DuenoService duenoService;
    private final MascotaService mascotaService;

    @Operation(summary = "Listar dueños", description = "Devuelve todos los dueños. Lista vacía si no hay ninguno.")
    @ApiResponse(responseCode = "200", description = "Lista de dueños")
    @GetMapping
    public ResponseEntity<List<DuenoDTO>> getAllDuenos() {
        return ResponseEntity.ok(duenoService.getAllDuenos());
    }

    @Operation(summary = "Listar mascotas de un dueño",
            description = "Endpoint anidado. Distingue dueño inexistente (404) de dueño sin mascotas (200 con lista vacía).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascotas del dueño"),
            @ApiResponse(responseCode = "404", description = "El dueño no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<List<MascotaDTO>> getMascotasByDueno(
            @Parameter(description = "ID del dueño", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.getMascotasByDueno(id));
    }

    @Operation(summary = "Obtener un dueño por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dueño encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DuenoDTO> getDuenoById(
            @Parameter(description = "ID del dueño", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(duenoService.getDuenoById(id));
    }

    // @Valid dispara Bean Validation ANTES de entrar al metodo.
    @Operation(summary = "Crear un dueño", description = "El DNI debe ser único. El id se ignora: lo genera la base.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dueño creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (campo vacío, email mal formado, DNI de largo incorrecto)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un dueño con ese DNI",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DuenoDTO> createDueno(@Valid @RequestBody DuenoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(duenoService.createDueno(dto));
    }

    @Operation(summary = "Actualizar un dueño",
            description = "Reemplaza nombre, apellido, teléfono y email. El DNI no se modifica aunque se envíe otro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dueño actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DuenoDTO> updateDueno(
            @Parameter(description = "ID del dueño", example = "1") @PathVariable Long id,
            @Valid @RequestBody DuenoDTO dto) {
        return ResponseEntity.ok(duenoService.updateDueno(id, dto));
    }

    @Operation(summary = "Eliminar un dueño", description = "Borra también sus mascotas (cascade), salvo que alguna tenga turnos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dueño eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Sus mascotas tienen turnos agendados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDueno(
            @Parameter(description = "ID del dueño", example = "1") @PathVariable Long id) {
        duenoService.deleteDueno(id);
        return ResponseEntity.noContent().build();
    }
}
