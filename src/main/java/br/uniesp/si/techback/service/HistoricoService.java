package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.HistoricoRequestDTO;
import br.uniesp.si.techback.dto.HistoricoResponseDTO;
import br.uniesp.si.techback.mapper.HistoricoMapper;
import br.uniesp.si.techback.model.Conteudo;
import br.uniesp.si.techback.model.Historico;
import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.ConteudoRepository;
import br.uniesp.si.techback.repository.HistoricoRepository;
import br.uniesp.si.techback.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConteudoRepository conteudoRepository;
    private final HistoricoMapper historicoMapper;

    @Transactional
    public HistoricoResponseDTO registrar(HistoricoRequestDTO dto) {
        log.info("Registrando histórico: usuarioId={}, conteudoId={}, percentual={}",
                dto.getUsuarioId(), dto.getConteudoId(), dto.getPercentualAssistido());

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));

        Conteudo conteudo = conteudoRepository.findById(dto.getConteudoId())
                .orElseThrow(() -> new EntityNotFoundException("Conteúdo não encontrado com ID: " + dto.getConteudoId()));

        Optional<Historico> existente = historicoRepository
                .findByUsuarioIdAndConteudoId(dto.getUsuarioId(), dto.getConteudoId());

        Historico historico;
        if (existente.isPresent()) {
            historico = existente.get();
            historico.setPercentualAssistido(dto.getPercentualAssistido());
            log.info("Atualizando progresso do histórico ID: {}", historico.getId());
        } else {
            historico = Historico.builder()
                    .usuario(usuario)
                    .conteudo(conteudo)
                    .percentualAssistido(dto.getPercentualAssistido())
                    .build();
            log.info("Criando novo registro de histórico");
        }

        return historicoMapper.toResponseDTO(historicoRepository.save(historico));
    }

    public List<HistoricoResponseDTO> listarPorUsuario(Long usuarioId) {
        log.info("Listando histórico do usuarioId={}", usuarioId);
        return historicoRepository.findHistoricoRecentePorUsuario(usuarioId)
                .stream().map(historicoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<HistoricoResponseDTO> listarCompletasPorUsuario(Long usuarioId) {
        log.info("Listando conteúdos completamente assistidos pelo usuarioId={}", usuarioId);
        return historicoRepository.findConteudosCompletasPorUsuario(usuarioId)
                .stream().map(historicoMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public void remover(Long id) {
        if (!historicoRepository.existsById(id)) {
            throw new EntityNotFoundException("Histórico não encontrado com ID: " + id);
        }
        historicoRepository.deleteById(id);
        log.info("Histórico ID {} removido", id);
    }
}
