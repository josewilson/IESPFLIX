package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.PlanoRequestDTO;
import br.uniesp.si.techback.dto.PlanoResponseDTO;
import br.uniesp.si.techback.model.Plano;
import br.uniesp.si.techback.repository.PlanoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;

    @Transactional
    public PlanoResponseDTO criar(PlanoRequestDTO dto) {
        Plano plano = Plano.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .preco(dto.getPreco())
                .limiteDispositivos(dto.getLimiteDispositivos())
                .build();
        Plano salvo = planoRepository.save(plano);
        return toResponseDTO(salvo);
    }

    public List<PlanoResponseDTO> listar() {
        return planoRepository.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    private PlanoResponseDTO toResponseDTO(Plano p) {
        return PlanoResponseDTO.builder()
                .id(p.getId()).nome(p.getNome()).descricao(p.getDescricao())
                .preco(p.getPreco()).limiteDispositivos(p.getLimiteDispositivos())
                .build();
    }
}
