package com.vetSystem.vet_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "veterinarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true, nullable = false)
    private String matricula;

    @Column(nullable = false)
    private String especialidad;  // ej: clínica general, dermatología, cirugía
}
