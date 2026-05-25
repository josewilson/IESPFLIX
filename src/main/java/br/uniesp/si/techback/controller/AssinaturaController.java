package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.AssinaturaRequestDTO;
import br.uniesp.si.techback.dto.AssinaturaResponseDTO;
import br.uniesp.si.techback.service.AssinaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/assinaturas")
@RequiredArgsConstructor
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    @PostMapping
    public ResponseEntity<AssinaturaResponseDTO> criar(@RequestBody AssinaturaRequestDTO dto) {
        AssinaturaResponseDTO criada = assinaturaService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<AssinaturaResponseDTO>> listarPorUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(assinaturaService.listarPorUsuario(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<AssinaturaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(assinaturaService.cancelar(id));
    }
}
