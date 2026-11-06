package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.UsuarioRequestDTO;
import br.uniesp.si.techback.dto.UsuarioResponseDTO;
import br.uniesp.si.techback.mapper.UsuarioMapper;
import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequestDTO request;
    private Usuario usuario;
    private UsuarioResponseDTO response;

    @BeforeEach
    void setUp() {
        request = new UsuarioRequestDTO();
        request.setNome("Maria");
        request.setEmail("maria@iespflix.com");
        request.setSenha("senha12345");
        request.setCpfCnpj("52998224725");
        request.setPerfil("USER");

        usuario = Usuario.builder().id(1L).nome("Maria").email("maria@iespflix.com").perfil("USER").build();

        response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setEmail("maria@iespflix.com");
    }

    @Test
    @DisplayName("Deve criar usuário gravando a senha com hash")
    void deveCriarUsuario() {
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(usuarioMapper.toEntity(request)).thenReturn(usuario);
        when(passwordEncoder.encode("senha12345")).thenReturn("HASH");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(response);

        UsuarioResponseDTO result = usuarioService.criar(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(usuario.getSenhaHash()).isEqualTo("HASH");
    }

    @Test
    @DisplayName("Deve rejeitar e-mail já cadastrado")
    void deveRejeitarEmailDuplicado() {
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.criar(request))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar usuários paginados")
    void deveListarPaginado() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(usuario)));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(response);

        Page<UsuarioResponseDTO> page = usuarioService.listar(pageable);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(response);

        assertThat(usuarioService.buscarPorId(1L).getEmail()).isEqualTo("maria@iespflix.com");
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID não existir")
    void deveFalharAoBuscarInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Deve atualizar dados e trocar a senha quando informada")
    void deveAtualizarComSenha() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("senha12345")).thenReturn("NOVO_HASH");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(response);
        request.setNome("Maria Silva");

        usuarioService.atualizar(1L, request);

        assertThat(usuario.getNome()).isEqualTo("Maria Silva");
        assertThat(usuario.getSenhaHash()).isEqualTo("NOVO_HASH");
    }

    @Test
    @DisplayName("Deve manter a senha atual quando a nova vier em branco")
    void deveManterSenhaQuandoEmBranco() {
        usuario.setSenhaHash("HASH_ANTIGO");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(response);
        request.setSenha(" ");

        usuarioService.atualizar(1L, request);

        assertThat(usuario.getSenhaHash()).isEqualTo("HASH_ANTIGO");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar inexistente")
    void deveFalharAoAtualizarInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizar(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Deve deletar usuário existente")
    void deveDeletar() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        usuarioService.deletar(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar inexistente")
    void deveFalharAoDeletarInexistente() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.deletar(99L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(usuarioRepository, never()).deleteById(any());
    }
}
