package com.vetSystem.vet_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que el cliente ve (y envia) de un Dueno.
 *
 * No tiene la lista de mascotas a proposito: se consultan por su propio
 * endpoint (GET /api/duenos/{id}/mascotas). Al no existir el campo, el bucle
 * de serializacion que resolvimos con Jackson en el Sprint 3 directamente no
 * puede ocurrir: no hay ciclo que cortar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuenoDTO {
    // El id NO se valida: en el POST viene null (lo genera la base) y en el
    // PUT viene por la URL, no por el body.
    @Schema(description = "ID generado por la base. Se ignora en POST y PUT", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del dueño", example = "Carlos")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(description = "Apellido del dueño", example = "González")
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    // @NotBlank y no @NotNull: " " es un String no nulo pero igualmente invalido.
    @Schema(description = "DNI sin puntos, 7 u 8 dígitos. Único e inmutable", example = "28543210")
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 7, max = 8, message = "El DNI debe tener entre 7 y 8 digitos")
    private String dni;

    // El telefono queda sin validar: es opcional en la entidad (sin nullable=false).
    @Schema(description = "Teléfono de contacto (opcional)", example = "1145678901")
    private String telefono;

    @Schema(description = "Email de contacto", example = "carlos.gonzalez@email.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    private String email;
}
