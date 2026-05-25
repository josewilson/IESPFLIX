package br.uniesp.si.techback.dto;

import lombok.Data;

@Data
public class MetodoPagamentoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nomeUsuario;
    private String tipo;
    private String tokenizado;
    private Boolean principal;
}
