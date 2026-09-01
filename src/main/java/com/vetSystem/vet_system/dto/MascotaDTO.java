package com.vetSystem.vet_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
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

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    private String especie;

    // La raza es opcional: hay mascotas mestizas.
    private String raza;

    // @PastOrPresent y no @Past: una mascota puede haber nacido hoy.
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate fechaNacimiento;

    // duenoId y duenoNombre son campos de SALIDA: el dueno se pasa por la query
    // string (?duenoId=1) al crear. Por eso no se validan.
    private Long duenoId;
    private String duenoNombre;
}
