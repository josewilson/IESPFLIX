package br.uniesp.si.techback.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 – Dados inválidos na requisição (falha de @Valid / @Validated)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<Map<String, String>> erros = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> {
                    Map<String, String> erro = new LinkedHashMap<>();
                    erro.put("campo", fe.getField());
                    erro.put("valor_informado", fe.getRejectedValue() == null ? "nulo" : fe.getRejectedValue().toString());
                    erro.put("mensagem", fe.getDefaultMessage());
                    return erro;
                })
                .collect(Collectors.toList());

        return build(HttpStatus.BAD_REQUEST,
                "Dados inválidos. Corrija os campos indicados antes de tentar novamente.",
                request, Map.of("campos_com_erro", erros));
    }

    // 400 – JSON mal formado ou tipo incompatível no corpo da requisição
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("JSON inválido recebido: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST,
                "O corpo da requisição está malformado ou contém um tipo de dado inválido. " +
                        "Verifique se o JSON está correto (datas no formato yyyy-MM-dd, números sem aspas, etc.).",
                request, null);
    }

    // 400 – Parâmetro de query obrigatório ausente
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: '" + ex.getParameterName() +
                        "' (tipo esperado: " + ex.getParameterType() + ").",
                request, null);
    }

    // 400 – Tipo errado em parâmetro de path ou query (ex: texto onde deveria ser long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String tipoEsperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido";
        return build(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + ex.getName() + "' recebeu o valor '" + ex.getValue() +
                        "', mas esperava um valor do tipo " + tipoEsperado + ".",
                request, null);
    }

    // 400 – Regra de negócio da aplicação
    @ExceptionHandler(CustomBeanException.class)
    public ResponseEntity<Map<String, Object>> handleCustomBeanException(
            CustomBeanException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    // 404 – Entidade não encontrada no banco
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    // 404 – Rota inexistente
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND,
                "O endpoint '" + request.getMethod() + " " + request.getRequestURI() + "' não existe. " +
                        "Consulte a documentação em /swagger-ui/index.html.",
                request, null);
    }

    // 405 – Metodo HTTP não suportado (ex: DELETE num endpoint que só aceita GET)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String permitidos = ex.getSupportedMethods() != null
                ? String.join(", ", ex.getSupportedMethods()) : "não informados";
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "O método " + ex.getMethod() + " não é permitido para este endpoint. " +
                        "Métodos aceitos: " + permitidos + ".",
                request, null);
    }

    // 409 – Violação de unicidade no banco (email duplicado, cpf duplicado, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT,
                "Já existe um registro com esses dados. Verifique campos únicos como e-mail ou CPF/CNPJ.",
                request, null);
    }

    // 500 – Qualquer erro não tratado (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Erro interno não tratado em {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Tente novamente mais tarde. " +
                        "Se o problema persistir, entre em contato com o suporte.",
                request, null);
    }

    // Metodo utilitário — monta o corpo padronizado da resposta de erro
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message,
                                                      HttpServletRequest request,
                                                      Map<String, Object> extras) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("erro", status.getReasonPhrase());
        body.put("mensagem", message);
        body.put("path", request.getRequestURI());
        if (extras != null) body.putAll(extras);
        return ResponseEntity.status(status).body(body);
    }
}