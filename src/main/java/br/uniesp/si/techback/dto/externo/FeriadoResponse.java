package br.uniesp.si.techback.dto.externo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FeriadoResponse {
    private String date;
    private String name;
    private String type;
}
