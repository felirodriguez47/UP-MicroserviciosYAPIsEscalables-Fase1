package com.vetSystem.vet_system.repository;

import com.vetSystem.vet_system.model.EstadoTurno;
import com.vetSystem.vet_system.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Devuelve el turno que choca (no solo true/false) para poder informarlo en el 409.
    // Los CANCELADOS no ocupan el horario: si no, un turno cancelado bloquearia ese horario para siempre.
    Optional<Turno> findFirstByVeterinarioIdAndFechaAndHoraAndEstadoNot(
            Long veterinarioId, LocalDate fecha, LocalTime hora, EstadoTurno estado);

    // Agenda del dia de un veterinario.
    List<Turno> findByVeterinarioIdAndFecha(Long veterinarioId, LocalDate fecha);
}
