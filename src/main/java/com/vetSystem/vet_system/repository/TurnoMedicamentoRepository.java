package com.vetSystem.vet_system.repository;

import com.vetSystem.vet_system.model.TurnoMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurnoMedicamentoRepository extends JpaRepository<TurnoMedicamento, Long> {

    List<TurnoMedicamento> findByTurnoId(Long turnoId);

    boolean existsByTurnoIdAndMedicamentoId(Long turnoId, Long medicamentoId);
}
