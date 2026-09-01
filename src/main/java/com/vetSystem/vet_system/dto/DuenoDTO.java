package com.vetSystem.vet_system.dto;

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
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;
}
