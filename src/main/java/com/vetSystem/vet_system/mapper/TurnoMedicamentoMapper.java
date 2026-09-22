package com.vetSystem.vet_system.mapper;

import com.vetSystem.vet_system.dto.MedicamentoRecetadoDTO;
import com.vetSystem.vet_system.model.TurnoMedicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TurnoMedicamentoMapper {

    @Mapping(source = "medicamento.id", target = "medicamentoId")
    @Mapping(source = "medicamento.nombre", target = "nombre")
    @Mapping(source = "medicamento.principioActivo", target = "principioActivo")
    MedicamentoRecetadoDTO toDTO(TurnoMedicamento turnoMedicamento);

    List<MedicamentoRecetadoDTO> toDTOList(List<TurnoMedicamento> turnoMedicamentos);
}
