package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.MetodoPagamentoRequestDTO;
import br.uniesp.si.techback.dto.MetodoPagamentoResponseDTO;
import br.uniesp.si.techback.model.MetodoPagamento;
import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.MetodoPagamentoRepository;
import br.uniesp.si.techback.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do MetodoPagamentoService")
class MetodoPagamentoServiceTest {

    @Mock
    private MetodoPagamentoRepository metodoPagamentoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MetodoPagamentoService service;

    private Usuario usuario;
    private MetodoPagamentoRequestDTO request;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).nome("Maria").build();
        request = new MetodoPagamentoRequestDTO();
        request.setUsuarioId(1L);
        request.setTipo("CARTAO");
        request.setTokenizado("tok_abc");
        request.setPrincipal(true);
    }

    @Test
    @DisplayName("Deve cadastrar método de pagamento para usuário existente")
    void deveCriar() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(metodoPagamentoRepository.save(any(MetodoPagamento.class))).thenAnswer(i -> {
            MetodoPagamento m = i.getArgument(0);
            m.setId(10L);
            return m;
        });

        MetodoPagamentoResponseDTO result = service.criar(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsuarioId()).isEqualTo(1L);
        assertThat(result.getNomeUsuario()).isEqualTo("Maria");
        assertThat(result.getTipo()).isEqualTo("CARTAO");
        assertThat(result.getPrincipal()).isTrue();
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar para usuário inexistente")
    void deveFalharComUsuarioInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request)).isInstanceOf(EntityNotFoundException.class);
        verify(metodoPagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar métodos do usuário")
    void deveListarPorUsuario() {
        MetodoPagamento m = MetodoPagamento.builder().id(10L).usuario(usuario)
                .tipo("PIX").tokenizado("tok").principal(false).build();
        when(metodoPagamentoRepository.findByUsuarioId(1L)).thenReturn(List.of(m));

        List<MetodoPagamentoResponseDTO> result = service.listarPorUsuario(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipo()).isEqualTo("PIX");
    }

    @Test
    @DisplayName("Deve remover método existente")
    void deveRemover() {
        when(metodoPagamentoRepository.existsById(10L)).thenReturn(true);

        service.remover(10L);

        verify(metodoPagamentoRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Deve falhar ao remover método inexistente")
    void deveFalharAoRemoverInexistente() {
        when(metodoPagamentoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.remover(99L)).isInstanceOf(EntityNotFoundException.class);
        verify(metodoPagamentoRepository, never()).deleteById(any());
    }
}
