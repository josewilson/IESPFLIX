package br.uniesp.si.techback.service.externo;

import br.uniesp.si.techback.dto.externo.FeriadoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrasilApiService {

    private final RestTemplate restTemplate;

    public List<FeriadoResponse> listarFeriados(Integer ano) {
        log.info("Consultando feriados do ano: {}", ano);
        String url = "https://brasilapi.com.br/api/feriados/v1/" + ano;
        FeriadoResponse[] feriados = restTemplate.getForObject(url, FeriadoResponse[].class);
        return Arrays.asList(feriados != null ? feriados : new FeriadoResponse[0]);
    }
}
