package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.externo.FeriadoResponse;
import br.uniesp.si.techback.service.externo.BrasilApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feriados")
@RequiredArgsConstructor
public class FeriadoController {

    private final BrasilApiService brasilApiService;

    @GetMapping("/{ano}")
    public ResponseEntity<List<FeriadoResponse>> listarFeriados(@PathVariable Integer ano) {
        return ResponseEntity.ok(brasilApiService.listarFeriados(ano));
    }
}
