package com.vetSystem.vet_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Mascota vista desde la API.
 *
 * En vez del objeto Dueno completo lleva su id y su nombre "aplanados".
 * El cliente casi siempre quiere mostrar el nombre del dueno, y asi se evita
 * una segunda llamada sin arrastrar toda la entidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private Long duenoId;
    private String duenoNombre;
}
