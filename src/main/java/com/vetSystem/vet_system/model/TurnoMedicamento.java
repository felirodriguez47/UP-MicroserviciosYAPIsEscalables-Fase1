package com.vetSystem.vet_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "turno_medicamentos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"turno_id", "medicamento_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    // Precio al momento de recetar: si despues cambia Medicamento.precioUnitario,
    // lo ya vendido en este turno no se modifica.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
}
