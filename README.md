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

O **IESPFLIX** é um backend completo para uma plataforma de streaming fictícia. Ele gerencia usuários, conteúdos, filmes, assinaturas, planos, favoritos, métodos de pagamento e funcionários, seguindo boas práticas de desenvolvimento Java com arquitetura em camadas, validações customizadas, logs rastreáveis e integração com APIs externas.

### ✨ Destaques

- 🔐 **Segurança** — senhas protegidas com BCrypt (hash unidirecional com salt)
- ✅ **Validação customizada** — `@CpfCnpj` com algoritmo de dígitos verificadores
- 📄 **Paginação** — retorno paginado com metadados em `/usuarios` e `/conteudos`
- 🔍 **Rastreabilidade** — UUID por requisição via `CorrelationIdFilter` + MDC
- 🌐 **Integrações externas** — ViaCEP (Feign) e BrasilAPI (RestTemplate)
- ⚠️ **Tratamento de erros** — `GlobalExceptionHandler` com respostas padronizadas
- 📚 **Documentação** — Swagger UI gerado automaticamente pelo SpringDoc

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.10 | Framework base |
| Spring Data JPA | - | ORM e acesso ao banco |
| Hibernate | - | Implementação JPA |
| H2 Database | - | Banco em arquivo (desenvolvimento) |
| Lombok | 1.18.32 | Redução de boilerplate |
| SpringDoc OpenAPI | 2.8.6 | Documentação Swagger |
| OpenFeign | - | Client HTTP declarativo (ViaCEP) |
| Spring Security Crypto | - | BCrypt para hash de senhas |
| JaCoCo | 0.8.11 | Cobertura de testes |

---

## 📁 Arquitetura

O projeto segue arquitetura em camadas com separação clara de responsabilidades:

```
br.uniesp.si.techback/
├── TechbackApplication.java     ← ponto de entrada (@SpringBootApplication)
│
├── client/                      ← clientes HTTP externos (Feign)
│   └── ViaCepClient.java
│
├── config/                      ← Beans globais e configurações
│   ├── AppConfig.java           ← BCryptPasswordEncoder + RestTemplate
│   └── OpenApiConfig.java       ← configuração Swagger
│
├── controller/                  ← camada HTTP · recebe e responde requisições
│
├── dto/                         ← objetos de transferência (Request / Response)
│   └── externo/                 ← DTOs de APIs de terceiros
│
├── exception/                   ← tratamento centralizado de erros
│   ├── GlobalExceptionHandler.java
│   └── CustomBeanException.java
│
├── filter/                      ← filtros HTTP (executados antes dos controllers)
│   └── CorrelationIdFilter.java ← UUID por requisição via MDC
│
├── mapper/                      ← conversão Entity ↔ DTO
│
├── model/                       ← entidades JPA (tabelas do banco)
│
├── repository/                  ← Spring Data JPA + queries JPQL
│
├── service/                     ← lógica de negócio
│   └── externo/                 ← integrações com APIs externas
│
└── validation/                  ← validações customizadas Bean Validation
    ├── CpfCnpj.java             ← anotação @CpfCnpj
    └── CpfCnpjValidator.java    ← algoritmo de dígitos verificadores
```

### Fluxo de uma requisição

```
Client (Swagger / Postman / React)
    ↓
CorrelationIdFilter  →  gera UUID e insere no MDC (aparece em todos os logs)
    ↓
Controller           →  valida DTO com @Valid · chama Service
    ↓
Service              →  aplica regras de negócio · usa Mapper e Repository
    ↓
Repository           →  persiste via JPA / executa JPQL personalizado
    ↓
H2 Database (arquivo ~/teckback20262)

Em caso de erro → GlobalExceptionHandler → JSON padronizado (400/404/409/500)
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
