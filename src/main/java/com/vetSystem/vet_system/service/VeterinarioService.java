package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.VeterinarioDTO;
import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.mapper.VeterinarioMapper;
import com.vetSystem.vet_system.model.Veterinario;
import com.vetSystem.vet_system.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * A diferencia de DuenoService (Sprint 2), este Service devuelve DTOs y no
 * entidades: la conversion ocurre aca, dentro de la transaccion, para que el
 * Controller nunca vea una entidad JPA ni un proxy LAZY.
 */
@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioMapper veterinarioMapper;

    @Transactional(readOnly = true)
    public List<VeterinarioDTO> getAllVeterinarios() {
        return veterinarioMapper.toDTOList(veterinarioRepository.findAll());
    }

    @Transactional(readOnly = true)
    public VeterinarioDTO getVeterinarioById(Long id) {
        return veterinarioMapper.toDTO(buscarOFallar(id));
    }

    @Transactional
    public VeterinarioDTO createVeterinario(VeterinarioDTO dto) {
        if (veterinarioRepository.existsByMatricula(dto.getMatricula())) {
            throw new DuplicateResourceException("Matricula ya registrada: " + dto.getMatricula());
        }
        Veterinario vet = veterinarioMapper.toEntity(dto);
        return veterinarioMapper.toDTO(veterinarioRepository.save(vet));
    }

    @Transactional
    public VeterinarioDTO updateVeterinario(Long id, VeterinarioDTO dto) {
        Veterinario vet = buscarOFallar(id);
        vet.setNombre(dto.getNombre());
        vet.setApellido(dto.getApellido());
        vet.setEspecialidad(dto.getEspecialidad());
        // La matricula no se actualiza: es el identificador profesional del
        // veterinario, igual que el DNI para el dueno.
        return veterinarioMapper.toDTO(veterinarioRepository.save(vet));
    }

    @Transactional
    public void deleteVeterinario(Long id) {
        veterinarioRepository.delete(buscarOFallar(id));
    }

    private Veterinario buscarOFallar(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", id));
    }
}
