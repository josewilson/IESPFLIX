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

```
br.uniesp.si.techback/
│
├── TechbackApplication.java          ← @SpringBootApplication · ponto de entrada
│
├── client/
│   └── ViaCepClient.java             ← @FeignClient · consulta endereço por CEP
│
├── config/
│   ├── AppConfig.java                ← @Bean BCryptPasswordEncoder + RestTemplate
│   └── OpenApiConfig.java            ← configuração título/versão do Swagger
│
├── controller/
│   ├── AssinaturaController.java
│   ├── ConteudoController.java
│   ├── EnderecoController.java       ← usa ViaCepClient via EnderecoService
│   ├── FavoritoController.java
│   ├── FeriadoController.java        ← usa BrasilApiService
│   ├── FilmeController.java
│   ├── FuncionarioController.java
│   ├── HistoricoController.java
│   ├── MetodoPagamentoController.java
│   ├── PlanoController.java
│   └── UsuarioController.java
│
├── dto/
│   ├── AssinaturaRequestDTO.java / AssinaturaResponseDTO.java
│   ├── ConteudoRequestDTO.java / ConteudoResponseDTO.java
│   ├── FavoritoRequestDTO.java / FavoritoResponseDTO.java
│   ├── FilmeDTO.java
│   ├── HistoricoRequestDTO.java / HistoricoResponseDTO.java
│   ├── MetodoPagamentoRequestDTO.java / MetodoPagamentoResponseDTO.java
│   ├── PlanoRequestDTO.java / PlanoResponseDTO.java
│   ├── UsuarioRequestDTO.java / UsuarioResponseDTO.java
│   ├── ViaCepResponseDTO.java
│   └── externo/
│       └── FeriadoResponse.java      ← DTO de resposta da BrasilAPI
│
├── exception/
│   ├── GlobalExceptionHandler.java   ← @RestControllerAdvice · 6 handlers
│   └── CustomBeanException.java      ← exceção de negócio (400)
│
├── filter/
│   └── CorrelationIdFilter.java      ← OncePerRequestFilter · UUID no MDC
│
├── mapper/
│   ├── ConteudoMapper.java
│   ├── FavoritoMapper.java
│   ├── FilmeMapper.java
│   ├── HistoricoMapper.java
│   └── UsuarioMapper.java
│
├── model/
│   ├── Assinatura.java               ← FK: usuario_id, plano_id
│   ├── Conteudo.java
│   ├── Favorito.java                 ← FK: usuario_id, conteudo_id
│   ├── Filme.java
│   ├── Funcionario.java
│   ├── Historico.java                ← FK: usuario_id, conteudo_id
│   ├── MetodoPagamento.java          ← FK: usuario_id
│   ├── Plano.java
│   └── Usuario.java
│
├── repository/
│   ├── AssinaturaRepository.java     ← derived query: findByUsuarioId
│   ├── ConteudoRepository.java       ← @Query JPQL: gênero, top, busca, ano
│   ├── FavoritoRepository.java       ← @Query JPQL: recentes por usuário
│   ├── FilmeRepository.java          ← @Query JPQL: ordenado por título
│   ├── FuncionarioRepository.java
│   ├── HistoricoRepository.java      ← @Query JPQL: por usuário / concluídos
│   ├── MetodoPagamentoRepository.java← @Query JPQL: por usuário ordenado
│   ├── PlanoRepository.java
│   └── UsuarioRepository.java        ← @Query JPQL: busca por email
│
├── service/
│   ├── AssinaturaService.java
│   ├── ConteudoService.java
│   ├── FavoritoService.java
│   ├── FilmeService.java
│   ├── FuncionarioService.java
│   ├── HistoricoService.java
│   ├── MetodoPagamentoService.java
│   ├── PlanoService.java
│   ├── UsuarioService.java           ← usa BCryptPasswordEncoder
│   └── externo/
│       └── BrasilApiService.java     ← RestTemplate · consulta feriados
│
└── validation/
    ├── CpfCnpj.java                  ← anotação @CpfCnpj (Bean Validation)
    └── CpfCnpjValidator.java         ← algoritmo de dígitos verificadores
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
