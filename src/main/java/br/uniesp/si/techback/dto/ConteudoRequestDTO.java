package br.uniesp.si.techback.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteudoRequestDTO {

    @NotBlank
    private String titulo;

    private String tipo;

    @Min(1888)
    @Max(2100)
    private Integer ano;

    @Positive
    private Integer duracaoMinutos;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double relevancia;

    private String sinopse;
    private String trailerUrl;
    private String genero;
}
