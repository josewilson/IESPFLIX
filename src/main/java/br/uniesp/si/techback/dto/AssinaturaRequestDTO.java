package br.uniesp.si.techback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssinaturaRequestDTO {
    private Long usuarioId;
    private Long planoId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
}
