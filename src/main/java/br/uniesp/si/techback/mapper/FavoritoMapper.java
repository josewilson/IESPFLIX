package br.uniesp.si.techback.mapper;

import br.uniesp.si.techback.dto.FavoritoResponseDTO;
import br.uniesp.si.techback.model.Favorito;
import org.springframework.stereotype.Component;

@Component
public class FavoritoMapper {

    public FavoritoResponseDTO toResponseDTO(Favorito favorito) {
        if (favorito == null) return null;
        return FavoritoResponseDTO.builder()
                .id(favorito.getId())
                .usuarioId(favorito.getUsuario().getId())
                .nomeUsuario(favorito.getUsuario().getNome())
                .conteudoId(favorito.getConteudo().getId())
                .tituloConteudo(favorito.getConteudo().getTitulo())
                .adicionadoEm(favorito.getAdicionadoEm())
                .build();
    }
}
