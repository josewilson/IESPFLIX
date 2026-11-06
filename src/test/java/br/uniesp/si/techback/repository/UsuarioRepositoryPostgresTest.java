package br.uniesp.si.techback.repository;

import br.uniesp.si.techback.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Roda as migrações Flyway e o {@code ddl-auto=validate} contra um PostgreSQL real
 * (se não houver Docker, o teste é ignorado).
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UsuarioRepository com PostgreSQL real")
class UsuarioRepositoryPostgresTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private UsuarioRepository repository;

    private Usuario novo(String email, String perfil) {
        return Usuario.builder().nome("Teste").email(email).senhaHash("hash").perfil(perfil).build();
    }

    @Test
    @DisplayName("Salva e busca por e-mail")
    void salvaEBuscaPorEmail() {
        repository.saveAndFlush(novo("a@iespflix.com", "USER"));

        assertThat(repository.findByEmail("a@iespflix.com")).isPresent();
        assertThat(repository.findByEmail("nao@existe.com")).isEmpty();
    }

    @Test
    @DisplayName("E-mail duplicado viola a constraint unique do schema")
    void emailDuplicado() {
        repository.saveAndFlush(novo("dup@iespflix.com", "USER"));

        assertThatThrownBy(() -> repository.saveAndFlush(novo("dup@iespflix.com", "ADMIN")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Filtra por perfil com paginação")
    void filtraPorPerfil() {
        repository.saveAndFlush(novo("u1@iespflix.com", "USER"));
        repository.saveAndFlush(novo("adm@iespflix.com", "ADMIN"));

        var page = repository.findByPerfil("ADMIN", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Usuario::getEmail).containsExactly("adm@iespflix.com");
    }
}
