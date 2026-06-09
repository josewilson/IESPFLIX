package br.uniesp.si.techback.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoRequestDTO {

    @NotNull
    private Long usuarioId;

    @NotNull
    private Long conteudoId;

    @Min(0)
    private Integer progressoSegundos;

    private Boolean concluido;
}
