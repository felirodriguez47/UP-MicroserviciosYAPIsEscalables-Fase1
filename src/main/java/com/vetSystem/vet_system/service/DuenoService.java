package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.DuenoDTO;
import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.mapper.DuenoMapper;
import com.vetSystem.vet_system.model.Dueno;
import com.vetSystem.vet_system.repository.DuenoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de negocio para Dueno.
 *
 * Refactor del Sprint 4: antes devolvia entidades Dueno, ahora devuelve
 * DuenoDTO. La conversion pasa aca adentro, dentro de la transaccion, para
 * que el Controller nunca reciba una entidad JPA ni un proxy LAZY.
 *
 * Sigue sin saber nada de HTTP: comunica errores con excepciones de dominio.
 */
@Service
@RequiredArgsConstructor
public class DuenoService {

    private final DuenoRepository duenoRepository;
    private final DuenoMapper duenoMapper;

    @Transactional(readOnly = true)
    public List<DuenoDTO> getAllDuenos() {
        return duenoMapper.toDTOList(duenoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public DuenoDTO getDuenoById(Long id) {
        return duenoMapper.toDTO(buscarOFallar(id));
    }

    @Transactional
    public DuenoDTO createDueno(DuenoDTO dto) {
        if (duenoRepository.existsByDni(dto.getDni())) {
            throw new DuplicateResourceException("Ya existe un dueno con DNI: " + dto.getDni());
        }
        Dueno dueno = duenoMapper.toEntity(dto);
        // El id llega siempre null a la entidad: el DTO puede traerlo, pero se
        // descarta para que un POST nunca pueda sobrescribir una fila existente.
        dueno.setId(null);
        return duenoMapper.toDTO(duenoRepository.save(dueno));
    }

    @Transactional
    public DuenoDTO updateDueno(Long id, DuenoDTO dto) {
        Dueno dueno = buscarOFallar(id);
        dueno.setNombre(dto.getNombre());
        dueno.setApellido(dto.getApellido());
        dueno.setTelefono(dto.getTelefono());
        dueno.setEmail(dto.getEmail());
        // El DNI no se actualiza: es el identificador de negocio del dueno.
        return duenoMapper.toDTO(duenoRepository.save(dueno));
    }

    @Transactional
    public void deleteDueno(Long id) {
        duenoRepository.delete(buscarOFallar(id));
    }

    private Dueno buscarOFallar(Long id) {
        return duenoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dueno", id));
    }
}
