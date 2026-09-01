package com.vetSystem.vet_system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "mascotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String especie;   // ej: perro, gato, conejo

    private String raza;

    private LocalDate fechaNacimiento;

    // Muchas mascotas pertenecen a un dueño
    // @JoinColumn define el nombre de la FK en la tabla mascotas
    // @JsonBackReference: este es el lado "hijo" (el que ES el elemento).
    // Jackson lo OMITE al serializar, y ahi se corta el bucle
    // Dueno -> mascotas -> Mascota -> dueno -> Dueno -> ...
    @JsonBackReference
    // Excluida de toString/equals: ver comentario en Dueno.mascotas
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dueno_id", nullable = false)
    private Dueno dueno;
}
