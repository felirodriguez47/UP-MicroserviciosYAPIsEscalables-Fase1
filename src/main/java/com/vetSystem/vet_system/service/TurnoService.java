package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.TurnoRequestDTO;
import com.vetSystem.vet_system.dto.TurnoResponseDTO;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.exception.TurnoSuperpuestoException;
import com.vetSystem.vet_system.mapper.TurnoMapper;
import com.vetSystem.vet_system.model.EstadoTurno;
import com.vetSystem.vet_system.model.Mascota;
import com.vetSystem.vet_system.model.Turno;
import com.vetSystem.vet_system.model.Veterinario;
import com.vetSystem.vet_system.repository.MascotaRepository;
import com.vetSystem.vet_system.repository.TurnoRepository;
import com.vetSystem.vet_system.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * El Service mas complejo del sistema: un Turno relaciona una Mascota con un
 * Veterinario, y tiene la primera regla de negocio real de la clinica
 * (no superponer turnos del mismo veterinario).
 */
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final TurnoMapper turnoMapper;

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> getAllTurnos() {
        return turnoMapper.toDTOList(turnoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public TurnoResponseDTO getTurnoById(Long id) {
        return turnoMapper.toDTO(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> getAgenda(Long veterinarioId, LocalDate fecha) {
        return turnoMapper.toDTOList(
                turnoRepository.findByVeterinarioIdAndFecha(veterinarioId, fecha));
    }

    /**
     * Crea un turno. El orden de las validaciones importa: primero que existan
     * las dos entidades (404), y recien despues la superposicion (409).
     * Si validaramos al reves, un turno con veterinarioId inexistente daria
     * 409 "no hay superposicion" en vez del 404 correcto.
     */
    @Transactional
    public TurnoResponseDTO createTurno(TurnoRequestDTO request) {
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", request.getMascotaId()));

        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", request.getVeterinarioId()));

        turnoRepository.findFirstByVeterinarioIdAndFechaAndHoraAndEstadoNot(
                        request.getVeterinarioId(), request.getFecha(), request.getHora(), EstadoTurno.CANCELADO)
                .ifPresent(conflicto -> {
                    throw new TurnoSuperpuestoException(
                            "El veterinario " + request.getVeterinarioId() + " ya tiene el turno "
                                    + conflicto.getId() + " el " + conflicto.getFecha() + " a las " + conflicto.getHora());
                });

        Turno turno = new Turno();
        turno.setFecha(request.getFecha());
        turno.setHora(request.getHora());
        turno.setMotivo(request.getMotivo());
        turno.setEstado(EstadoTurno.PENDIENTE);  // un turno nuevo siempre nace PENDIENTE
        turno.setMascota(mascota);
        turno.setVeterinario(veterinario);

        return turnoMapper.toDTO(turnoRepository.save(turno));
    }

    /** PATCH del estado: la unica parte del turno que cambia despues de crearlo. */
    @Transactional
    public TurnoResponseDTO actualizarEstado(Long id, EstadoTurno nuevoEstado, String observaciones) {
        Turno turno = buscarOFallar(id);
        turno.setEstado(nuevoEstado);
        if (observaciones != null) {
            turno.setObservaciones(observaciones);
        }
        return turnoMapper.toDTO(turnoRepository.save(turno));
    }

    private Turno buscarOFallar(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno", id));
    }
}
