package br.uniesp.si.techback.security;

import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Autenticação JWT (ponta a ponta, H2)")
class AuthIntegrationTest {

    private static final String EMAIL = "auth@iespflix.com";
    private static final String SENHA = "senha12345";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        usuarioRepository.save(Usuario.builder().nome("Auth").email(EMAIL)
                .senhaHash(encoder.encode(SENHA)).perfil("USER").build());
    }

    private String login(String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Login válido devolve token e o token abre rota protegida")
    void loginEAcessoComToken() throws Exception {
        var resp = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(resp);
        assertThat(json.get("tipo").asText()).isEqualTo("Bearer");
        String token = json.get("token").asText();

        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Senha errada retorna 401")
    void senhaErrada() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"senha\":\"errada123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rota protegida sem token retorna 401")
    void semToken() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token adulterado retorna 401")
    void tokenInvalido() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }
}
