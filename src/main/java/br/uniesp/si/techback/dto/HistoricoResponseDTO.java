package br.uniesp.si.techback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nomeUsuario;
    private Long conteudoId;
    private String tituloConteudo;
    private String generoConteudo;
    private Integer percentualAssistido;
    private LocalDateTime assistidoEm;
    private LocalDateTime atualizadoEm;
}
