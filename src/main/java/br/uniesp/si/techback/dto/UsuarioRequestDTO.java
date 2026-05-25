package br.uniesp.si.techback.dto;

import br.uniesp.si.techback.validation.CpfCnpj;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank
    private String nome;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String senha;

    @CpfCnpj
    private String cpfCnpj;

    @Past
    private LocalDate dataNascimento;

    private String perfil;
}
