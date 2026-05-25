package br.uniesp.si.techback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteudoResponseDTO {

    private Long id;
    private String titulo;
    private String tipo;
    private Integer ano;
    private Integer duracaoMinutos;
    private Double relevancia;
    private String sinopse;
    private String trailerUrl;
    private String genero;
}
