package com.vetSystem.vet_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "duenos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dueno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true, nullable = false)
    private String dni;

    private String telefono;

    @Column(nullable = false)
    private String email;

    // Un dueño puede tener muchas mascotas
    // mappedBy indica que Mascota es la propietaria de la relación (tiene la FK)
    // Excluida de toString/equals: con la relación bidireccional Dueno <-> Mascota,
    // incluirla provoca recursión infinita (StackOverflowError) y fuerza la carga
    // de la colección LAZY.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "dueno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mascota> mascotas;
}
