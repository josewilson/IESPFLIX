package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.FavoritoRequestDTO;
import br.uniesp.si.techback.dto.FavoritoResponseDTO;
import br.uniesp.si.techback.mapper.FavoritoMapper;
import br.uniesp.si.techback.model.Conteudo;
import br.uniesp.si.techback.model.Favorito;
import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.ConteudoRepository;
import br.uniesp.si.techback.repository.FavoritoRepository;
import br.uniesp.si.techback.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConteudoRepository conteudoRepository;
    private final FavoritoMapper favoritoMapper;

    @Transactional
    public FavoritoResponseDTO adicionar(FavoritoRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        Conteudo conteudo = conteudoRepository.findById(dto.getConteudoId())
                .orElseThrow(() -> new EntityNotFoundException("Conteúdo não encontrado com ID: " + dto.getConteudoId()));
        Favorito favorito = Favorito.builder().usuario(usuario).conteudo(conteudo).build();
        return favoritoMapper.toResponseDTO(favoritoRepository.save(favorito));
    }

    public List<FavoritoResponseDTO> listarPorUsuario(Long usuarioId) {
        return favoritoRepository.findFavoritosRecentesPorUsuario(usuarioId)
                .stream().map(favoritoMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public void remover(Long id) {
        if (!favoritoRepository.existsById(id)) {
            throw new EntityNotFoundException("Favorito não encontrado com ID: " + id);
        }
        favoritoRepository.deleteById(id);
    }
}
