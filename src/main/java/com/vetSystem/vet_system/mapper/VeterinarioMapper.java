package com.vetSystem.vet_system.mapper;

import com.vetSystem.vet_system.dto.VeterinarioDTO;
import com.vetSystem.vet_system.model.Veterinario;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VeterinarioMapper {

    VeterinarioDTO toDTO(Veterinario veterinario);

    List<VeterinarioDTO> toDTOList(List<Veterinario> veterinarios);

    Veterinario toEntity(VeterinarioDTO dto);
}
