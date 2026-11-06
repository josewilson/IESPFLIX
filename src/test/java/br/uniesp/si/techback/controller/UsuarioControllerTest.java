package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.UsuarioRequestDTO;
import br.uniesp.si.techback.dto.UsuarioResponseDTO;
import br.uniesp.si.techback.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@DisplayName("Testes do UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UsuarioService usuarioService;

    private UsuarioRequestDTO request;
    private UsuarioResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new UsuarioRequestDTO();
        request.setNome("Maria");
        request.setEmail("maria@iespflix.com");
        request.setSenha("senha12345");
        request.setCpfCnpj("52998224725"); // CPF válido
        request.setPerfil("USER");

        response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setNome("Maria");
        response.setEmail("maria@iespflix.com");
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("POST /usuarios com CPF válido retorna 201 e Location")
    void deveCriarComCpfValido() throws Exception {
        when(usuarioService.criar(any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("maria@iespflix.com"));
    }

    @Test
    @DisplayName("POST /usuarios com CPF de dígitos repetidos retorna 400")
    void deveRejeitarCpfComDigitosRepetidos() throws Exception {
        request.setCpfCnpj("11111111111");

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuarios com dígito verificador do CPF errado retorna 400")
    void deveRejeitarCpfComDigitoVerificadorErrado() throws Exception {
        request.setCpfCnpj("52998224726");

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuarios com tamanho inválido de CPF/CNPJ retorna 400")
    void deveRejeitarTamanhoInvalido() throws Exception {
        request.setCpfCnpj("123");

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuarios com e-mail inválido e senha curta retorna 400")
    void deveRejeitarEmailESenhaInvalidos() throws Exception {
        request.setEmail("nao-e-email");
        request.setSenha("curta");

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /usuarios/{id} retorna 200")
    void deveBuscarPorId() throws Exception {
        when(usuarioService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria"));
    }

    @Test
    @DisplayName("GET /usuarios/{id} inexistente retorna 404")
    void deveRetornar404AoBuscarInexistente() throws Exception {
        when(usuarioService.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException("Usuário não encontrado com ID: 99"));

        mockMvc.perform(get("/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado com ID: 99"));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} inexistente retorna 404")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        when(usuarioService.atualizar(any(Long.class), any(UsuarioRequestDTO.class)))
                .thenThrow(new EntityNotFoundException("Usuário não encontrado com ID: 99"));

        mockMvc.perform(put("/usuarios/99").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} retorna 204")
    void deveDeletar() throws Exception {
        mockMvc.perform(delete("/usuarios/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} inexistente retorna 404")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        doThrow(new EntityNotFoundException("não encontrado")).when(usuarioService).deletar(99L);

        mockMvc.perform(delete("/usuarios/99")).andExpect(status().isNotFound());
    }
}
