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
public class FavoritoResponseDTO {
    private Long id;
    private Long usuarioId;
    private String nomeUsuario;
    private Long conteudoId;
    private String tituloConteudo;
    private LocalDateTime adicionadoEm;
}
