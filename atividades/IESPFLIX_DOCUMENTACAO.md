# IESPFLIX — Documentação Técnica Completa

> **Disciplina:** Tecnologias para Backend (Spring Boot) · Prof. Rodrigo Fujioka  
> **Curso:** Sistemas para Internet · UNIESP · 2025/P3  
> **Stack:** Java 21 · Spring Boot 3.5.10 · Lombok · Spring Data JPA · H2 · Swagger  
> **Data:** 2026-05-25

---

## EQUIPE

| Integrante | Responsabilidade |
|------------|-----------------|
| José Wilson Alves de Souza | Líder técnico · Usuários · Infraestrutura · MetodoPagamento |
| Ana Julya Rodrigues Dionizio | Conteúdos · Filtros e buscas JPQL |
| Alex Júlio de Brito | Filmes · Ordenação e queries |
| Everton Fernandes S. Da Silva | Assinaturas · Planos |
| Silvano Bernardino da S. Filho | Favoritos · Integração ViaCEP · BrasilAPI |

---

## 1. CHECKLIST DO PROJETO — STATUS FINAL

| Item Obrigatório | Status | Evidência |
|-----------------|--------|-----------|
| Equipe com no máximo 5 integrantes | ✅ | 5 integrantes |
| Pelo menos 1 endpoint por integrante | ✅ | Ver seção 6 |
| Lombok no backend | ✅ | `@Data` `@Builder` `@RequiredArgsConstructor` `@Slf4j` em todas as camadas |
| ORM com Spring Data JPA + JPQL personalizado | ✅ | 9 repositories com `@Query` customizadas |
| Integração com serviço externo | ✅ | ViaCEP (Feign) + BrasilAPI (RestTemplate) |
| Validações + Custom Bean Validator | ✅ | `@CpfCnpj` com algoritmo de dígitos verificadores |
| Logs | ✅ | `@Slf4j` + `CorrelationIdFilter` com UUID no MDC |
| Paginação | ✅ | `Page<T>` + `@PageableDefault` em Usuários e Conteúdos |
| Tratamento global de exceções centralizado | ✅ | `GlobalExceptionHandler` `@RestControllerAdvice` |

**Resultado: 9/9 itens atendidos — projeto completo.**

---

## 2. ARQUITETURA DO PROJETO

```
src/main/java/br/uniesp/si/techback/
├── config/
│   ├── AppConfig.java          ← @Bean: BCryptPasswordEncoder + RestTemplate
│   └── OpenApiConfig.java      ← @Bean: configuração Swagger/OpenAPI
├── controller/                 ← Camada HTTP: recebe DTOs, devolve ResponseEntity
├── service/                    ← Camada de negócio: @Transactional, logs, regras
├── repository/                 ← Spring Data JPA + @Query JPQL personalizadas
├── model/                      ← Entidades JPA com Lombok
├── dto/                        ← Request/Response DTOs com Bean Validation
├── mapper/                     ← Conversão Entity ↔ DTO (@Component manual)
├── validation/                 ← @CpfCnpj + CpfCnpjValidator (custom validator)
├── exception/                  ← GlobalExceptionHandler + CustomBeanException
├── filter/                     ← CorrelationIdFilter (MDC, UUID por requisição)
└── client/                     ← ViaCepClient (FeignClient)
    service/externo/            ← BrasilApiService (RestTemplate)
```

### Fluxo de uma Requisição HTTP (ponta a ponta)

```
Cliente HTTP (Swagger / Postman / React)
        │
        ▼
┌──────────────────────────┐
│   CorrelationIdFilter    │  Gera UUID único → MDC.put("correlationId")
│   (todo log da req tem   │  Limpa o MDC ao final (finally)
│    o mesmo ID rastreável)│
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│      Controller          │  @RestController · @Valid · @RequestBody
│  ex: UsuarioController   │  Converte JSON → DTO · chama Service
│                          │  Devolve ResponseEntity<DTO> (200/201/204)
└────────────┬─────────────┘
             │  @RequiredArgsConstructor (Lombok injeta via construtor)
             ▼
┌──────────────────────────┐
│        Service           │  @Service · @Slf4j · @Transactional
│  ex: UsuarioService      │  Regras de negócio · BCrypt · JPQL
│                          │  Lança EntityNotFoundException / DataIntegrityViolation
└──────┬─────────┬─────────┘
       │         │  @RequiredArgsConstructor (Lombok)
       ▼         ▼
┌──────────┐  ┌──────────┐
│Repository│  │  Mapper  │  @Component · converte Entity ↔ DTO manualmente
│(JPA/JPQL)│  │          │
└──────────┘  └──────────┘
       │
       ▼
  H2 Database
  (arquivo ~/teckback20262)

        ↓ qualquer Exception
┌──────────────────────────┐
│  GlobalExceptionHandler  │  @RestControllerAdvice
│                          │  Retorna JSON padronizado com:
│                          │  timestamp · status · error · message · path
└──────────────────────────┘
```

### Como funciona a Injeção de Dependências

O projeto usa **injeção por construtor** com Lombok — não há `@Autowired` explícito:

```java
// Lombok gera o construtor automaticamente:
@Service
@RequiredArgsConstructor   // ← gera: public UsuarioService(UsuarioRepository r, UsuarioMapper m, BCryptPasswordEncoder e)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;   // Spring injeta
    private final UsuarioMapper usuarioMapper;            // Spring injeta (@Component)
    private final BCryptPasswordEncoder passwordEncoder;  // Spring injeta (@Bean de AppConfig)
}
```

**AppConfig** registra os @Bean globais usados por vários services:
```java
@Configuration
public class AppConfig {
    @Bean public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public RestTemplate restTemplate() { return new RestTemplate(); }
}
```

---

## 3. ENTIDADES JPA

### 3.1 Usuario
```
id · nome · email (unique) · senhaHash · cpfCnpj · dataNascimento · perfil · criadoEm
```
- `@CreationTimestamp` preenche `criadoEm` automaticamente
- `email` tem `@Column(unique = true)` — duplicata gera HTTP 409

### 3.2 Conteudo
```
id · titulo · sinopse · genero · anoLancamento · relevancia
```

### 3.3 Filme
```
id · titulo · diretor · duracao · genero · anoLancamento · nota
```

### 3.4 Favorito
```
id · usuario (ManyToOne) · conteudo (ManyToOne) · adicionadoEm
```

### 3.5 Plano
```
id · nome · preco · descricao
```

### 3.6 Assinatura
```
id · usuario (ManyToOne) · plano (ManyToOne) · dataInicio · dataFim · ativa/status
```

### 3.7 MetodoPagamento
```
id · usuario (ManyToOne) · tipo · tokenizado · principal
```

### 3.8 Funcionario
```
id · nome · email · cargo · cep · [campos de endereço via ViaCEP]
```

---

## 4. CUSTOM BEAN VALIDATOR — @CpfCnpj

Valida CPF (11 dígitos) ou CNPJ (14 dígitos). Para CPF, verifica os **dígitos verificadores**:

```
Algoritmo:
1. Remove não-dígitos. Verifica comprimento (11 = CPF, 14 = CNPJ).
2. Rejeita sequências iguais (111.111.111-11, etc.)
3. Soma ponderada 9 primeiros dígitos (pesos 10→2) → calcula 1º verificador
4. Soma ponderada 10 primeiros dígitos (pesos 11→2) → calcula 2º verificador
5. Compara com os dois últimos dígitos do CPF informado
```

**Uso:** `@CpfCnpj private String cpfCnpj;` no DTO — validado automaticamente pelo `@Valid`.

---

## 5. TRATAMENTO GLOBAL DE EXCEÇÕES

`GlobalExceptionHandler` com `@RestControllerAdvice` centraliza todos os erros:

| Exceção | HTTP | Quando ocorre |
|---------|------|---------------|
| `MethodArgumentNotValidException` | **400** | `@Valid` falha (campo obrigatório vazio, email inválido, etc.) |
| `CustomBeanException` | **400** | CEP inválido, regra de negócio específica |
| `EntityNotFoundException` | **404** | `repository.findById()` não encontra o registro |
| `DataIntegrityViolationException` | **409** | Email duplicado, constraint de banco violada |
| `Exception` (genérico) | **500** | Qualquer erro não previsto |

**Formato padrão de resposta de erro:**
```json
{
  "timestamp": "2026-05-25T18:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuário não encontrado com ID: 99",
  "path": "/usuarios/99"
}
```

---

## 6. ENDPOINTS POR INTEGRANTE

### José Wilson — Usuários + MetodoPagamento + Infraestrutura

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/usuarios` | Cria usuário · BCrypt · header Location · 201 |
| GET | `/usuarios` | Listagem paginada (`?page=0&size=10`) |
| GET | `/usuarios/{id}` | Busca por ID · 404 se ausente |
| PUT | `/usuarios/{id}` | Atualiza dados e senha |
| DELETE | `/usuarios/{id}` | Remove · 204 No Content |
| POST | `/funcionarios` | Cria funcionário com `@Valid` |
| GET | `/funcionarios` | Lista funcionários |
| POST | `/metodos-pagamento` | Cadastra método de pagamento · 201 |
| GET | `/metodos-pagamento/usuario/{id}` | Lista métodos do usuário (JPQL ordenado) |
| DELETE | `/metodos-pagamento/{id}` | Remove método · 204 |

### Ana Julya — Conteúdos e Filtros JPQL

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/conteudos` | Cria conteúdo com `@Valid` |
| GET | `/conteudos` | Listagem paginada |
| GET | `/conteudos/{id}` | Busca por ID |
| GET | `/conteudos/genero/{genero}` | Filtro JPQL case-insensitive por gênero |
| GET | `/conteudos/top?n=5` | Top N por relevância (JPQL ORDER BY) |
| GET | `/conteudos/buscar?termo=xyz` | Busca em título e sinopse (JPQL LIKE) |
| GET | `/conteudos/lancados-apos?ano=2020` | Filtro por ano de lançamento |
| PUT | `/conteudos/{id}` | Atualiza conteúdo |
| DELETE | `/conteudos/{id}` | Remove · 204 |

### Alex Júlio — Filmes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/filmes` | Lista todos os filmes |
| GET | `/filmes/ordenado` | Lista ordenado por título (JPQL) |
| GET | `/filmes/{id}` | Busca por ID |
| POST | `/filmes` | Cria filme |
| PUT | `/filmes/{id}` | Atualiza filme |
| DELETE | `/filmes/{id}` | Remove filme |

### Everton — Assinaturas e Planos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/planos` | Cria plano (Basic, Standard, Premium) |
| GET | `/planos` | Lista todos os planos |
| POST | `/assinaturas` | Cria assinatura vinculando usuário a plano |
| GET | `/assinaturas/usuario/{id}` | Lista assinaturas do usuário |
| PUT | `/assinaturas/{id}/cancelar` | Cancela assinatura |

### Silvano — Favoritos + Integrações Externas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/favoritos` | Adiciona conteúdo aos favoritos |
| GET | `/favoritos/usuario/{id}` | Lista favoritos recentes (JPQL ORDER BY data) |
| DELETE | `/favoritos/{id}` | Remove favorito · 204 |
| GET | `/enderecos/{cep}` | Consulta ViaCEP (FeignClient) — valida 8 dígitos |
| GET | `/feriados/{ano}` | Consulta BrasilAPI (RestTemplate) |

---

## 7. LOGS E RASTREABILIDADE

Cada requisição recebe um **UUID único** via `CorrelationIdFilter`:

```java
MDC.put("correlationId", UUID.randomUUID().toString());
// → aparece em TODOS os logs da mesma requisição
// → limpo no finally (MDC.clear())
```

**Exemplo de log rastreado:**
```
18:30:15 [a3f2-9c1b-48d0-...] INFO  UsuarioService - Criando usuário: joao@email.com
18:30:15 [a3f2-9c1b-48d0-...] INFO  UsuarioService - Usuário criado com ID: 1
18:30:15 [a3f2-9c1b-48d0-...] INFO  FilmeService   - Buscando todos os filmes
```

O mesmo `correlationId` amarra todos os logs de uma mesma chamada HTTP.

---

## 8. COMO USAR O SWAGGER (Passo a Passo)

### Passo 1 — Subir o projeto
```bash
./mvnw spring-boot:run
```
Aguarde: `Started TechbackApplication in X.XXX seconds`

### Passo 2 — Acessar o Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Passo 3 — Criar um Usuário
1. Expanda **`usuarios-controller`**
2. Clique em **`POST /usuarios`** → **"Try it out"**
3. Cole no body:
```json
{
  "nome": "José Wilson",
  "email": "jose@iespflix.com",
  "senha": "Senha@123",
  "cpfCnpj": "529.982.247-25",
  "dataNascimento": "1990-05-15",
  "perfil": "ADMIN"
}
```
4. Clique **Execute** → resposta `201 Created` com header `Location`

### Passo 4 — Listar com Paginação
1. Clique em **`GET /usuarios`** → **"Try it out"**
2. Preencha `page = 0`, `size = 10`
3. Execute → resposta paginada com `totalElements`, `totalPages`

### Passo 5 — Buscar CEP
1. Expanda **`endereco-controller`**
2. `GET /enderecos/{cep}` → `cep = 01310100` → Execute
3. Retorna logradouro, bairro, cidade, UF da Avenida Paulista

### Passo 6 — Consultar Feriados
1. Expanda **`feriado-controller`**
2. `GET /feriados/{ano}` → `ano = 2025` → Execute
3. Retorna lista completa de feriados nacionais

### Passo 7 — Erros intencionais (para demonstrar tratamento)
- `GET /usuarios/9999` → retorna `404 Not Found` com JSON padronizado
- `POST /usuarios` com email já cadastrado → `409 Conflict`
- `POST /usuarios` com body vazio → `400 Bad Request` com lista de erros de validação

---

## 9. COMO USAR O CONSOLE H2 (Passo a Passo)

### Passo 1 — Acessar o console
```
http://localhost:8080/h2
```

### Passo 2 — Configuração de conexão

| Campo | Valor |
|-------|-------|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:~/teckback20262` |
| User Name | `sa` |
| Password | *(deixar em branco)* |

Clique em **Connect**.

### Passo 3 — Consultas úteis

**Ver todas as tabelas:**
```sql
SHOW TABLES;
```

**Usuários cadastrados:**
```sql
SELECT id, nome, email, perfil, criado_em FROM usuarios;
```

**Assinaturas com JOIN (usuário + plano):**
```sql
SELECT a.id, u.nome AS usuario, p.nome AS plano, a.ativa
FROM assinaturas a
JOIN usuarios u ON a.usuario_id = u.id
JOIN planos p ON a.plano_id = p.id;
```

**Favoritos com JOIN:**
```sql
SELECT f.id, u.nome AS usuario, c.titulo AS conteudo, f.adicionado_em
FROM favoritos f
JOIN usuarios u ON f.usuario_id = u.id
JOIN conteudos c ON f.conteudo_id = c.id
ORDER BY f.adicionado_em DESC;
```

**Métodos de pagamento por usuário:**
```sql
SELECT m.id, u.nome AS usuario, m.tipo, m.principal
FROM metodo_pagamento m
JOIN usuarios u ON m.usuario_id = u.id
ORDER BY m.principal DESC;
```

**Filmes ordenados por nota:**
```sql
SELECT titulo, diretor, ano_lancamento, nota
FROM filmes
ORDER BY nota DESC;
```

---

## 10. ROTEIRO DE INSERÇÕES (2 por módulo)

> Execute na ordem abaixo — alguns módulos dependem de dados anteriores.

### 10.1 Planos (POST `/planos`)

**Inserção 1:**
```json
{
  "nome": "Basic",
  "preco": 19.90,
  "descricao": "1 tela simultânea, qualidade SD"
}
```

**Inserção 2:**
```json
{
  "nome": "Premium",
  "preco": 45.90,
  "descricao": "4 telas simultâneas, qualidade 4K + downloads"
}
```

---

### 10.2 Usuários (POST `/usuarios`)

**Inserção 1:**
```json
{
  "nome": "José Wilson",
  "email": "jose@iespflix.com",
  "senha": "Senha@123",
  "cpfCnpj": "529.982.247-25",
  "dataNascimento": "1990-05-15",
  "perfil": "ADMIN"
}
```

**Inserção 2:**
```json
{
  "nome": "Ana Julya",
  "email": "ana@iespflix.com",
  "senha": "Senha@456",
  "cpfCnpj": "048.445.600-80",
  "dataNascimento": "1998-03-22",
  "perfil": "USUARIO"
}
```

---

### 10.3 Assinaturas (POST `/assinaturas`) — requer usuários e planos criados

**Inserção 1** (usuário 1 → plano 1):
```json
{
  "usuarioId": 1,
  "planoId": 1,
  "dataInicio": "2026-01-01",
  "dataFim": "2026-12-31"
}
```

**Inserção 2** (usuário 2 → plano 2):
```json
{
  "usuarioId": 2,
  "planoId": 2,
  "dataInicio": "2026-05-01",
  "dataFim": "2027-05-01"
}
```

---

### 10.4 Conteúdos (POST `/conteudos`)

**Inserção 1:**
```json
{
  "titulo": "Interestelar",
  "sinopse": "Exploradores viajam através de um buraco de minhoca em busca de um novo lar para a humanidade",
  "genero": "FICCAO_CIENTIFICA",
  "anoLancamento": 2014,
  "relevancia": 9
}
```

**Inserção 2:**
```json
{
  "titulo": "Cidade de Deus",
  "sinopse": "Dois meninos crescem em uma favela do Rio de Janeiro nos anos 1970 seguindo caminhos opostos",
  "genero": "DRAMA",
  "anoLancamento": 2002,
  "relevancia": 10
}
```

---

### 10.5 Filmes (POST `/filmes`)

**Inserção 1:**
```json
{
  "titulo": "O Poderoso Chefão",
  "diretor": "Francis Ford Coppola",
  "duracao": 175,
  "genero": "DRAMA",
  "anoLancamento": 1972,
  "nota": 9.2
}
```

**Inserção 2:**
```json
{
  "titulo": "Matrix",
  "diretor": "Lana Wachowski",
  "duracao": 136,
  "genero": "FICCAO_CIENTIFICA",
  "anoLancamento": 1999,
  "nota": 8.7
}
```

---

### 10.6 Favoritos (POST `/favoritos`) — requer usuários e conteúdos criados

**Inserção 1** (usuário 1 → conteúdo 1):
```json
{
  "usuarioId": 1,
  "conteudoId": 1
}
```

**Inserção 2** (usuário 2 → conteúdo 2):
```json
{
  "usuarioId": 2,
  "conteudoId": 2
}
```

---

### 10.7 Métodos de Pagamento (POST `/metodos-pagamento`)

**Inserção 1:**
```json
{
  "usuarioId": 1,
  "tipo": "CARTAO_CREDITO",
  "tokenizado": "tok_visa_4242",
  "principal": true
}
```

**Inserção 2:**
```json
{
  "usuarioId": 2,
  "tipo": "PIX",
  "tokenizado": "chave_pix_ana@iespflix.com",
  "principal": true
}
```

---

### 10.8 Funcionários (POST `/funcionarios`)

**Inserção 1:**
```json
{
  "nome": "Carlos Gerente",
  "email": "carlos@iespflix.com",
  "cargo": "Gerente de TI",
  "cep": "01310100"
}
```

**Inserção 2:**
```json
{
  "nome": "Maria Suporte",
  "email": "maria@iespflix.com",
  "cargo": "Analista de Suporte",
  "cep": "20040020"
}
```

---

### 10.9 Integrações Externas (GET — sem body)

**ViaCEP — 2 consultas:**
```
GET http://localhost:8080/enderecos/01310100   → Av. Paulista, Bela Vista, São Paulo/SP
GET http://localhost:8080/enderecos/20040020   → R. da Assembléia, Centro, Rio de Janeiro/RJ
```

**BrasilAPI — 2 consultas:**
```
GET http://localhost:8080/feriados/2025   → lista feriados nacionais de 2025
GET http://localhost:8080/feriados/2026   → lista feriados nacionais de 2026
```

---

## 11. DEMONSTRAÇÃO DE QUERIES JPQL PERSONALIZADAS

Cada repository possui ao menos uma `@Query` JPQL — abaixo as principais:

**ConteudoRepository — filtro por gênero (case-insensitive):**
```sql
SELECT c FROM Conteudo c WHERE LOWER(c.genero) = LOWER(:genero)
```

**ConteudoRepository — busca por termo em título e sinopse:**
```sql
SELECT c FROM Conteudo c WHERE LOWER(c.titulo) LIKE LOWER(CONCAT('%',:termo,'%'))
   OR LOWER(c.sinopse) LIKE LOWER(CONCAT('%',:termo,'%'))
```

**ConteudoRepository — top N por relevância:**
```sql
SELECT c FROM Conteudo c ORDER BY c.relevancia DESC
```
*(limitado via `Pageable.ofSize(n)`)*

**FilmeRepository — ordenado por título:**
```sql
SELECT f FROM Filme f ORDER BY f.titulo ASC
```

**FavoritoRepository — recentes do usuário:**
```sql
SELECT f FROM Favorito f WHERE f.usuario.id = :usuarioId ORDER BY f.adicionadoEm DESC
```

**MetodoPagamentoRepository — por usuário, principal primeiro:**
```sql
SELECT m FROM MetodoPagamento m WHERE m.usuario.id = :usuarioId ORDER BY m.principal DESC
```

---

## 12. COMO GERAR O PDF

**Opção A — Via browser (mais simples):**
1. Abra o arquivo `IESPFLIX_DOCUMENTACAO.md` no VS Code
2. Instale a extensão **"Markdown PDF"**
3. Botão direito no arquivo → **"Markdown PDF: Export (pdf)"**
4. O PDF é gerado na mesma pasta

**Opção B — Via Pandoc:**
```bash
pandoc IESPFLIX_DOCUMENTACAO.md -o IESPFLIX_Documentacao.pdf \
  --pdf-engine=wkhtmltopdf \
  -V geometry:margin=2cm \
  -V fontsize=11pt
```

**Opção C — Via browser diretamente:**
1. Abra o Markdown em `https://dillinger.io` (cole o conteúdo)
2. Menu **Export As** → **PDF**

---

*Documento gerado em 2026-05-25 · IESPFLIX · UNIESP Sistemas para Internet*
