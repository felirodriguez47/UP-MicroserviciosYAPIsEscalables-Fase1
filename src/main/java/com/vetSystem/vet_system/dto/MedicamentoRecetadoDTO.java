package com.vetSystem.vet_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoRecetadoDTO {
    private Long medicamentoId;
    private String nombre;
    private String principioActivo;
    private BigDecimal precioUnitario;
}
