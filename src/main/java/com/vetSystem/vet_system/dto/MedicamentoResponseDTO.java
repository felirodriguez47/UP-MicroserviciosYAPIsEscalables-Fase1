package com.vetSystem.vet_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoResponseDTO {
    private Long id;
    private String nombre;
    private String principioActivo;
    private Integer stock;
    private BigDecimal precioUnitario;
}
