package br.uniesp.si.techback.dto;

import jakarta.validation.constraints.Max;
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

    @NotNull(message = "O ID do usuário é obrigatório")
    private Long usuarioId;

    @NotNull(message = "O ID do conteúdo é obrigatório")
    private Long conteudoId;

    @NotNull(message = "O percentual assistido é obrigatório")
    @Min(value = 0, message = "O percentual assistido deve ser no mínimo 0")
    @Max(value = 100, message = "O percentual assistido deve ser no máximo 100")
    private Integer percentualAssistido;
}
