package com.vetSystem.vet_system.repository;

import com.vetSystem.vet_system.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    /**
     * Regla de negocio: un veterinario no puede tener dos turnos a la misma
     * hora el mismo dia.
     * SELECT COUNT(*) > 0 FROM turnos WHERE veterinario_id = ? AND fecha = ? AND hora = ?
     */
    boolean existsByVeterinarioIdAndFechaAndHora(Long veterinarioId, LocalDate fecha, LocalTime hora);

    // Agenda del dia de un veterinario.
    List<Turno> findByVeterinarioIdAndFecha(Long veterinarioId, LocalDate fecha);
}
