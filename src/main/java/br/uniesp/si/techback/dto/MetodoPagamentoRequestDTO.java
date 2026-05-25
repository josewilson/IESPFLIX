package br.uniesp.si.techback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MetodoPagamentoRequestDTO {

    @NotNull(message = "usuarioId é obrigatório")
    private Long usuarioId;

    @NotBlank(message = "tipo é obrigatório")
    private String tipo;

    @NotBlank(message = "tokenizado é obrigatório")
    private String tokenizado;

    @NotNull(message = "principal é obrigatório")
    private Boolean principal;
}
