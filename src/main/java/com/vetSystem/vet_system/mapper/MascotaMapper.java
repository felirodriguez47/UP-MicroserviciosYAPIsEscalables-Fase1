package com.vetSystem.vet_system.mapper;

import com.vetSystem.vet_system.dto.MascotaDTO;
import com.vetSystem.vet_system.model.Mascota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MascotaMapper {

    // source con punto navega la relacion: toma Mascota.dueno.id y lo deja plano.
    @Mapping(source = "dueno.id", target = "duenoId")
    @Mapping(source = "dueno.nombre", target = "duenoNombre")
    MascotaDTO toDTO(Mascota mascota);

    List<MascotaDTO> toDTOList(List<Mascota> mascotas);

    // El dueno lo resuelve el Service buscandolo por id: el cliente no puede
    // mandar un objeto Dueno arbitrario y forzar su persistencia.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dueno", ignore = true)
    Mascota toEntity(MascotaDTO dto);
}
