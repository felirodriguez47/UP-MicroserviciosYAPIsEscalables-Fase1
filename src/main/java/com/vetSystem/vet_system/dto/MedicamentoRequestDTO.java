package com.vetSystem.vet_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoRequestDTO {

    @Schema(example = "Amoxicilina 500mg")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(example = "Amoxicilina")
    @NotBlank(message = "El principio activo es obligatorio")
    private String principioActivo;

    @Schema(example = "20")
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    // @Digits replica decimal(10,2) de la columna: sin esto, un precio fuera de
    // rango llegaria a MySQL y fallaria ahi en vez de devolver un 400 claro.
    @Schema(example = "1500.00")
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
    private BigDecimal precioUnitario;
}
