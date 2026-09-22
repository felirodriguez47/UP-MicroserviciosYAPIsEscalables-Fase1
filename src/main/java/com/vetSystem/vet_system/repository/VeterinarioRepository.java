package com.vetSystem.vet_system.repository;

import com.vetSystem.vet_system.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    // SELECT COUNT(*) > 0 FROM veterinarios WHERE matricula = ?
    boolean existsByMatricula(String matricula);
}
