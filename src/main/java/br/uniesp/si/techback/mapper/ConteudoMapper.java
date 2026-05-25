package br.uniesp.si.techback.mapper;

import br.uniesp.si.techback.dto.ConteudoRequestDTO;
import br.uniesp.si.techback.dto.ConteudoResponseDTO;
import br.uniesp.si.techback.model.Conteudo;
import org.springframework.stereotype.Component;

@Component
public class ConteudoMapper {

    public Conteudo toEntity(ConteudoRequestDTO dto) {
        if (dto == null) return null;
        return Conteudo.builder()
                .titulo(dto.getTitulo())
                .tipo(dto.getTipo())
                .ano(dto.getAno())
                .duracaoMinutos(dto.getDuracaoMinutos())
                .relevancia(dto.getRelevancia())
                .sinopse(dto.getSinopse())
                .trailerUrl(dto.getTrailerUrl())
                .genero(dto.getGenero())
                .build();
    }

    public ConteudoResponseDTO toResponseDTO(Conteudo conteudo) {
        if (conteudo == null) return null;
        return ConteudoResponseDTO.builder()
                .id(conteudo.getId())
                .titulo(conteudo.getTitulo())
                .tipo(conteudo.getTipo())
                .ano(conteudo.getAno())
                .duracaoMinutos(conteudo.getDuracaoMinutos())
                .relevancia(conteudo.getRelevancia())
                .sinopse(conteudo.getSinopse())
                .trailerUrl(conteudo.getTrailerUrl())
                .genero(conteudo.getGenero())
                .build();
    }
}
