package com.vetSystem.vet_system.repository;

import com.vetSystem.vet_system.model.Dueno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Capa de acceso a datos para Dueno.
 *
 * Es una interfaz, no una clase: Spring Data JPA genera la implementacion en
 * tiempo de ejecucion (proxy dinamico). Al extender JpaRepository ya vienen
 * findAll, findById, save, delete, existsById, count, etc.
 *
 * Los metodos declarados abajo usan Query Derivation: Spring lee el nombre del
 * metodo y deduce el SQL. No hay que escribir la consulta.
 */
@Repository
public interface DuenoRepository extends JpaRepository<Dueno, Long> {

    // SELECT COUNT(*) > 0 FROM duenos WHERE dni = ?
    boolean existsByDni(String dni);

    // SELECT * FROM duenos WHERE email = ?
    // Optional porque el email puede no existir: obliga a manejar la ausencia
    // en vez de devolver null y arriesgar un NullPointerException.
    Optional<Dueno> findByEmail(String email);
}
