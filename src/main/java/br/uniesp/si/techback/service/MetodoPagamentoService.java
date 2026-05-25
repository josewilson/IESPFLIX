package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.MetodoPagamentoRequestDTO;
import br.uniesp.si.techback.dto.MetodoPagamentoResponseDTO;
import br.uniesp.si.techback.model.MetodoPagamento;
import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.MetodoPagamentoRepository;
import br.uniesp.si.techback.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetodoPagamentoService {

    private final MetodoPagamentoRepository metodoPagamentoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MetodoPagamentoResponseDTO criar(MetodoPagamentoRequestDTO dto) {
        log.info("Cadastrando método de pagamento para usuário ID: {}", dto.getUsuarioId());
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        MetodoPagamento metodo = MetodoPagamento.builder()
                .usuario(usuario)
                .tipo(dto.getTipo())
                .tokenizado(dto.getTokenizado())
                .principal(dto.getPrincipal())
                .build();
        MetodoPagamento salvo = metodoPagamentoRepository.save(metodo);
        log.info("Método de pagamento cadastrado com ID: {}", salvo.getId());
        return toResponse(salvo);
    }

    public List<MetodoPagamentoResponseDTO> listarPorUsuario(Long usuarioId) {
        log.info("Listando métodos de pagamento do usuário ID: {}", usuarioId);
        return metodoPagamentoRepository.findByUsuarioId(usuarioId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void remover(Long id) {
        if (!metodoPagamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Método de pagamento não encontrado com ID: " + id);
        }
        metodoPagamentoRepository.deleteById(id);
        log.info("Método de pagamento ID {} removido", id);
    }

    private MetodoPagamentoResponseDTO toResponse(MetodoPagamento m) {
        MetodoPagamentoResponseDTO dto = new MetodoPagamentoResponseDTO();
        dto.setId(m.getId());
        dto.setUsuarioId(m.getUsuario().getId());
        dto.setNomeUsuario(m.getUsuario().getNome());
        dto.setTipo(m.getTipo());
        dto.setTokenizado(m.getTokenizado());
        dto.setPrincipal(m.getPrincipal());
        return dto;
    }
}
