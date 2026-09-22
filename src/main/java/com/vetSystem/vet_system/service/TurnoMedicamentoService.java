package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.MedicamentoRecetadoDTO;
import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.exception.StockInsuficienteException;
import com.vetSystem.vet_system.mapper.TurnoMedicamentoMapper;
import com.vetSystem.vet_system.model.Medicamento;
import com.vetSystem.vet_system.model.Turno;
import com.vetSystem.vet_system.model.TurnoMedicamento;
import com.vetSystem.vet_system.repository.MedicamentoRepository;
import com.vetSystem.vet_system.repository.TurnoMedicamentoRepository;
import com.vetSystem.vet_system.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoMedicamentoService {

    private final TurnoMedicamentoRepository turnoMedicamentoRepository;
    private final TurnoRepository turnoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final TurnoMedicamentoMapper turnoMedicamentoMapper;

    @Transactional(readOnly = true)
    public List<MedicamentoRecetadoDTO> getMedicamentosDeTurno(Long turnoId) {
        if (!turnoRepository.existsById(turnoId)) {
            throw new ResourceNotFoundException("Turno", turnoId);
        }
        return turnoMedicamentoMapper.toDTOList(turnoMedicamentoRepository.findByTurnoId(turnoId));
    }

    @Transactional
    public MedicamentoRecetadoDTO recetarMedicamento(Long turnoId, Long medicamentoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno", turnoId));
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", medicamentoId));

        if (turnoMedicamentoRepository.existsByTurnoIdAndMedicamentoId(turnoId, medicamentoId)) {
            throw new DuplicateResourceException(
                    "El medicamento " + medicamentoId + " ya esta recetado en el turno " + turnoId);
        }

        if (medicamentoRepository.descontarUnaUnidad(medicamentoId) == 0) {
            throw new StockInsuficienteException(
                    "El medicamento '" + medicamento.getNombre() + "' (id " + medicamentoId + ") no tiene stock disponible");
        }

        TurnoMedicamento receta = new TurnoMedicamento(null, turno, medicamento, medicamento.getPrecioUnitario());
        return turnoMedicamentoMapper.toDTO(turnoMedicamentoRepository.save(receta));
    }
}
