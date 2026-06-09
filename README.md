<div align="center">

# 🎬 IESPFLIX

### Backend de Plataforma de Streaming

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.10-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![H2 Database](https://img.shields.io/badge/H2-Database-0000BB?style=for-the-badge&logo=h2&logoColor=white)](https://www.h2database.com/)
[![Lombok](https://img.shields.io/badge/Lombok-1.18.32-BC4521?style=for-the-badge&logo=lombok&logoColor=white)](https://projectlombok.org/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Academic-blue?style=for-the-badge)](LICENSE)

<br/>

> API REST para gerenciamento de uma plataforma de streaming, desenvolvida com Spring Boot e arquitetura em camadas.
> Projeto acadêmico da disciplina **Tecnologias para Backend (Spring Boot)** · UNIESP · Sistemas para Internet 2025/P3

<br/>

[📖 Documentação](#-documentação-da-api) · [🚀 Como Executar](#-como-executar) · [🗂️ Endpoints](#️-endpoints) · [👥 Equipe](#-equipe)

</div>

---

## 📋 Sobre o Projeto

O **IESPFLIX** é um backend completo para uma plataforma de streaming fictícia. Ele gerencia usuários, conteúdos, filmes, assinaturas, planos, favoritos, histórico de reprodução, métodos de pagamento e funcionários, seguindo boas práticas de desenvolvimento Java com arquitetura em camadas, validações customizadas, logs rastreáveis e integração com APIs externas.

### ✨ Destaques

- 🔐 **Segurança — senhas com BCrypt**
  - Classe: `AppConfig` declara o bean `BCryptPasswordEncoder`
  - Classe: `UsuarioService` usa o encoder ao criar e atualizar usuários
  - Hash unidirecional com salt — a senha nunca é armazenada em texto puro

- ✅ **Validação customizada — `@CpfCnpj`**
  - Anotação: `validation/CpfCnpj.java`
  - Lógica: `validation/CpfCnpjValidator.java` — algoritmo completo de dígitos verificadores
  - Aplicada em `UsuarioRequestDTO.cpfCnpj`

- 📄 **Paginação com metadados**
  - `UsuarioController` e `ConteudoController` recebem `Pageable` e retornam `Page<T>`
  - `UsuarioService.listar(Pageable)` e `ConteudoService.listar(Pageable)`

- 🔍 **Rastreabilidade por requisição**
  - Filtro: `filter/CorrelationIdFilter.java` — gera UUID por requisição e insere no MDC
  - Todos os logs de `@Slf4j` nos services carregam o `correlationId` automaticamente

- 🌐 **Integrações externas**
  - ViaCEP via Feign: `client/ViaCepClient.java` + `EnderecoController` + `ViaCepResponseDTO`
  - BrasilAPI via RestTemplate: `service/externo/BrasilApiService.java` + `FeriadoController` + `dto/externo/FeriadoResponse.java`
  - `AppConfig` declara o bean `RestTemplate` usado por `BrasilApiService`

- ⚠️ **Tratamento centralizado de erros**
  - Classe: `exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`)
  - Cobre 6 cenários: validação (400) · negócio customizado (400) · JSON malformado (400) · entidade não encontrada (404) · rota inexistente (404) · conflito de integridade (409) · erro interno (500)
  - Exceção customizada: `exception/CustomBeanException.java`

- 📚 **Documentação automática — Swagger**
  - Configuração: `config/OpenApiConfig.java`
  - Gerado pelo SpringDoc 2.8.8 · acesso em `/swagger-ui.html`

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.10 | Framework base |
| Spring Data JPA | — | ORM e acesso ao banco |
| Hibernate | — | Implementação JPA |
| H2 Database | — | Banco em arquivo (desenvolvimento) |
| Lombok | 1.18.32 | Redução de boilerplate |
| SpringDoc OpenAPI | 2.8.8 | Documentação Swagger |
| OpenFeign | — | Client HTTP declarativo (ViaCEP) |
| Spring Security Crypto | — | BCrypt para hash de senhas |
| JaCoCo | 0.8.11 | Cobertura de testes |

---

## 📁 Estrutura de Pacotes

> Legenda: **consome →** classes/beans que a classe injeta ou usa · **valida com →** anotações Bean Validation aplicadas nos campos

```
br.uniesp.si.techback/
│
├── TechbackApplication.java
│     Ponto de entrada da aplicação. @SpringBootApplication habilita
│     auto-configuração, component scan e @EnableFeignClients.
│
│
├── client/
│   └── ViaCepClient.java
│         Interface @FeignClient que declara a chamada HTTP GET para a
│         API ViaCEP. Spring gera a implementação em runtime.
│         Retorna: ViaCepResponseDTO
│         Consumida por: FuncionarioService, EnderecoController
│
│
├── config/
│   ├── AppConfig.java
│   │     @Configuration · declara dois @Bean disponíveis em todo o contexto:
│   │     • BCryptPasswordEncoder  → injetado em UsuarioService
│   │     • RestTemplate           → injetado em BrasilApiService
│   │
│   └── OpenApiConfig.java
│         @Configuration · declara @Bean OpenAPI com título, descrição e
│         versão do Swagger. Consumido pelo SpringDoc para montar /swagger-ui.html.
│
│
├── controller/                    (todos anotados com @RestController + @RequiredArgsConstructor)
│   │
│   ├── UsuarioController.java
│   │     @RequestMapping("/usuarios") · CRUD completo + paginação.
│   │     Usa @Valid nos métodos POST e PUT → dispara validação do UsuarioRequestDTO.
│   │     consome → UsuarioService
│   │     retorna → UsuarioResponseDTO, Page<UsuarioResponseDTO>
│   │
│   ├── ConteudoController.java
│   │     @RequestMapping("/conteudos") · CRUD + 4 filtros avançados.
│   │     Usa @Valid nos métodos POST e PUT → dispara validação do ConteudoRequestDTO.
│   │     consome → ConteudoService
│   │     retorna → ConteudoResponseDTO, Page<ConteudoResponseDTO>
│   │
│   ├── FilmeController.java
│   │     @RequestMapping("/filmes") · CRUD + listagem ordenada.
│   │     Usa @Valid nos métodos POST e PUT → dispara validação do FilmeDTO.
│   │     Possui @Slf4j próprio (logs de cada operação no controller).
│   │     consome → FilmeService
│   │     retorna → FilmeDTO
│   │
│   ├── FavoritoController.java
│   │     @RequestMapping("/favoritos") · adicionar / listar / remover.
│   │     Não usa @Valid (FavoritoRequestDTO não tem constraints Bean Validation).
│   │     consome → FavoritoService
│   │     retorna → FavoritoResponseDTO
│   │
│   ├── HistoricoController.java
│   │     @RequestMapping("/historicos") · registrar / listar / concluídos / remover.
│   │     Usa @Valid no POST → dispara validação do HistoricoRequestDTO.
│   │     consome → HistoricoService
│   │     retorna → HistoricoResponseDTO
│   │
│   ├── AssinaturaController.java
│   │     @RequestMapping("/assinaturas") · criar / listar por usuário / cancelar.
│   │     Não usa @Valid (AssinaturaRequestDTO não tem constraints Bean Validation).
│   │     consome → AssinaturaService
│   │     retorna → AssinaturaResponseDTO
│   │
│   ├── PlanoController.java
│   │     @RequestMapping("/planos") · criar / listar.
│   │     Não usa @Valid (PlanoRequestDTO não tem constraints Bean Validation).
│   │     consome → PlanoService
│   │     retorna → PlanoResponseDTO
│   │
│   ├── MetodoPagamentoController.java
│   │     @RequestMapping("/metodos-pagamento") · criar / listar / remover.
│   │     Usa @Valid no POST → dispara validação do MetodoPagamentoRequestDTO.
│   │     consome → MetodoPagamentoService
│   │     retorna → MetodoPagamentoResponseDTO
│   │
│   ├── FuncionarioController.java
│   │     @RequestMapping("/funcionarios") · listar / incluir.
│   │     Usa @Valid no POST → dispara validação direta na entidade Funcionario.
│   │     consome → FuncionarioService
│   │     retorna → Funcionario (entidade diretamente, sem DTO)
│   │
│   ├── EnderecoController.java
│   │     @RequestMapping("/enderecos") · busca CEP.
│   │     Valida tamanho do CEP manualmente (8 dígitos); lança CustomBeanException
│   │     se inválido → capturado pelo GlobalExceptionHandler → HTTP 400.
│   │     consome → ViaCepClient (diretamente, sem service intermediário)
│   │     retorna → ViaCepResponseDTO
│   │
│   └── FeriadoController.java
│         @RequestMapping("/feriados") · lista feriados por ano.
│         consome → BrasilApiService
│         retorna → List<FeriadoResponse>
│
│
├── dto/
│   │  (todos com @Data @Builder @NoArgsConstructor @AllArgsConstructor via Lombok)
│   │
│   ├── UsuarioRequestDTO.java
│   │     valida com → @NotBlank (nome, email, senha)
│   │                   @Email (email)
│   │                   @Size(min=8) (senha)
│   │                   @CpfCnpj (cpfCnpj) ← validador customizado
│   │                   @Past (dataNascimento)
│   │     consumido por → UsuarioController (@Valid), UsuarioService
│   │
│   ├── UsuarioResponseDTO.java
│   │     Sem validações — apenas saída. Produzido por UsuarioMapper.
│   │
│   ├── ConteudoRequestDTO.java
│   │     valida com → @NotBlank (titulo)
│   │                   @Min(1888) @Max(2100) (ano)
│   │                   @Positive (duracaoMinutos)
│   │                   @DecimalMin("0.0") @DecimalMax("10.0") (relevancia)
│   │     consumido por → ConteudoController (@Valid), ConteudoService, ConteudoMapper
│   │
│   ├── ConteudoResponseDTO.java
│   │     Sem validações — apenas saída. Produzido por ConteudoMapper.
│   │
│   ├── FilmeDTO.java
│   │     valida com → @NotBlank (titulo)
│   │     consumido por → FilmeController (@Valid), FilmeService, FilmeMapper
│   │
│   ├── FavoritoRequestDTO.java
│   │     Sem validações Bean Validation. Campos: usuarioId, conteudoId.
│   │     consumido por → FavoritoController, FavoritoService
│   │
│   ├── FavoritoResponseDTO.java
│   │     Sem validações — apenas saída. Produzido por FavoritoMapper.
│   │
│   ├── HistoricoRequestDTO.java
│   │     valida com → @NotNull (usuarioId, conteudoId)
│   │                   @Min(0) (progressoSegundos)
│   │     consumido por → HistoricoController (@Valid), HistoricoService
│   │
│   ├── HistoricoResponseDTO.java
│   │     Sem validações — apenas saída. Produzido por HistoricoMapper.
│   │
│   ├── AssinaturaRequestDTO.java
│   │     Sem validações Bean Validation. Campos: usuarioId, planoId, datas.
│   │     consumido por → AssinaturaController, AssinaturaService
│   │
│   ├── AssinaturaResponseDTO.java
│   │     Sem validações — apenas saída. Produzido por AssinaturaService (inline).
│   │
│   ├── PlanoRequestDTO.java
│   │     Sem validações Bean Validation. Campos: nome, descricao, preco, limite.
│   │     consumido por → PlanoController, PlanoService
│   │
│   ├── PlanoResponseDTO.java
│   │     Sem validações — apenas saída. Produzido por PlanoService (inline).
│   │
│   ├── MetodoPagamentoRequestDTO.java
│   │     valida com → @NotNull (usuarioId, principal)
│   │                   @NotBlank (tipo, tokenizado)
│   │     consumido por → MetodoPagamentoController (@Valid), MetodoPagamentoService
│   │
│   ├── MetodoPagamentoResponseDTO.java
│   │     Sem validações — apenas saída. Produzido por MetodoPagamentoService (inline).
│   │
│   ├── ViaCepResponseDTO.java
│   │     Sem validações — DTO de entrada de API externa.
│   │     consumido por → ViaCepClient (desserialização), FuncionarioService, EnderecoController
│   │
│   └── externo/
│       └── FeriadoResponse.java
│             Sem validações — DTO de entrada da BrasilAPI.
│             consumido por → BrasilApiService (desserialização), FeriadoController
│
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   │     @RestControllerAdvice @Slf4j · intercepta exceções lançadas em qualquer
│   │     controller e retorna JSON padronizado com timestamp, status, message e path.
│   │     Trata 6 tipos:
│   │     • MethodArgumentNotValidException  → HTTP 400 (Bean Validation falhou)
│   │     • CustomBeanException              → HTTP 400 (regra de negócio)
│   │     • HttpMessageNotReadableException  → HTTP 400 (JSON malformado)
│   │     • EntityNotFoundException          → HTTP 404 (registro não encontrado)
│   │     • NoResourceFoundException         → HTTP 404 (rota não existe)
│   │     • DataIntegrityViolationException  → HTTP 409 (duplicidade no banco)
│   │     • Exception (fallback)             → HTTP 500
│   │
│   └── CustomBeanException.java
│         RuntimeException simples com mensagem customizável.
│         Lançada por: EnderecoController, FuncionarioService
│         Capturada por: GlobalExceptionHandler → HTTP 400
│
│
├── filter/
│   └── CorrelationIdFilter.java
│         Implementa Filter (@Component) · executado antes de todo controller.
│         Gera UUID único por requisição e insere no MDC com a chave "correlationId".
│         Todos os logs @Slf4j dos services incluem esse valor automaticamente.
│         Remove do MDC no bloco finally (evita vazamento entre threads).
│
│
├── mapper/                        (todos @Component · injetados nos services)
│   ├── UsuarioMapper.java
│   │     toEntity(UsuarioRequestDTO) → Usuario
│   │     toResponseDTO(Usuario)      → UsuarioResponseDTO
│   │     consumido por → UsuarioService
│   │
│   ├── ConteudoMapper.java
│   │     toEntity(ConteudoRequestDTO) → Conteudo
│   │     toResponseDTO(Conteudo)      → ConteudoResponseDTO
│   │     consumido por → ConteudoService
│   │
│   ├── FilmeMapper.java
│   │     toEntity(FilmeDTO) → Filme
│   │     toDTO(Filme)       → FilmeDTO
│   │     consumido por → FilmeService
│   │
│   ├── FavoritoMapper.java
│   │     toResponseDTO(Favorito) → FavoritoResponseDTO
│   │     consumido por → FavoritoService
│   │
│   └── HistoricoMapper.java
│         toResponseDTO(Historico) → HistoricoResponseDTO
│         consumido por → HistoricoService
│
│
├── model/                         (todos @Entity @Data @Builder @NoArgsConstructor @AllArgsConstructor)
│   ├── Usuario.java               tabela "usuarios" · campos com @NotBlank @Email @Past
│   ├── Conteudo.java              tabela "conteudo"
│   ├── Filme.java                 tabela "filmes" · @NotBlank(titulo) @Column(length=100)
│   ├── Favorito.java              tabela "favorito" · @ManyToOne usuario + conteudo · @CreationTimestamp
│   ├── Historico.java             tabela "historico" · @ManyToOne usuario + conteudo · @CreationTimestamp
│   ├── Assinatura.java            tabela "assinatura" · @ManyToOne usuario + plano
│   ├── MetodoPagamento.java       tabela "metodo_pagamento" · @ManyToOne usuario
│   ├── Plano.java                 tabela "plano"
│   └── Funcionario.java           tabela "funcionarios" · campos de endereço preenchidos via ViaCEP
│
│
├── repository/                    (todos extends JpaRepository<Entidade, Long> @Repository)
│   ├── UsuarioRepository.java
│   │     @Query JPQL: findByEmail(email) → Optional<Usuario>
│   │     consumido por → UsuarioService, AssinaturaService, FavoritoService,
│   │                      HistoricoService, MetodoPagamentoService
│   │
│   ├── ConteudoRepository.java
│   │     @Query JPQL (4): findByGeneroCaseInsensitive · findTopByRelevancia
│   │                       buscarPorTermo (LIKE) · findLancadosApos
│   │     consumido por → ConteudoService, FavoritoService, HistoricoService
│   │
│   ├── FilmeRepository.java
│   │     @Query JPQL (1): listarOrdenado (ORDER BY titulo)
│   │     consumido por → FilmeService
│   │
│   ├── FavoritoRepository.java
│   │     @Query JPQL (1): findFavoritosRecentesPorUsuario (ORDER BY adicionadoEm DESC)
│   │     consumido por → FavoritoService
│   │
│   ├── HistoricoRepository.java
│   │     @Query JPQL (2): findByUsuarioIdOrderByAssistidoEmDesc
│   │                       findConcluidosPorUsuario (WHERE concluido = true)
│   │     consumido por → HistoricoService
│   │
│   ├── AssinaturaRepository.java
│   │     derived query: findByUsuarioId(Long) → List<Assinatura>
│   │     consumido por → AssinaturaService
│   │
│   ├── MetodoPagamentoRepository.java
│   │     @Query JPQL (1): findByUsuarioId ordenado por principal DESC
│   │     consumido por → MetodoPagamentoService
│   │
│   ├── PlanoRepository.java
│   │     Apenas métodos herdados do JpaRepository (findAll, save, findById).
│   │     consumido por → PlanoService, AssinaturaService
│   │
│   └── FuncionarioRepository.java
│         Apenas métodos herdados do JpaRepository.
│         consumido por → FuncionarioService
│
│
├── service/                       (todos @Service @RequiredArgsConstructor)
│   ├── UsuarioService.java
│   │     @Slf4j · @Transactional nos métodos de escrita.
│   │     consome → UsuarioRepository · UsuarioMapper · BCryptPasswordEncoder (de AppConfig)
│   │     lança → DataIntegrityViolationException (email duplicado) · EntityNotFoundException
│   │
│   ├── ConteudoService.java
│   │     @Slf4j · @Transactional nos métodos de escrita.
│   │     consome → ConteudoRepository · ConteudoMapper
│   │     lança → EntityNotFoundException
│   │
│   ├── FilmeService.java
│   │     @Slf4j · @Transactional nos métodos de escrita.
│   │     consome → FilmeRepository · FilmeMapper
│   │     lança → EntityNotFoundException
│   │
│   ├── FavoritoService.java
│   │     @Transactional nos métodos de escrita.
│   │     consome → FavoritoRepository · UsuarioRepository · ConteudoRepository · FavoritoMapper
│   │     lança → EntityNotFoundException
│   │
│   ├── HistoricoService.java
│   │     @Slf4j · @Transactional nos métodos de escrita.
│   │     consome → HistoricoRepository · UsuarioRepository · ConteudoRepository · HistoricoMapper
│   │     lança → EntityNotFoundException
│   │
│   ├── AssinaturaService.java
│   │     @Transactional nos métodos de escrita.
│   │     consome → AssinaturaRepository · UsuarioRepository · PlanoRepository
│   │     lança → EntityNotFoundException
│   │
│   ├── PlanoService.java
│   │     @Transactional nos métodos de escrita.
│   │     consome → PlanoRepository
│   │
│   ├── MetodoPagamentoService.java
│   │     @Slf4j · @Transactional nos métodos de escrita.
│   │     consome → MetodoPagamentoRepository · UsuarioRepository
│   │     lança → EntityNotFoundException
│   │
│   ├── FuncionarioService.java
│   │     consome → FuncionarioRepository · ViaCepClient
│   │     lança → CustomBeanException (CEP inválido retornado pela API)
│   │
│   └── externo/
│       └── BrasilApiService.java
│             @Slf4j.
│             consome → RestTemplate (bean de AppConfig)
│             chama → https://brasilapi.com.br/api/feriados/v1/{ano}
│             retorna → List<FeriadoResponse>
│
│
└── validation/
    ├── CpfCnpj.java
    │     Anotação customizada Bean Validation (@Constraint).
    │     Atributos obrigatórios: message, groups, payload.
    │     Liga-se ao validador: @Constraint(validatedBy = CpfCnpjValidator.class)
    │     Usada em: UsuarioRequestDTO.cpfCnpj
    │
    └── CpfCnpjValidator.java
          Implementa ConstraintValidator<CpfCnpj, String>.
          isValid(): aceita null (campo opcional), valida CPF de 11 dígitos
          pelo algoritmo de dois dígitos verificadores, aceita CNPJ de 14 dígitos.
          Acionado automaticamente pelo Bean Validation quando @Valid é processado
          no UsuarioController (POST e PUT).
```

---

## 🏛️ Arquitetura e Fluxo de Requisição

### Fluxo padrão (exemplo: `POST /historicos`)

```
Cliente (Swagger / Postman / Frontend)
    │
    ▼
CorrelationIdFilter
    • gera UUID único para a requisição
    • insere no MDC → aparece em TODOS os logs da cadeia
    │
    ▼
HistoricoController  (@RestController · @RequestMapping("/historicos"))
    • recebe o JSON no corpo da requisição
    • @Valid dispara a validação do HistoricoRequestDTO
      (@NotNull em usuarioId e conteudoId, @Min(0) em progressoSegundos)
    • chama HistoricoService.registrar(dto)
    │
    ▼
HistoricoService  (@Service · @RequiredArgsConstructor · @Slf4j)
    • log.info com correlationId (via MDC)
    • busca Usuario em UsuarioRepository.findById()
      → lança EntityNotFoundException se não encontrado
    • busca Conteudo em ConteudoRepository.findById()
      → lança EntityNotFoundException se não encontrado
    • monta Historico (entity) com Historico.builder()
    • salva via HistoricoRepository.save(historico)
    • converte resultado com HistoricoMapper.toResponseDTO()
    │
    ▼
HistoricoRepository  (JpaRepository<Historico, Long>)
    • Spring Data JPA gera o SQL INSERT automaticamente
    • tabela "historico" com FK usuario_id e conteudo_id
    │
    ▼
H2 Database  (arquivo ~/teckback20262)
    │
    ▼
HistoricoController
    • monta o header Location com o ID do recurso criado
    • retorna HTTP 201 Created + HistoricoResponseDTO (JSON)

Em caso de erro:
    EntityNotFoundException  →  GlobalExceptionHandler  →  HTTP 404 JSON
    MethodArgumentNotValidException  →  GlobalExceptionHandler  →  HTTP 400 JSON
    HttpMessageNotReadableException  →  GlobalExceptionHandler  →  HTTP 400 JSON
```

### Fluxo de integração externa (exemplo: `GET /funcionarios` com ViaCEP)

```
Cliente
    │
    ▼
CorrelationIdFilter  →  UUID no MDC
    │
    ▼
FuncionarioController  →  @Valid em FuncionarioRequestDTO
    │
    ▼
FuncionarioService
    • chama ViaCepClient.buscarEndereco(cep)  (interface @FeignClient)
    │
    ▼
ViaCepClient  (OpenFeign)
    • HTTP GET https://viacep.com.br/ws/{cep}/json/
    • desserializa resposta em ViaCepResponseDTO
    │
    ▼
FuncionarioService  →  persiste via FuncionarioRepository
    │
    ▼
H2 Database
```

### Fluxo de feriados (BrasilAPI via RestTemplate)

```
Cliente
    │
    ▼
FeriadoController
    │
    ▼
BrasilApiService  (@Service · @Slf4j)
    • usa RestTemplate (bean de AppConfig)
    • HTTP GET https://brasilapi.com.br/api/feriados/v1/{ano}
    • desserializa em List<FeriadoResponse>  (dto/externo/FeriadoResponse.java)
    │
    ▼
FeriadoController  →  retorna HTTP 200 + lista JSON
```

---

## 🗂️ Endpoints

### 👤 Usuários
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/usuarios` | Criar usuário · BCrypt · 201 + Location |
| `GET` | `/usuarios` | Listagem paginada (`?page=0&size=10`) |
| `GET` | `/usuarios/{id}` | Buscar por ID |
| `PUT` | `/usuarios/{id}` | Atualizar dados |
| `DELETE` | `/usuarios/{id}` | Remover · 204 |

### 🎬 Conteúdos
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/conteudos` | Criar conteúdo |
| `GET` | `/conteudos` | Listagem paginada |
| `GET` | `/conteudos/{id}` | Buscar por ID |
| `GET` | `/conteudos/genero/{genero}` | Filtro por gênero (JPQL case-insensitive) |
| `GET` | `/conteudos/top?n=5` | Top N por relevância (JPQL) |
| `GET` | `/conteudos/buscar?termo=` | Busca em título e sinopse (JPQL LIKE) |
| `GET` | `/conteudos/lancados-apos?ano=` | Filtro por ano (JPQL) |
| `PUT` | `/conteudos/{id}` | Atualizar |
| `DELETE` | `/conteudos/{id}` | Remover · 204 |

### 🎥 Filmes
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/filmes` | Listar todos |
| `GET` | `/filmes/ordenado` | Listar ordenado por título (JPQL) |
| `GET` | `/filmes/{id}` | Buscar por ID |
| `POST` | `/filmes` | Criar filme |
| `PUT` | `/filmes/{id}` | Atualizar |
| `DELETE` | `/filmes/{id}` | Remover · 204 |

### ⭐ Favoritos
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/favoritos` | Adicionar favorito |
| `GET` | `/favoritos/usuario/{id}` | Favoritos recentes (JPQL ORDER BY data) |
| `DELETE` | `/favoritos/{id}` | Remover · 204 |

### 📼 Histórico de Reprodução
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/historicos` | Registrar reprodução · 201 + Location |
| `GET` | `/historicos/usuario/{id}` | Histórico completo do usuário (JPQL ORDER BY data) |
| `GET` | `/historicos/usuario/{id}/concluidos` | Apenas conteúdos concluídos (JPQL) |
| `DELETE` | `/historicos/{id}` | Remover entrada · 204 |

### 📦 Planos
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/planos` | Criar plano |
| `GET` | `/planos` | Listar planos |

### 📝 Assinaturas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/assinaturas` | Criar assinatura |
| `GET` | `/assinaturas/usuario/{id}` | Assinaturas do usuário |
| `PUT` | `/assinaturas/{id}/cancelar` | Cancelar assinatura |

### 💳 Métodos de Pagamento
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/metodos-pagamento` | Cadastrar método · 201 |
| `GET` | `/metodos-pagamento/usuario/{id}` | Listar por usuário (JPQL ordenado) |
| `DELETE` | `/metodos-pagamento/{id}` | Remover · 204 |

### 👷 Funcionários
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/funcionarios` | Listar funcionários |
| `POST` | `/funcionarios` | Incluir · endereço preenchido via ViaCEP |

### 🌐 Integrações Externas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/enderecos/{cep}` | Buscar endereço por CEP (ViaCEP via Feign) |
| `GET` | `/feriados/{ano}` | Feriados nacionais do ano (BrasilAPI via RestTemplate) |

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.6+ (ou usar o `mvnw` incluso no projeto)

### Executando

```bash
# 1. Clonar o repositório
git clone https://github.com/josewilson/IESPFLIX.git
cd IESPFLIX

# 2. Compilar e executar
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`

---

## 📖 Documentação da API

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```
Acesse pelo browser para visualizar e testar todos os endpoints interativamente.

### Console H2 (banco de dados)
```
http://localhost:8080/h2
```

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:file:~/teckback20262` |
| User Name | `sa` |
| Password | *(deixar em branco)* |

---

## ✅ Checklist do Projeto

| Requisito | Status |
|-----------|--------|
| Equipe com até 5 integrantes | ✅ |
| Mínimo 1 endpoint por integrante | ✅ |
| Lombok no backend | ✅ |
| ORM com Spring Data JPA + JPQL personalizado | ✅ |
| Integração com serviço externo (ViaCEP / BrasilAPI) | ✅ |
| Validações + Custom Bean Validator (`@CpfCnpj`) | ✅ |
| Logs com `@Slf4j` e rastreabilidade por UUID | ✅ |
| Paginação com `Page<T>` e `Pageable` | ✅ |
| Tratamento global de exceções centralizado | ✅ |

---

## 📊 Status do projeto

Última atualização: 2026-06-09

| Módulo | Classes principais | Status |
|--------|-------------------|--------|
| Usuários | `UsuarioController` · `UsuarioService` · `UsuarioRepository` · `UsuarioMapper` | ✓ |
| Conteúdos | `ConteudoController` · `ConteudoService` · `ConteudoRepository` · `ConteudoMapper` | ✓ |
| Filmes | `FilmeController` · `FilmeService` · `FilmeRepository` · `FilmeMapper` | ✓ |
| Favoritos | `FavoritoController` · `FavoritoService` · `FavoritoRepository` · `FavoritoMapper` | ✓ |
| Histórico | `HistoricoController` · `HistoricoService` · `HistoricoRepository` · `HistoricoMapper` | ✓ |
| Planos e Assinaturas | `PlanoController` · `AssinaturaController` · services e repositories | ✓ |
| Métodos de Pagamento | `MetodoPagamentoController` · `MetodoPagamentoService` · `MetodoPagamentoRepository` | ✓ |
| Funcionários | `FuncionarioController` · `FuncionarioService` · `FuncionarioRepository` | ✓ |
| Integração ViaCEP | `ViaCepClient` · `EnderecoController` · `ViaCepResponseDTO` | ✓ |
| Integração BrasilAPI | `BrasilApiService` · `FeriadoController` · `FeriadoResponse` | ✓ |
| Swagger / OpenAPI | `OpenApiConfig` · SpringDoc 2.8.8 | ✓ |
| Logs e Rastreabilidade | `CorrelationIdFilter` · MDC · `@Slf4j` em todos os services | ✓ |
| Tratamento de Exceções | `GlobalExceptionHandler` · `CustomBeanException` | ✓ |
| Validador customizado | `CpfCnpj` · `CpfCnpjValidator` | ✓ |

---

## 👥 Equipe

| Integrante | Responsabilidade |
|------------|-----------------|
| **José Wilson Alves de Souza** | Líder técnico · Usuários · MetodoPagamento · Infraestrutura |
| **Ana Julya Rodrigues Dionizio** | Conteúdos · Filtros JPQL · Buscas avançadas |
| **Alex Júlio de Brito** | Filmes · Ordenação · Logs extensivos |
| **Everton Fernandes S. Da Silva** | Assinaturas · Planos · Fluxo de cancelamento |
| **Silvano Bernardino da S. Filho** | Favoritos · Integração ViaCEP · Integração BrasilAPI |

---

## 📄 Comandos Úteis

```bash
# Compilar
./mvnw compile

# Executar testes
./mvnw test

# Gerar relatório de cobertura (JaCoCo)
./mvnw test jacoco:report
# → Abrir: target/site/jacoco/index.html

# Empacotar
./mvnw package

# Limpar
./mvnw clean
```

---

<div align="center">

Desenvolvido como projeto acadêmico da disciplina **Tecnologias para Backend (Spring Boot)**

**Prof. Rodrigo Fujioka · UNIESP · Sistemas para Internet · 2026/P3**

</div>
