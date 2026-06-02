package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.HistoricoRequestDTO;
import br.uniesp.si.techback.dto.HistoricoResponseDTO;
import br.uniesp.si.techback.service.HistoricoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService historicoService;

    @PostMapping
    public ResponseEntity<HistoricoResponseDTO> registrar(@Valid @RequestBody HistoricoRequestDTO dto) {
        HistoricoResponseDTO criado = historicoService.registrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HistoricoResponseDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(historicoService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/completos")
    public ResponseEntity<List<HistoricoResponseDTO>> listarCompletasPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(historicoService.listarCompletasPorUsuario(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        historicoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
