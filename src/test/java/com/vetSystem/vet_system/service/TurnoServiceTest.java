package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.TurnoRequestDTO;
import com.vetSystem.vet_system.dto.TurnoResponseDTO;
import com.vetSystem.vet_system.exception.TurnoSuperpuestoException;
import com.vetSystem.vet_system.mapper.TurnoMapper;
import com.vetSystem.vet_system.model.EstadoTurno;
import com.vetSystem.vet_system.model.Mascota;
import com.vetSystem.vet_system.model.Turno;
import com.vetSystem.vet_system.model.Veterinario;
import com.vetSystem.vet_system.repository.MascotaRepository;
import com.vetSystem.vet_system.repository.TurnoRepository;
import com.vetSystem.vet_system.repository.VeterinarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock private TurnoRepository turnoRepository;
    @Mock private MascotaRepository mascotaRepository;
    @Mock private VeterinarioRepository veterinarioRepository;
    @Mock private TurnoMapper turnoMapper;

    @InjectMocks
    private TurnoService turnoService;

    private static final LocalDate FECHA = LocalDate.of(2027, 7, 10);
    private static final LocalTime HORA = LocalTime.of(10, 30);

    private final TurnoRequestDTO request = new TurnoRequestDTO(FECHA, HORA, "Control anual", 1L, 1L);

    @Test
    void createTurno_cuandoHorarioLibre_guardaUnaVezConEstadoPendiente() {
        // Arrange: las tres validaciones pasan
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(veterinarioRepository.findById(1L)).thenReturn(Optional.of(new Veterinario()));
        when(turnoRepository.existsByVeterinarioIdAndFechaAndHora(1L, FECHA, HORA)).thenReturn(false);
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));
        when(turnoMapper.toDTO(any(Turno.class))).thenReturn(new TurnoResponseDTO());

        // Act
        turnoService.createTurno(request);

        // Assert: save se llamo exactamente una vez, y ArgumentCaptor nos deja
        // inspeccionar el Turno que el Service armo antes de guardarlo.
        ArgumentCaptor<Turno> captor = ArgumentCaptor.forClass(Turno.class);
        verify(turnoRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoTurno.PENDIENTE);
        assertThat(captor.getValue().getFecha()).isEqualTo(FECHA);
    }

    @Test
    void createTurno_cuandoHaySuperposicion_lanzaTurnoSuperpuestoExceptionYNoGuarda() {
        // Arrange
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(veterinarioRepository.findById(1L)).thenReturn(Optional.of(new Veterinario()));
        when(turnoRepository.existsByVeterinarioIdAndFechaAndHora(1L, FECHA, HORA)).thenReturn(true);

        // Act + Assert
        assertThrows(TurnoSuperpuestoException.class, () -> turnoService.createTurno(request));

        verify(turnoRepository, never()).save(any());
        verifyNoInteractions(turnoMapper);
    }
}
