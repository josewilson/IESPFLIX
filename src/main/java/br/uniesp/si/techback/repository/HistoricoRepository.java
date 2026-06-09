package br.uniesp.si.techback.repository;

import br.uniesp.si.techback.model.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Query("select h from Historico h where h.usuario.id = :usuarioId order by h.assistidoEm desc")
    List<Historico> findByUsuarioIdOrderByAssistidoEmDesc(@Param("usuarioId") Long usuarioId);

    @Query("select h from Historico h where h.usuario.id = :usuarioId and h.concluido = true order by h.assistidoEm desc")
    List<Historico> findConcluidosPorUsuario(@Param("usuarioId") Long usuarioId);
}
