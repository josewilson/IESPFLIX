package br.uniesp.si.techback.repository;

import br.uniesp.si.techback.model.Conteudo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {

    @Query("select c from Conteudo c where lower(c.genero) = lower(:genero)")
    List<Conteudo> findByGeneroCaseInsensitive(@Param("genero") String genero);

    @Query("select c from Conteudo c order by c.relevancia desc")
    Page<Conteudo> findTopByRelevancia(Pageable pageable);

    @Query("select c from Conteudo c where c.ano > :ano")
    List<Conteudo> findLancadosApos(@Param("ano") Integer ano);

    @Query("select c from Conteudo c where lower(c.titulo) like lower(concat('%', :termo, '%')) or lower(c.sinopse) like lower(concat('%', :termo, '%')) order by c.relevancia desc")
    List<Conteudo> buscarPorTermo(@Param("termo") String termo);
}
