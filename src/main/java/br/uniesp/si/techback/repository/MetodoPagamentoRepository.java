package br.uniesp.si.techback.repository;

import br.uniesp.si.techback.model.MetodoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, Long> {

    @Query("SELECT m FROM MetodoPagamento m WHERE m.usuario.id = :usuarioId ORDER BY m.principal DESC")
    List<MetodoPagamento> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
