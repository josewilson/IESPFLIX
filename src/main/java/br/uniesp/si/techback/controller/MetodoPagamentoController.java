package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.MetodoPagamentoRequestDTO;
import br.uniesp.si.techback.dto.MetodoPagamentoResponseDTO;
import br.uniesp.si.techback.service.MetodoPagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/metodos-pagamento")
@RequiredArgsConstructor
public class MetodoPagamentoController {

    private final MetodoPagamentoService metodoPagamentoService;

    @PostMapping
    public ResponseEntity<MetodoPagamentoResponseDTO> criar(@Valid @RequestBody MetodoPagamentoRequestDTO dto) {
        MetodoPagamentoResponseDTO criado = metodoPagamentoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MetodoPagamentoResponseDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(metodoPagamentoService.listarPorUsuario(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        metodoPagamentoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
