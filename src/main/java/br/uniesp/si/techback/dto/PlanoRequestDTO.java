package br.uniesp.si.techback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoRequestDTO {
    private String nome;
    private String descricao;
    private Double preco;
    private Integer limiteDispositivos;
}
