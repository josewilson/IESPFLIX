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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConteudoRepository conteudoRepository;
    private final HistoricoMapper historicoMapper;

    @Transactional
    public HistoricoResponseDTO registrar(HistoricoRequestDTO dto) {
        log.info("Registrando histórico: usuarioId={}, conteudoId={}", dto.getUsuarioId(), dto.getConteudoId());
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        Conteudo conteudo = conteudoRepository.findById(dto.getConteudoId())
                .orElseThrow(() -> new EntityNotFoundException("Conteúdo não encontrado com ID: " + dto.getConteudoId()));
        Historico historico = Historico.builder()
                .usuario(usuario)
                .conteudo(conteudo)
                .progressoSegundos(dto.getProgressoSegundos())
                .concluido(dto.getConcluido() != null ? dto.getConcluido() : false)
                .build();
        HistoricoResponseDTO response = historicoMapper.toResponseDTO(historicoRepository.save(historico));
        log.info("Histórico registrado com ID={}", response.getId());
        return response;
    }

    public List<HistoricoResponseDTO> listarPorUsuario(Long usuarioId) {
        log.info("Listando histórico do usuarioId={}", usuarioId);
        return historicoRepository.findByUsuarioIdOrderByAssistidoEmDesc(usuarioId)
                .stream().map(historicoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<HistoricoResponseDTO> listarConcluidosPorUsuario(Long usuarioId) {
        log.info("Listando conteúdos concluídos do usuarioId={}", usuarioId);
        return historicoRepository.findConcluidosPorUsuario(usuarioId)
                .stream().map(historicoMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public void remover(Long id) {
        log.info("Removendo histórico ID={}", id);
        if (!historicoRepository.existsById(id)) {
            throw new EntityNotFoundException("Histórico não encontrado com ID: " + id);
        }
        historicoRepository.deleteById(id);
    }
}
