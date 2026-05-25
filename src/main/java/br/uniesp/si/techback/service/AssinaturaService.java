package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.AssinaturaRequestDTO;
import br.uniesp.si.techback.dto.AssinaturaResponseDTO;
import br.uniesp.si.techback.model.Assinatura;
import br.uniesp.si.techback.model.Plano;
import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.AssinaturaRepository;
import br.uniesp.si.techback.repository.PlanoRepository;
import br.uniesp.si.techback.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssinaturaService {

    private final AssinaturaRepository assinaturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;

    @Transactional
    public AssinaturaResponseDTO criar(AssinaturaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        Plano plano = planoRepository.findById(dto.getPlanoId())
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado com ID: " + dto.getPlanoId()));
        Assinatura assinatura = Assinatura.builder()
                .usuario(usuario).plano(plano)
                .dataInicio(dto.getDataInicio()).dataFim(dto.getDataFim())
                .status("ATIVA")
                .build();
        return toResponseDTO(assinaturaRepository.save(assinatura));
    }

    public List<AssinaturaResponseDTO> listarPorUsuario(Long usuarioId) {
        return assinaturaRepository.findByUsuarioId(usuarioId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public AssinaturaResponseDTO cancelar(Long id) {
        Assinatura assinatura = assinaturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura não encontrada com ID: " + id));
        assinatura.setStatus("CANCELADA");
        return toResponseDTO(assinaturaRepository.save(assinatura));
    }

    private AssinaturaResponseDTO toResponseDTO(Assinatura a) {
        return AssinaturaResponseDTO.builder()
                .id(a.getId())
                .usuarioId(a.getUsuario().getId())
                .planoId(a.getPlano().getId())
                .dataInicio(a.getDataInicio())
                .dataFim(a.getDataFim())
                .status(a.getStatus())
                .build();
    }
}
