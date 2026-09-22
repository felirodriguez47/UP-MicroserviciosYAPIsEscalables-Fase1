package com.vetSystem.vet_system.mapper;

import com.vetSystem.vet_system.dto.MedicamentoRequestDTO;
import com.vetSystem.vet_system.dto.MedicamentoResponseDTO;
import com.vetSystem.vet_system.model.Medicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicamentoMapper {

    MedicamentoResponseDTO toDTO(Medicamento medicamento);

    List<MedicamentoResponseDTO> toDTOList(List<Medicamento> medicamentos);

    @Mapping(target = "id", ignore = true)
    Medicamento toEntity(MedicamentoRequestDTO dto);
}
