package com.vetSystem.vet_system.service;

import com.vetSystem.vet_system.dto.MedicamentoRequestDTO;
import com.vetSystem.vet_system.dto.MedicamentoResponseDTO;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.mapper.MedicamentoMapper;
import com.vetSystem.vet_system.model.Medicamento;
import com.vetSystem.vet_system.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    @Transactional(readOnly = true)
    public List<MedicamentoResponseDTO> getAllMedicamentos() {
        return medicamentoMapper.toDTOList(medicamentoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public MedicamentoResponseDTO getMedicamentoById(Long id) {
        return medicamentoMapper.toDTO(buscarOFallar(id));
    }

    @Transactional
    public MedicamentoResponseDTO createMedicamento(MedicamentoRequestDTO dto) {
        Medicamento medicamento = medicamentoMapper.toEntity(dto);
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamento));
    }

    @Transactional
    public MedicamentoResponseDTO updateMedicamento(Long id, MedicamentoRequestDTO dto) {
        Medicamento medicamento = buscarOFallar(id);
        medicamento.setNombre(dto.getNombre());
        medicamento.setPrincipioActivo(dto.getPrincipioActivo());
        medicamento.setStock(dto.getStock());
        medicamento.setPrecioUnitario(dto.getPrecioUnitario());
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamento));
    }

    @Transactional
    public void deleteMedicamento(Long id) {
        medicamentoRepository.delete(buscarOFallar(id));
    }

    private Medicamento buscarOFallar(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
    }
}
