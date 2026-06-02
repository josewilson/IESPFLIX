package br.uniesp.si.techback.repository;

import br.uniesp.si.techback.model.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Query("select h from Historico h where h.usuario.id = :usuarioId order by h.atualizadoEm desc")
    List<Historico> findHistoricoRecentePorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select h from Historico h where h.usuario.id = :usuarioId and h.percentualAssistido = 100 order by h.atualizadoEm desc")
    List<Historico> findConteudosCompletasPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select h from Historico h where h.usuario.id = :usuarioId and h.conteudo.id = :conteudoId")
    Optional<Historico> findByUsuarioIdAndConteudoId(@Param("usuarioId") Long usuarioId, @Param("conteudoId") Long conteudoId);
}
