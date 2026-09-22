package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.MascotaDTO;
import com.vetSystem.vet_system.exception.CupoMascotasExcedidoException;
import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.mapper.MascotaMapper;
import com.vetSystem.vet_system.model.Dueno;
import com.vetSystem.vet_system.model.Mascota;
import com.vetSystem.vet_system.repository.DuenoRepository;
import com.vetSystem.vet_system.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

    static final int MAX_MASCOTAS_POR_DUENO = 5;

    private final MascotaRepository mascotaRepository;
    private final DuenoRepository duenoRepository;
    private final MascotaMapper mascotaMapper;

    @Transactional(readOnly = true)
    public List<MascotaDTO> getAllMascotas() {
        return mascotaMapper.toDTOList(mascotaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public MascotaDTO getMascotaById(Long id) {
        return mascotaMapper.toDTO(buscarOFallar(id));
    }

    /**
     * Valida primero que el dueno exista, para poder distinguir "el dueno no
     * existe" (404) de "el dueno existe pero no tiene mascotas" (200 con lista
     * vacia). Sin la validacion, los dos casos devolverian lo mismo.
     */
    @Transactional(readOnly = true)
    public List<MascotaDTO> getMascotasByDueno(Long duenoId) {
        if (!duenoRepository.existsById(duenoId)) {
            throw new ResourceNotFoundException("Dueno", duenoId);
        }
        return mascotaMapper.toDTOList(mascotaRepository.findByDuenoId(duenoId));
    }

    @Transactional
    public MascotaDTO createMascota(Long duenoId, MascotaDTO dto) {
        Dueno dueno = duenoRepository.findById(duenoId)
                .orElseThrow(() -> new ResourceNotFoundException("Dueno", duenoId));

        if (mascotaRepository.existsByNombreAndDuenoId(dto.getNombre(), duenoId)) {
            throw new DuplicateResourceException(
                    "El dueno " + duenoId + " ya tiene una mascota llamada " + dto.getNombre());
        }

        // Toda mascota guardada cuenta como activa: el DELETE es fisico, y una mascota
        // con turnos no se puede borrar (la FK de turnos lo impide).
        long cantidad = mascotaRepository.countByDuenoId(duenoId);
        if (cantidad >= MAX_MASCOTAS_POR_DUENO) {
            throw new CupoMascotasExcedidoException("El dueno " + duenoId + " ya tiene " + cantidad
                    + " mascotas registradas y el maximo es " + MAX_MASCOTAS_POR_DUENO);
        }

        Mascota mascota = mascotaMapper.toEntity(dto);
        // Setear el lado propietario es OBLIGATORIO: la FK vive en la tabla
        // mascotas y Hibernate la lee de Mascota.dueno, no de Dueno.mascotas
        // (que es el lado inverso, marcado con mappedBy).
        mascota.setDueno(dueno);
        return mascotaMapper.toDTO(mascotaRepository.save(mascota));
    }

    @Transactional
    public MascotaDTO updateMascota(Long id, MascotaDTO dto) {
        Mascota mascota = buscarOFallar(id);
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setFechaNacimiento(dto.getFechaNacimiento());
        // El dueno no cambia por PUT: seria un cambio de titularidad, no un
        // update de datos. Si hiciera falta, iria en su propio endpoint.
        return mascotaMapper.toDTO(mascotaRepository.save(mascota));
    }

    @Transactional
    public void deleteMascota(Long id) {
        mascotaRepository.delete(buscarOFallar(id));
    }

    private Mascota buscarOFallar(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", id));
    }
}
