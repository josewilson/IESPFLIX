# Techback - Backend 1 Uniesp

Projeto Spring Boot desenvolvido como parte da disciplina de Backend 1 da Uniesp, demonstrando boas práticas de desenvolvimento Java com arquitetura em camadas e testes abrangentes.

## 🚀 Tecnologias Utilizadas

- **Java 21** - Última versão LTS do Java
- **Spring Boot 3.5.10** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em arquivo para desenvolvimento
- **SpringDoc OpenAPI 2.8.6** - Documentação de API
- **Lombok 1.18.32** - Redução de código boilerplate
- **OpenFeign** - Cliente HTTP declarativo (ViaCEP)
- **Spring Security Crypto** - BCrypt para hash de senhas
- **JUnit 5** - Framework de testes
- **Mockito** - Framework para mocks em testes
- **JaCoCo** - Análise de cobertura de testes

## 📁 Estrutura do Projeto

```
src/main/java/br/uniesp/si/techback/
├── config/              # Beans de configuração (BCrypt, RestTemplate, Swagger)
├── client/              # FeignClient para ViaCEP
├── controller/          # Camada REST (9 controllers)
├── service/             # Lógica de negócio (8 services)
├── service/externo/     # Integrações externas (BrasilAPI)
├── repository/          # Spring Data JPA (8 repositories)
├── model/               # Entidades JPA (8 entidades)
├── dto/                 # Data Transfer Objects (12 DTOs)
├── dto/externo/         # DTOs de APIs externas
├── mapper/              # Conversores Entity ↔ DTO (4 mappers)
├── exception/           # GlobalExceptionHandler + CustomBeanException
├── filter/              # CorrelationIdFilter (MDC)
├── validation/          # Custom Bean Validator (@CpfCnpj)
└── TechbackApplication.java
```

## ✅ Funcionalidades implementadas

### Usuários
- `GET /usuarios` — listagem paginada (Page, size=10 padrão)
- `POST /usuarios` — cadastro com validação Bean Validation + hash BCrypt
- `GET /usuarios/{id}` — busca por ID
- `PUT /usuarios/{id}` — atualização com validação
- `DELETE /usuarios/{id}` — remoção (204 No Content)

### Conteúdos
- `GET /conteudos` — listagem paginada (Page, size=10 padrão)
- `POST /conteudos` — cadastro com validação
- `GET /conteudos/{id}` — busca por ID
- `GET /conteudos/genero/{genero}` — filtro case-insensitive por gênero (JPQL)
- `GET /conteudos/top?n=` — top N por relevância (JPQL + Pageable)
- `GET /conteudos/buscar?termo=` — busca em título e sinopse (JPQL LIKE)
- `GET /conteudos/lancados-apos?ano=` — filtro por ano de lançamento (JPQL)
- `PUT /conteudos/{id}` — atualização com validação
- `DELETE /conteudos/{id}` — remoção (204 No Content)

### Filmes
- `GET /filmes` — listagem completa
- `GET /filmes/ordenado` — listagem ordenada por título (JPQL)
- `GET /filmes/{id}` — busca por ID
- `POST /filmes` — cadastro com validação
- `PUT /filmes/{id}` — atualização com validação
- `DELETE /filmes/{id}` — remoção (204 No Content)

### Favoritos
- `POST /favoritos` — adicionar conteúdo aos favoritos
- `GET /favoritos/usuario/{usuarioId}` — favoritos recentes do usuário (JPQL ORDER BY)
- `DELETE /favoritos/{id}` — remover dos favoritos (204 No Content)

### Planos
- `GET /planos` — listagem de planos
- `POST /planos` — criar plano

### Assinaturas
- `POST /assinaturas` — criar assinatura (valida usuário e plano)
- `GET /assinaturas/usuario/{id}` — assinaturas do usuário
- `PUT /assinaturas/{id}/cancelar` — cancelar assinatura

### Funcionários
- `GET /funcionarios` — listagem
- `POST /funcionarios` — incluir com preenchimento automático de endereço via ViaCEP

### Integrações Externas
- `GET /enderecos/{cep}` — busca endereço por CEP (ViaCEP via Feign)
- `GET /feriados/{ano}` — feriados nacionais do ano (BrasilAPI via RestTemplate)

## 📊 Status do projeto

Última atualização: 2026-05-19

| Módulo | Status |
|--------|--------|
| Usuários | ✓ |
| Conteúdos | ✓ |
| Filmes | ✓ |
| Favoritos | ✓ |
| Planos e Assinaturas | ✓ |
| Funcionários | ✓ |
| Integrações externas (ViaCEP + BrasilAPI) | ✓ |
| Swagger / OpenAPI | ✓ |
| Logs e Infraestrutura (MDC, Correlation-ID) | ✓ |
| Custom Validator (@CpfCnpj) | ✓ |
| Tratamento global de exceções | ✓ |
| Paginação | ✓ |
| MetodoPagamento (Service + Controller) | ◑ |

## 🧪 Testes

O projeto possui cobertura de testes abrangente:

- **FilmeRepository**: 100% de cobertura
- **FilmeService**: 80.9% de cobertura
- **FilmeController**: 83.2% de cobertura
- **FilmeMapper**: 100% de cobertura

### Executando os Testes

```bash
# Executar todos os testes
mvn test

# Executar testes com relatório de cobertura
mvn test jacoco:report

# Visualizar relatório de cobertura
# Abra: target/site/jacoco/index.html
```

## 🏗️ Arquitetura e Boas Práticas

### Separação de Responsabilidades
- **Controller**: Trata requisições HTTP, valida entrada com `@Valid`
- **Service**: Contém a lógica de negócio e regras do domínio
- **Repository**: Interface Spring Data JPA com queries JPQL customizadas
- **DTO**: Objetos de transferência com Bean Validation
- **Mapper**: Conversão entre entidades e DTOs

### Validação
- Bean Validation padrão: `@NotBlank`, `@Email`, `@Past`, `@Min`, `@Max`, `@DecimalMin`, `@DecimalMax`, `@Positive`, `@Size`
- **Custom Validator**: `@CpfCnpj` — valida CPF (algoritmo de dígitos verificadores) e CNPJ

### Tratamento de Exceções
- `@RestControllerAdvice` centralizado (`GlobalExceptionHandler`)
- Resposta padronizada com `timestamp`, `status`, `error`, `message`, `path`
- Cobre: 400 (validação), 400 (regra de negócio), 404, 409 (conflito), 500

### Logging
- `@Slf4j` com níveis INFO, DEBUG, WARN, ERROR
- **Correlation-ID** por requisição via `CorrelationIdFilter` + MDC
- Padrão de log: `HH:mm:ss [correlationId] LEVEL logger - mensagem`

## 🚀 Como Executar

### Pré-requisitos
- Java 21 ou superior
- Maven 3.6 ou superior

### Execução

```bash
# Clonar o projeto
git clone <repositório>

# Entrar no diretório
cd tecback

# Compilar e executar
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Documentação da API

A documentação OpenAPI/Swagger está disponível em:
`http://localhost:8080/swagger-ui.html`

### Console H2

O console do banco H2 está disponível em:
`http://localhost:8080/h2`

**Configurações de conexão:**
- URL: `jdbc:h2:file:~/teckback20262`
- User Name: `sa`
- Password: *(vazio)*

## 🔧 Desenvolvimento

### Comandos Úteis

```bash
# Compilar projeto
mvn compile

# Executar testes
mvn test

# Gerar relatório de cobertura
mvn jacoco:report

# Limpar projeto
mvn clean

# Empacotar aplicação
mvn package
```

## 👨‍💻 Equipe

| Nome |
|------|
| José Wilson Alves de Souza |
| Ana Julya Rodrigues Dionizio |
| Alex Júlio de Brito |
| Everton Fernandes S. Da Silva |
| Silvano Bernardino da S.Filho |

Desenvolvido como parte da disciplina **Tecnologias para Backend (SpringBoot)** — UNIESP · Sistemas para Internet 2025/2026.
