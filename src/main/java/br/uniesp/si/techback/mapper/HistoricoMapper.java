package br.uniesp.si.techback.mapper;

import br.uniesp.si.techback.dto.HistoricoResponseDTO;
import br.uniesp.si.techback.model.Historico;
import org.springframework.stereotype.Component;

@Component
public class HistoricoMapper {

    public HistoricoResponseDTO toResponseDTO(Historico historico) {
        if (historico == null) return null;
        return HistoricoResponseDTO.builder()
                .id(historico.getId())
                .usuarioId(historico.getUsuario().getId())
                .nomeUsuario(historico.getUsuario().getNome())
                .conteudoId(historico.getConteudo().getId())
                .tituloConteudo(historico.getConteudo().getTitulo())
                .generoConteudo(historico.getConteudo().getGenero())
                .percentualAssistido(historico.getPercentualAssistido())
                .assistidoEm(historico.getAssistidoEm())
                .atualizadoEm(historico.getAtualizadoEm())
                .build();
    }
}
