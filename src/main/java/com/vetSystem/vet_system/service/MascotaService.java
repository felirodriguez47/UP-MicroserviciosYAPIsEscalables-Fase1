package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.model.Dueno;
import com.vetSystem.vet_system.model.Mascota;
import com.vetSystem.vet_system.repository.DuenoRepository;
import com.vetSystem.vet_system.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de negocio para Mascota.
 *
 * Inyecta DuenoRepository ademas del propio: para crear una mascota hay que
 * validar que el dueno existe, y esa es una regla de negocio, no de HTTP.
 */
@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final DuenoRepository duenoRepository;

    @Transactional(readOnly = true)
    public List<Mascota> getAllMascotas() {
        return mascotaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Mascota getMascotaById(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", id));
    }

    /**
     * Mascotas de un dueno. Valida primero que el dueno exista para poder
     * distinguir "el dueno no existe" (404) de "el dueno existe pero no tiene
     * mascotas" (200 con lista vacia). Sin esta validacion, ambos casos
     * devolverian una lista vacia y el cliente no podria diferenciarlos.
     */
    @Transactional(readOnly = true)
    public List<Mascota> getMascotasByDueno(Long duenoId) {
        if (!duenoRepository.existsById(duenoId)) {
            throw new ResourceNotFoundException("Dueno", duenoId);
        }
        return mascotaRepository.findByDuenoId(duenoId);
    }

    /**
     * Crea una mascota para un dueno existente.
     *
     * El duenoId viaja por la URL (?duenoId=1) y no dentro del JSON: la mascota
     * NO puede existir sin dueno, asi que la pertenencia es parte de la ruta.
     */
    @Transactional
    public Mascota createMascota(Long duenoId, Mascota mascota) {
        Dueno dueno = duenoRepository.findById(duenoId)
                .orElseThrow(() -> new ResourceNotFoundException("Dueno", duenoId));

        if (mascotaRepository.existsByNombreAndDuenoId(mascota.getNombre(), duenoId)) {
            throw new DuplicateResourceException(
                    "El dueno " + duenoId + " ya tiene una mascota llamada " + mascota.getNombre());
        }

        // Setear el lado propietario de la relacion es OBLIGATORIO: la FK vive en
        // la tabla mascotas y Hibernate la lee de Mascota.dueno, no de la lista
        // Dueno.mascotas (que es solo el lado inverso, marcado con mappedBy).
        mascota.setDueno(dueno);
        return mascotaRepository.save(mascota);
    }

    @Transactional
    public Mascota updateMascota(Long id, Mascota datos) {
        Mascota mascota = getMascotaById(id);
        mascota.setNombre(datos.getNombre());
        mascota.setEspecie(datos.getEspecie());
        mascota.setRaza(datos.getRaza());
        mascota.setFechaNacimiento(datos.getFechaNacimiento());
        // El dueno no se cambia por PUT: una mascota no "cambia de dueno" con un
        // update de datos. Si hiciera falta, seria un endpoint propio.
        return mascotaRepository.save(mascota);
    }

    @Transactional
    public void deleteMascota(Long id) {
        mascotaRepository.delete(getMascotaById(id));
    }
}
