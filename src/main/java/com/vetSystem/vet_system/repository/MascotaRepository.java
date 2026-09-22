package com.vetSystem.vet_system.repository;

import com.vetSystem.vet_system.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Capa de acceso a datos para Mascota.
 *
 * Los dos metodos usan Query Derivation: Spring lee el nombre y genera el SQL.
 * "DuenoId" navega la relacion Mascota.dueno y usa su id, sin escribir el JOIN.
 */
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    // SELECT * FROM mascotas WHERE dueno_id = ?
    List<Mascota> findByDuenoId(Long duenoId);

    // SELECT COUNT(*) > 0 FROM mascotas WHERE nombre = ? AND dueno_id = ?
    // Un mismo dueno no puede tener dos mascotas con el mismo nombre.
    boolean existsByNombreAndDuenoId(String nombre, Long duenoId);

    // SELECT COUNT(*) FROM mascotas WHERE dueno_id = ?
    long countByDuenoId(Long duenoId);
}
