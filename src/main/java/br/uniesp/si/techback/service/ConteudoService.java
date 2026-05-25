package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.ConteudoRequestDTO;
import br.uniesp.si.techback.dto.ConteudoResponseDTO;
import br.uniesp.si.techback.mapper.ConteudoMapper;
import br.uniesp.si.techback.model.Conteudo;
import br.uniesp.si.techback.repository.ConteudoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConteudoService {

    private final ConteudoRepository conteudoRepository;
    private final ConteudoMapper conteudoMapper;

    @Transactional
    public ConteudoResponseDTO criar(ConteudoRequestDTO dto) {
        log.info("Criando conteúdo: {}", dto.getTitulo());
        Conteudo salvo = conteudoRepository.save(conteudoMapper.toEntity(dto));
        return conteudoMapper.toResponseDTO(salvo);
    }

    public Page<ConteudoResponseDTO> listar(Pageable pageable) {
        return conteudoRepository.findAll(pageable).map(conteudoMapper::toResponseDTO);
    }

    public ConteudoResponseDTO buscarPorId(Long id) {
        Conteudo conteudo = conteudoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conteúdo não encontrado com ID: " + id));
        return conteudoMapper.toResponseDTO(conteudo);
    }

    @Transactional
    public ConteudoResponseDTO atualizar(Long id, ConteudoRequestDTO dto) {
        log.info("Atualizando conteúdo ID: {}", id);
        conteudoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conteúdo não encontrado com ID: " + id));
        Conteudo atualizado = conteudoMapper.toEntity(dto);
        atualizado.setId(id);
        return conteudoMapper.toResponseDTO(conteudoRepository.save(atualizado));
    }

    @Transactional
    public void deletar(Long id) {
        if (!conteudoRepository.existsById(id)) {
            throw new EntityNotFoundException("Conteúdo não encontrado com ID: " + id);
        }
        conteudoRepository.deleteById(id);
        log.info("Conteúdo ID {} deletado", id);
    }

    public List<ConteudoResponseDTO> filtrarPorGenero(String genero) {
        return conteudoRepository.findByGeneroCaseInsensitive(genero)
                .stream().map(conteudoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<ConteudoResponseDTO> topPorRelevancia(int n) {
        return conteudoRepository.findTopByRelevancia(Pageable.ofSize(n))
                .stream().map(conteudoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<ConteudoResponseDTO> buscarPorTermo(String termo) {
        return conteudoRepository.buscarPorTermo(termo)
                .stream().map(conteudoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<ConteudoResponseDTO> lancadosApos(Integer ano) {
        return conteudoRepository.findLancadosApos(ano)
                .stream().map(conteudoMapper::toResponseDTO).collect(Collectors.toList());
    }
}
