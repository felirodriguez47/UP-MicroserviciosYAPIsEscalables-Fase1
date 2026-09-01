package com.vetSystem.vet_system.mapper;

import com.vetSystem.vet_system.dto.DuenoDTO;
import com.vetSystem.vet_system.model.Dueno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * componentModel = "spring" hace que MapStruct genere la implementacion como
 * un @Component, para poder inyectarla en el Service por constructor.
 * El codigo generado esta en target/generated-sources/annotations/.
 */
@Mapper(componentModel = "spring")
public interface DuenoMapper {

    DuenoDTO toDTO(Dueno dueno);

    List<DuenoDTO> toDTOList(List<Dueno> duenos);

    // La coleccion se ignora: el DTO no la tiene y la mascota se asocia por su
    // propio endpoint. Sin este ignore, MapStruct avisa del campo sin mapear.
    @Mapping(target = "mascotas", ignore = true)
    Dueno toEntity(DuenoDTO dto);
}
