package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.PlanoRequestDTO;
import br.uniesp.si.techback.dto.PlanoResponseDTO;
import br.uniesp.si.techback.service.PlanoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;

    @PostMapping
    public ResponseEntity<PlanoResponseDTO> criar(@RequestBody PlanoRequestDTO dto) {
        PlanoResponseDTO criado = planoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<PlanoResponseDTO>> listar() {
        return ResponseEntity.ok(planoService.listar());
    }
}
