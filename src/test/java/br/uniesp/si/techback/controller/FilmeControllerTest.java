package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.FilmeDTO;
import br.uniesp.si.techback.service.FilmeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmeController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.boot.test.mock.mockito.MockBean(classes = {br.uniesp.si.techback.security.JwtUtils.class, org.springframework.security.core.userdetails.UserDetailsService.class})
@DisplayName("Testes do FilmeController")
class FilmeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmeService filmeService;

    @Autowired
    private ObjectMapper objectMapper;

    private FilmeDTO filmeDTO;
    private FilmeDTO filmeSalvoDTO;

    @BeforeEach
    void setUp() {
        filmeDTO = FilmeDTO.builder()
                .titulo("Filme de Teste")
                .sinopse("Sinopse do filme de teste")
                .dataLancamento(LocalDate.of(2023, 1, 1))
                .genero("Ação")
                .duracaoMinutos(120)
                .classificacaoIndicativa("12 anos")
                .build();

        filmeSalvoDTO = FilmeDTO.builder()
                .id(1L)
                .titulo("Filme de Teste")
                .sinopse("Sinopse do filme de teste")
                .dataLancamento(LocalDate.of(2023, 1, 1))
                .genero("Ação")
                .duracaoMinutos(120)
                .classificacaoIndicativa("12 anos")
                .build();
    }

    @Test
    @DisplayName("Deve listar todos os filmes")
    void deveListarTodosOsFilmes() throws Exception {
        List<FilmeDTO> filmes = Arrays.asList(filmeSalvoDTO);
        when(filmeService.listar()).thenReturn(filmes);

        mockMvc.perform(get("/filmes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Filme de Teste"))
                .andExpect(jsonPath("$[0].genero").value("Ação"));
    }

    @Test
    @DisplayName("Deve buscar filme por ID quando existir")
    void deveBuscarFilmePorIdQuandoExistir() throws Exception {
        when(filmeService.buscarPorId(1L)).thenReturn(filmeSalvoDTO);

        mockMvc.perform(get("/filmes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Filme de Teste"))
                .andExpect(jsonPath("$.genero").value("Ação"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando buscar filme por ID inexistente")
    void deveRetornar404QuandoBuscarFilmePorIdInexistente() throws Exception {
        when(filmeService.buscarPorId(999L)).thenThrow(new RuntimeException("Filme não encontrado com o ID: 999"));

        mockMvc.perform(get("/filmes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar um novo filme")
    void deveCriarNovoFilme() throws Exception {
        when(filmeService.salvar(any(FilmeDTO.class))).thenReturn(filmeSalvoDTO);

        mockMvc.perform(post("/filmes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmeDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Filme de Teste"))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando criar filme com dados inválidos")
    void deveRetornar400QuandoCriarFilmeComDadosInvalidos() throws Exception {
        FilmeDTO filmeInvalido = FilmeDTO.builder()
                .titulo("") // Título vazio deve falhar na validação
                .build();

        mockMvc.perform(post("/filmes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmeInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve atualizar um filme existente")
    void deveAtualizarFilmeExistente() throws Exception {
        FilmeDTO filmeAtualizado = FilmeDTO.builder()
                .id(1L)
                .titulo("Filme Atualizado")
                .sinopse("Sinopse atualizada")
                .dataLancamento(LocalDate.of(2023, 1, 1))
                .genero("Drama")
                .duracaoMinutos(150)
                .classificacaoIndicativa("16 anos")
                .build();

        when(filmeService.atualizar(eq(1L), any(FilmeDTO.class))).thenReturn(filmeAtualizado);

        mockMvc.perform(put("/filmes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmeAtualizado)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Filme Atualizado"))
                .andExpect(jsonPath("$.genero").value("Drama"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando tentar atualizar filme inexistente")
    void deveRetornar404QuandoTentarAtualizarFilmeInexistente() throws Exception {
        when(filmeService.atualizar(eq(999L), any(FilmeDTO.class)))
                .thenThrow(new RuntimeException("Falha ao atualizar: filme não encontrado com o ID: 999"));

        mockMvc.perform(put("/filmes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 400 quando atualizar filme com dados inválidos")
    void deveRetornar400QuandoAtualizarFilmeComDadosInvalidos() throws Exception {
        FilmeDTO filmeInvalido = FilmeDTO.builder()
                .titulo("") // Título vazio deve falhar na validação
                .build();

        mockMvc.perform(put("/filmes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmeInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve excluir um filme existente")
    void deveExcluirFilmeExistente() throws Exception {
        mockMvc.perform(delete("/filmes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 204 quando tentar excluir filme inexistente")
    void deveRetornar204QuandoTentarExcluirFilmeInexistente() throws Exception {
        mockMvc.perform(delete("/filmes/999"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve criar filme com dados válidos")
    void deveCriarFilmeComDadosValidos() throws Exception {
        when(filmeService.salvar(any(FilmeDTO.class))).thenReturn(filmeSalvoDTO);

        mockMvc.perform(post("/filmes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmeDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Filme de Teste"))
                .andExpect(header().exists("Location"));
    }
}
