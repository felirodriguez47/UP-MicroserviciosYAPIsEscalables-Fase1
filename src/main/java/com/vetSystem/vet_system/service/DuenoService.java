package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.model.Dueno;
import com.vetSystem.vet_system.repository.DuenoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de negocio para Dueno.
 *
 * No sabe nada de HTTP: no conoce ResponseEntity, ni codigos de estado, ni
 * @RequestBody. Comunica los errores lanzando excepciones de dominio, y es el
 * Controller el que decide como se traducen a respuestas HTTP. Gracias a eso
 * este Service se podria reusar desde un job programado o desde un consumidor
 * de mensajes sin cambiar una linea.
 */
@Service
@RequiredArgsConstructor  // Lombok genera el constructor con los campos final
public class DuenoService {

    // Constructor injection (via @RequiredArgsConstructor) en vez de @Autowired
    // sobre el campo: permite que el campo sea final (inmutable), deja explicitas
    // las dependencias en la firma del constructor, y hace la clase testeable
    // con un new DuenoService(mockRepo) sin necesitar el contexto de Spring.
    private final DuenoRepository duenoRepository;

    @Transactional(readOnly = true)
    public List<Dueno> getAllDuenos() {
        return duenoRepository.findAll();
    }

    /** Busca por ID. Lanza ResourceNotFoundException (-> 404) si no existe. */
    @Transactional(readOnly = true)
    public Dueno getDuenoById(Long id) {
        return duenoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dueno", id));
    }

    /** Crea un dueno. Lanza DuplicateResourceException (-> 409) si el DNI ya existe. */
    @Transactional
    public Dueno createDueno(Dueno dueno) {
        if (duenoRepository.existsByDni(dueno.getDni())) {
            throw new DuplicateResourceException(
                    "Ya existe un dueno con DNI: " + dueno.getDni());
        }
        return duenoRepository.save(dueno);
    }

    /**
     * Actualiza un dueno existente.
     *
     * No se hace duenoRepository.save(duenoActualizado) directo porque ese objeto
     * viene del JSON sin id y sin dni: guardarlo insertaria una fila nueva en vez
     * de actualizar. Se carga la entidad de la base y se le copian solo los campos
     * editables.
     */
    @Transactional
    public Dueno updateDueno(Long id, Dueno duenoActualizado) {
        Dueno dueno = getDuenoById(id);  // reutiliza la validacion de existencia
        dueno.setNombre(duenoActualizado.getNombre());
        dueno.setApellido(duenoActualizado.getApellido());
        dueno.setTelefono(duenoActualizado.getTelefono());
        dueno.setEmail(duenoActualizado.getEmail());
        // El DNI no se actualiza: es el identificador de negocio del dueno.
        return duenoRepository.save(dueno);
    }

    /** Elimina un dueno. Lanza ResourceNotFoundException (-> 404) si no existe. */
    @Transactional
    public void deleteDueno(Long id) {
        Dueno dueno = getDuenoById(id);  // valida que existe antes de borrar
        duenoRepository.delete(dueno);
    }
}
