package br.uniesp.si.techback.dto;

public record LoginResponseDTO(String token, String tipo, long expiraEmMs) {
}
