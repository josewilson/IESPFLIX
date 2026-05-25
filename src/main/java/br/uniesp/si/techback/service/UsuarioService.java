package br.uniesp.si.techback.service;

import br.uniesp.si.techback.dto.UsuarioRequestDTO;
import br.uniesp.si.techback.dto.UsuarioResponseDTO;
import br.uniesp.si.techback.mapper.UsuarioMapper;
import br.uniesp.si.techback.model.Usuario;
import br.uniesp.si.techback.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        log.info("Criando usuário: {}", dto.getEmail());
        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new DataIntegrityViolationException("Email já cadastrado: " + dto.getEmail());
        });
        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário criado com ID: {}", salvo.getId());
        return usuarioMapper.toResponseDTO(salvo);
    }

    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(usuarioMapper::toResponseDTO);
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        log.info("Atualizando usuário ID: {}", id);
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));
        existente.setNome(dto.getNome());
        existente.setEmail(dto.getEmail());
        existente.setCpfCnpj(dto.getCpfCnpj());
        existente.setDataNascimento(dto.getDataNascimento());
        existente.setPerfil(dto.getPerfil());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            existente.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        }
        return usuarioMapper.toResponseDTO(usuarioRepository.save(existente));
    }

    @Transactional
    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
        log.info("Usuário ID {} deletado", id);
    }
}
