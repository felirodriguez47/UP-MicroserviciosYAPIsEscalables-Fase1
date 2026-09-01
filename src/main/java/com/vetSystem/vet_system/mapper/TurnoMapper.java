package com.vetSystem.vet_system.mapper;

import com.vetSystem.vet_system.dto.TurnoResponseDTO;
import com.vetSystem.vet_system.model.Turno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Solo tiene toDTO. La direccion inversa (TurnoRequestDTO -> Turno) la hace el
 * TurnoService a mano, porque no es un mapeo de campos: hay que resolver
 * mascotaId y veterinarioId contra la base y validar que existan.
 */
@Mapper(componentModel = "spring")
public interface TurnoMapper {

    @Mapping(source = "mascota.id", target = "mascotaId")
    @Mapping(source = "mascota.nombre", target = "mascotaNombre")
    @Mapping(source = "veterinario.id", target = "veterinarioId")
    @Mapping(source = "veterinario.nombre", target = "veterinarioNombre")
    TurnoResponseDTO toDTO(Turno turno);

    List<TurnoResponseDTO> toDTOList(List<Turno> turnos);
}
