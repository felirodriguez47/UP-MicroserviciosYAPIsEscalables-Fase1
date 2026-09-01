package com.vetSystem.vet_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    // La matricula es obligatoria tambien en el PUT: PUT reemplaza el recurso
    // completo, asi que el cliente manda todos los campos aunque el Service
    // decida no actualizar este.
    @NotBlank(message = "La matricula es obligatoria")
    @Pattern(regexp = "MV-\\d+", message = "La matricula debe tener el formato MV-1234")
    private String matricula;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
}
