package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.dto.ConteudoRequestDTO;
import br.uniesp.si.techback.dto.ConteudoResponseDTO;
import br.uniesp.si.techback.service.ConteudoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/conteudos")
@RequiredArgsConstructor
public class ConteudoController {

    private final ConteudoService conteudoService;

    @PostMapping
    public ResponseEntity<ConteudoResponseDTO> criar(@Valid @RequestBody ConteudoRequestDTO dto) {
        ConteudoResponseDTO criado = conteudoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    public ResponseEntity<Page<ConteudoResponseDTO>> listar(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(conteudoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConteudoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(conteudoService.buscarPorId(id));
    }

    @GetMapping("/genero/{genero}")
    public ResponseEntity<List<ConteudoResponseDTO>> filtrarPorGenero(@PathVariable String genero) {
        return ResponseEntity.ok(conteudoService.filtrarPorGenero(genero));
    }

    @GetMapping("/top")
    public ResponseEntity<List<ConteudoResponseDTO>> topPorRelevancia(@RequestParam(defaultValue = "10") int n) {
        return ResponseEntity.ok(conteudoService.topPorRelevancia(n));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ConteudoResponseDTO>> buscarPorTermo(@RequestParam String termo) {
        return ResponseEntity.ok(conteudoService.buscarPorTermo(termo));
    }

    @GetMapping("/lancados-apos")
    public ResponseEntity<List<ConteudoResponseDTO>> lancadosApos(@RequestParam Integer ano) {
        return ResponseEntity.ok(conteudoService.lancadosApos(ano));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConteudoResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ConteudoRequestDTO dto) {
        return ResponseEntity.ok(conteudoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        conteudoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
