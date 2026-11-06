package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.ConteudoRequestDTO;
import br.uniesp.si.techback.dto.PlanoRequestDTO;
import br.uniesp.si.techback.model.Conteudo;
import br.uniesp.si.techback.model.Plano;
import br.uniesp.si.techback.repository.ConteudoRepository;
import br.uniesp.si.techback.repository.PlanoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Cache de planos e conteúdos")
class CacheTest {

    @Autowired
    private PlanoService planoService;
    @Autowired
    private ConteudoService conteudoService;
    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private PlanoRepository planoRepository;
    @MockBean
    private ConteudoRepository conteudoRepository;

    @BeforeEach
    void limpar() {
        cacheManager.getCacheNames().forEach(n -> cacheManager.getCache(n).clear());
    }

    @Test
    @DisplayName("Listagem de planos vai ao banco só uma vez")
    void planosSaoCacheados() {
        when(planoRepository.findAll()).thenReturn(List.of(Plano.builder().id(1L).nome("Basico").build()));

        planoService.listar();
        planoService.listar();

        verify(planoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Criar plano invalida o cache da listagem")
    void criarPlanoLimpaCache() {
        when(planoRepository.findAll()).thenReturn(List.of());
        when(planoRepository.save(any(Plano.class))).thenAnswer(i -> i.getArgument(0));
        PlanoRequestDTO dto = new PlanoRequestDTO();
        dto.setNome("Premium");

        planoService.listar();
        planoService.criar(dto);
        planoService.listar();

        verify(planoRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("Busca de conteúdo por id é cacheada por id")
    void conteudoEhCacheadoPorId() {
        when(conteudoRepository.findById(1L)).thenReturn(Optional.of(Conteudo.builder().id(1L).titulo("A").build()));
        when(conteudoRepository.findById(2L)).thenReturn(Optional.of(Conteudo.builder().id(2L).titulo("B").build()));

        conteudoService.buscarPorId(1L);
        conteudoService.buscarPorId(1L);
        conteudoService.buscarPorId(2L);

        verify(conteudoRepository, times(1)).findById(1L);
        verify(conteudoRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Atualizar e deletar conteúdo invalidam a entrada do id")
    void alteracoesLimpamCache() {
        when(conteudoRepository.findById(1L)).thenReturn(Optional.of(Conteudo.builder().id(1L).titulo("A").build()));
        when(conteudoRepository.save(any(Conteudo.class))).thenAnswer(i -> i.getArgument(0));
        when(conteudoRepository.existsById(1L)).thenReturn(true);
        ConteudoRequestDTO dto = new ConteudoRequestDTO();
        dto.setTitulo("A2");

        conteudoService.buscarPorId(1L);          // findById #1 (preenche cache)
        conteudoService.atualizar(1L, dto);       // findById #2 (existência) + evict
        conteudoService.buscarPorId(1L);          // findById #3 (cache foi limpo)
        conteudoService.deletar(1L);              // evict
        conteudoService.buscarPorId(1L);          // findById #4

        verify(conteudoRepository, times(4)).findById(1L);
    }
}
