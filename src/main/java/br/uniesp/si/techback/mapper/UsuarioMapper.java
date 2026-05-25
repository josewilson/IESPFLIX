package br.uniesp.si.techback.mapper;

import br.uniesp.si.techback.dto.UsuarioRequestDTO;
import br.uniesp.si.techback.dto.UsuarioResponseDTO;
import br.uniesp.si.techback.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequestDTO dto) {
        if (dto == null) return null;
        return Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpfCnpj(dto.getCpfCnpj())
                .dataNascimento(dto.getDataNascimento())
                .perfil(dto.getPerfil())
                .build();
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) return null;
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpfCnpj(usuario.getCpfCnpj())
                .dataNascimento(usuario.getDataNascimento())
                .perfil(usuario.getPerfil())
                .criadoEm(usuario.getCriadoEm())
                .build();
    }
}
