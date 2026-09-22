package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.DuenoDTO;
import com.vetSystem.vet_system.exception.DuplicateResourceException;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.mapper.DuenoMapper;
import com.vetSystem.vet_system.model.Dueno;
import com.vetSystem.vet_system.repository.DuenoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test UNITARIO: prueba DuenoService aislado. No levanta Spring ni toca MySQL.
 * El repositorio y el mapper son mocks: objetos simulados cuyo comportamiento
 * definimos en cada test con when(...).thenReturn(...).
 */
@ExtendWith(MockitoExtension.class)
class DuenoServiceTest {

    @Mock
    private DuenoRepository duenoRepository;

    @Mock
    private DuenoMapper duenoMapper;

    @InjectMocks  // crea un DuenoService real y le pasa los dos mocks por constructor
    private DuenoService duenoService;

    private final Dueno dueno =
            new Dueno(1L, "Carlos", "Gonzalez", "28543210", "1145678901", "carlos@email.com", null);
    private final DuenoDTO duenoDTO =
            new DuenoDTO(1L, "Carlos", "Gonzalez", "28543210", "1145678901", "carlos@email.com");

    @Test
    void getAllDuenos_cuandoListaVacia_retornaListaVacia() {
        // Arrange
        when(duenoRepository.findAll()).thenReturn(List.of());
        when(duenoMapper.toDTOList(List.of())).thenReturn(List.of());

        // Act
        List<DuenoDTO> resultado = duenoService.getAllDuenos();

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    void getAllDuenos_cuandoHayDatos_retornaLista() {
        // Arrange
        when(duenoRepository.findAll()).thenReturn(List.of(dueno));
        when(duenoMapper.toDTOList(List.of(dueno))).thenReturn(List.of(duenoDTO));

        // Act
        List<DuenoDTO> resultado = duenoService.getAllDuenos();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Carlos");
    }

    @Test
    void getDuenoById_cuandoExiste_retornaDTO() {
        // Arrange
        when(duenoRepository.findById(1L)).thenReturn(Optional.of(dueno));
        when(duenoMapper.toDTO(dueno)).thenReturn(duenoDTO);

        // Act
        DuenoDTO resultado = duenoService.getDuenoById(1L);

        // Assert
        assertThat(resultado.getNombre()).isEqualTo("Carlos");
        verify(duenoRepository).findById(1L);
    }

    @Test
    void getDuenoById_cuandoNoExiste_lanzaResourceNotFoundException() {
        // Arrange
        when(duenoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert: assertThrows ejecuta la lambda y exige esa excepcion
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> duenoService.getDuenoById(99L));

        assertThat(ex.getMessage()).contains("99");
        verifyNoInteractions(duenoMapper);  // no se intento mapear nada
    }

    @Test
    void createDueno_cuandoDniNuevo_guardaYRetornaDTO() {
        // Arrange
        when(duenoRepository.existsByDni("28543210")).thenReturn(false);
        when(duenoMapper.toEntity(duenoDTO)).thenReturn(dueno);
        when(duenoRepository.save(dueno)).thenReturn(dueno);
        when(duenoMapper.toDTO(dueno)).thenReturn(duenoDTO);

        // Act
        DuenoDTO resultado = duenoService.createDueno(duenoDTO);

        // Assert
        assertThat(resultado.getDni()).isEqualTo("28543210");
        verify(duenoRepository, times(1)).save(dueno);
    }

    @Test
    void createDueno_cuandoDniDuplicado_lanzaDuplicateResourceExceptionYNoGuarda() {
        // Arrange
        when(duenoRepository.existsByDni("28543210")).thenReturn(true);

        // Act + Assert
        assertThrows(DuplicateResourceException.class, () -> duenoService.createDueno(duenoDTO));

        // Lo importante: la base NO se modifico. Si alguien borra la validacion
        // del DNI, este verify es el que falla.
        verify(duenoRepository, never()).save(any());
    }
}
