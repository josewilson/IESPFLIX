package br.uniesp.si.techback.controller;

import br.uniesp.si.techback.client.ViaCepClient;
import br.uniesp.si.techback.dto.ViaCepResponseDTO;
import br.uniesp.si.techback.exception.CustomBeanException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final ViaCepClient viaCepClient;

    @GetMapping("/{cep}")
    public ResponseEntity<ViaCepResponseDTO> buscarPorCep(@PathVariable String cep) {
        String cepLimpo = cep.replaceAll("\\D", "");
        if (cepLimpo.length() != 8) {
            throw new CustomBeanException("CEP deve conter 8 dígitos");
        }
        ViaCepResponseDTO response = viaCepClient.buscarPorCep(cepLimpo);
        if (Boolean.TRUE.equals(response.getErro())) {
            throw new CustomBeanException("CEP não encontrado: " + cep);
        }
        return ResponseEntity.ok(response);
    }
}
