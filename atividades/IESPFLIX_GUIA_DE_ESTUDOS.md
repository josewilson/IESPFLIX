# IESPFLIX — Guia de Estudos para a Apresentação

> Documento preparado para a equipe estudar e responder perguntas do professor.
> Baseado 100% no código real do projeto — nada inventado.
> Camadas na mesma ordem em que aparecem no projeto.

---

## PARTE 1 — VISÃO GERAL DO PROJETO

### O que é o IESPFLIX?

O IESPFLIX é um sistema de backend para uma plataforma de streaming fictícia, parecida com Netflix. Ele gerencia usuários, conteúdos, filmes, assinaturas, planos, favoritos, métodos de pagamento e funcionários. O projeto foi construído usando Spring Boot com Java 21, que é uma das tecnologias mais usadas no mercado para criar APIs REST.

A sigla API REST significa que o sistema expõe endereços chamados endpoints que recebem requisições HTTP — como GET para buscar dados, POST para criar, PUT para atualizar e DELETE para remover — e devolve respostas no formato JSON. O frontend ou qualquer ferramenta como Postman e Swagger se comunica com o backend por esses endereços.

### Por que Spring Boot?

Spring Boot é um framework que elimina a necessidade de configurar tudo manualmente. Ele já vem com um servidor embutido chamado Tomcat, gerencia as injeções de dependências automaticamente e tem integração nativa com banco de dados, validações e documentação. Usa o princípio de convenção sobre configuração, ou seja, se você seguir os padrões esperados, ele configura tudo sozinho sem que você precise escrever arquivos XML ou configurações extensas.

### Por que H2?

H2 é um banco de dados que roda dentro da própria aplicação, sem precisar instalar nada separado. Ele salva os dados em um arquivo no computador, no caso deste projeto em um arquivo chamado `teckback20262` na pasta do usuário. É ideal para projetos acadêmicos e desenvolvimento porque é simples de configurar e vem com um console web acessível pelo browser. Em produção real usaríamos PostgreSQL ou MySQL.

### Estrutura de pacotes do projeto

```
br.uniesp.si.techback/
├── TechbackApplication.java     ← ponto de entrada da aplicação
├── client/                      ← client Feign para ViaCEP
├── config/                      ← configurações de Beans e Swagger
├── controller/                  ← camada HTTP (entrada e saída)
├── dto/                         ← objetos de transferência de dados
│   └── externo/                 ← DTOs de APIs externas
├── exception/                   ← tratamento global de erros
├── filter/                      ← filtros HTTP (CorrelationId)
├── mapper/                      ← conversores entre entidade e DTO
├── model/                       ← entidades JPA (tabelas do banco)
├── repository/                  ← camada de acesso ao banco
├── service/                     ← camada de negócio
│   └── externo/                 ← serviços de integração externa
└── validation/                  ← validação customizada @CpfCnpj
```

---

## PARTE 2 — ARQUITETURA EM CAMADAS

### O que são camadas e por que o projeto usa essa abordagem

O projeto IESPFLIX é organizado em camadas, onde cada camada tem uma única responsabilidade e só se comunica com as camadas adjacentes. Essa organização segue o princípio de separação de responsabilidades, que torna o código mais fácil de manter, evoluir e entender. Se uma regra de negócio muda, só a camada de Service precisa ser alterada. Se o banco de dados muda, só a camada de Repository é afetada. As outras camadas continuam funcionando sem modificação.

O projeto tem onze camadas, cada uma com uma função bem definida. Abaixo a descrição de cada uma, o que faz, por que existe e com quem se comunica.

### client/

A camada `client/` contém interfaces de comunicação com APIs externas usando OpenFeign. Ela existe para encapsular toda a lógica de chamada HTTP a serviços de terceiros de forma declarativa. Ao declarar uma interface com as anotações de mapeamento, o Feign gera automaticamente o código de abertura de conexão, montagem da URL, envio da requisição e conversão da resposta. No projeto existe o `ViaCepClient`, que é injetado diretamente no `FuncionarioService` e no `EnderecoController` para consultar endereços por CEP. Essa camada não conhece nenhuma outra camada do projeto além dos DTOs externos que usa para mapear as respostas recebidas.

### config/

A camada `config/` registra os Beans globais e as configurações que precisam existir antes que qualquer outra parte do sistema funcione. É aqui que o `BCryptPasswordEncoder` e o `RestTemplate` são criados como Beans do Spring e disponibilizados para injeção em qualquer outro componente da aplicação. Também é aqui que o Swagger é configurado com o título, descrição e versão da API. Essa camada não depende de nenhuma outra camada do projeto — ela apenas produz objetos que as outras camadas consomem.

### controller/

A camada `controller/` é a porta de entrada HTTP do sistema. Cada classe de controller recebe requisições HTTP, aciona o Bean Validation para validar o DTO de entrada com `@Valid`, delega o processamento ao Service correspondente e devolve a resposta com o código HTTP correto. O controller não contém lógica de negócio. Ele não sabe como o usuário é criado, não sabe se o email é duplicado e não sabe como a senha é hasheada. Essas decisões são do Service. O controller conhece o `service/` e os `dto/`. Não conhece `model/`, `repository/` nem `mapper/` diretamente.

### dto/

A camada `dto/` contém os objetos de transferência de dados que definem o contrato da API. Os DTOs de Request representam o que o client envia e carregam as anotações de validação como `@NotBlank`, `@Email`, `@Past` e `@CpfCnpj`. Os DTOs de Response representam o que o servidor devolve, omitindo campos sensíveis como `senhaHash`. A subpasta `dto/externo/` separa os DTOs que modelam respostas de APIs de terceiros como ViaCEP e BrasilAPI, que seguem um formato que o projeto não controla. Essa camada não depende de nenhuma outra camada do projeto — é um pacote de dados puro.

### exception/

A camada `exception/` centraliza o tratamento de todos os erros da aplicação em um único lugar. A classe `CustomBeanException` é uma exceção personalizada usada para comunicar falhas de regras de negócio simples com mensagens claras. A classe `GlobalExceptionHandler` intercepta globalmente todas as exceções lançadas em qualquer controller ou service e as converte em respostas JSON padronizadas com código HTTP adequado. Sem essa camada, erros retornariam em formatos inconsistentes e poderiam expor detalhes técnicos internos ao client. Essa camada não depende de nenhuma outra camada do projeto além dos tipos de exceção que trata.

### filter/

A camada `filter/` contém filtros HTTP que interceptam todas as requisições antes que cheguem ao controller. O `CorrelationIdFilter` gera um UUID único para cada requisição e o armazena no MDC do SLF4J, fazendo com que esse identificador apareça automaticamente em todos os logs gerados durante o processamento daquela requisição. Isso permite rastrear toda a jornada de uma chamada nos logs mesmo quando várias requisições estão sendo processadas em paralelo. Essa camada não depende de nenhuma outra camada do projeto.

### mapper/

A camada `mapper/` converte entidades JPA em DTOs e DTOs em entidades JPA. Ela existe porque essas duas representações são intencionalmente diferentes: a entidade modela o banco de dados e o DTO modela o contrato da API. O Mapper centraliza essa conversão para que ela não fique espalhada dentro dos Services. Os Mappers conhecem tanto a camada `model/` quanto a camada `dto/`, mas não conhecem `repository/`, `controller/` nem `service/`.

### model/

A camada `model/` contém as entidades JPA que representam as tabelas do banco de dados. Cada classe é uma descrição da estrutura dos dados: quais campos existem, quais são os tipos, quais são as restrições de banco e quais são os relacionamentos com outras entidades. O Hibernate lê essas classes e cria ou atualiza as tabelas automaticamente. Essa camada não conhece nenhuma outra camada do projeto — ela apenas descreve dados.

### repository/

A camada `repository/` é responsável por toda comunicação com o banco de dados. Cada interface estende `JpaRepository` e ganha automaticamente os métodos de persistência básicos como `save()`, `findById()`, `findAll()` e `deleteById()`. Quando é necessária uma consulta mais específica, usa-se `@Query` com JPQL ou derivação por nome de método. Essa camada conhece apenas a camada `model/`. Não conhece `service/`, `controller/` nem `dto/`.

### service/

A camada `service/` contém a lógica de negócio da aplicação. É aqui que estão as regras que definem o que o sistema pode ou não fazer: verificar email duplicado antes de criar usuário, hashear a senha com BCrypt, validar existência de entidades relacionadas antes de criar vínculos, cancelar assinatura sem apagar o registro. Os Services conhecem `repository/`, `mapper/`, `dto/` e `model/`. A subpasta `service/externo/` separa os services que apenas se comunicam com APIs externas dos services que contêm regras de negócio do projeto, tornando claro que aquele código depende de algo fora do controle da aplicação.

### validation/

A camada `validation/` contém a validação customizada `@CpfCnpj`. Ela existe porque as anotações nativas do Bean Validation como `@NotBlank` e `@Email` não cobrem a validação de CPF com verificação de dígitos verificadores. A anotação `@CpfCnpj` funciona exatamente como as validações nativas e se integra ao mecanismo de `@Valid` do Spring sem nenhuma configuração adicional. Quando usada em um campo de DTO, é invocada automaticamente pelo Bean Validation durante o processamento da requisição no controller. Essa camada não depende de nenhuma outra camada do projeto.

### Como as camadas se comunicam em uma requisição real

Quando um client faz um `POST /usuarios`, o fluxo passa por várias camadas em sequência. Primeiro o `filter/` intercepta a requisição e atribui um UUID de rastreamento. Em seguida o `controller/` recebe o JSON e usa `dto/` para representar os dados de entrada, acionando `validation/` pelo mecanismo de `@Valid`. Se a validação passar, o controller chama o `service/`, que aplica as regras de negócio. O service usa o `mapper/` para converter o DTO em entidade, chama o `repository/` para persistir no banco usando a entidade de `model/`, recebe a entidade salva de volta, usa o `mapper/` novamente para converter em DTO de resposta e devolve ao controller. O controller monta a resposta HTTP com o código 201 e o header Location. Se em qualquer ponto uma exceção for lançada, a camada `exception/` a intercepta e devolve um JSON de erro padronizado. A camada `config/` fornece os Beans necessários para que tudo isso funcione, como o encoder de senha e o RestTemplate. A camada `client/` é acionada quando o fluxo precisa consultar uma API externa, como acontece ao incluir um funcionário com CEP.

---

## PARTE 3 — A CLASSE PRINCIPAL (TechbackApplication)

### O que é e para que serve

É o ponto de entrada de toda a aplicação. Quando você executa o projeto, o Java chama o método `main` desta classe. Ela contém apenas duas anotações e o método `main`, mas essas duas anotações são responsáveis por iniciar todo o ecossistema do Spring Boot: o servidor, o banco, o Swagger, os filtros, os controllers e tudo mais.

### Anotações usadas

`@SpringBootApplication` é a anotação mais importante do projeto inteiro. Ela equivale a três anotações combinadas em uma só. A primeira é `@Configuration`, que marca a classe como fonte de configurações de Beans. A segunda é `@EnableAutoConfiguration`, que instrui o Spring a detectar automaticamente tudo que está no classpath e configurar os componentes correspondentes, como o H2, o JPA, o servidor web, o Feign e o Bean Validation, sem que você precise declarar nada manualmente. A terceira é `@ComponentScan`, que faz o Spring varrer o pacote `br.uniesp.si.techback` e todos os subpacotes em busca de classes anotadas com `@Component`, `@Service`, `@Repository`, `@Controller` e derivadas, registrando cada uma como um Bean gerenciado no contexto da aplicação.

`@EnableFeignClients` ativa o suporte ao OpenFeign no projeto. Sem ela, a interface `ViaCepClient` marcada com `@FeignClient` não seria reconhecida pelo Spring e a aplicação não iniciaria. Essa anotação instrui o Spring a escanear o projeto em busca de interfaces `@FeignClient` e gerar automaticamente uma implementação para cada uma.

---

## PARTE 3 — CAMADA CLIENT (client/)

### O que é e para que serve

A pasta `client/` contém interfaces que representam clientes HTTP declarativos, construídos com a biblioteca OpenFeign. O objetivo desta camada é encapsular a comunicação com APIs externas de forma limpa, sem que o restante do código precise saber como a requisição HTTP é montada, enviada ou deserializada.

A ideia do Feign é que você declara uma interface Java com as anotações de mapeamento de URL e o Feign gera a implementação automaticamente. Você não escreve código de abertura de conexão, construção de URL, leitura de resposta ou conversão de JSON — tudo isso é feito pelo Feign nos bastidores.

### ViaCepClient

É uma interface anotada com `@FeignClient` que representa a API do ViaCEP. Quando qualquer classe do projeto precisa buscar um endereço por CEP, ela recebe o `ViaCepClient` por injeção de dependências e chama o método `buscarPorCep()` como se fosse um método local. O Feign intercepta essa chamada, monta a URL completa com o CEP informado, faz a requisição GET para a API do ViaCEP e converte a resposta JSON para um objeto `ViaCepResponseDTO` automaticamente.

### Anotações usadas

`@FeignClient` marca a interface como um client HTTP gerenciado pelo Feign. O atributo `name` é o identificador interno do client no contexto do Spring. O atributo `url` define a URL base da API — no projeto usa a notação `${viacep.url:https://viacep.com.br/ws}`, que lê o valor da propriedade `viacep.url` do `application.properties` e usa o endereço da internet como valor padrão caso a propriedade não esteja definida.

`@GetMapping` dentro da interface Feign funciona da mesma forma que nos controllers: define que o método faz uma requisição GET e especifica o caminho a ser concatenado com a URL base.

`@PathVariable` captura o parâmetro do método Java e o insere na variável correspondente da URL.

### Por que Feign e não RestTemplate para o ViaCEP?

O Feign é mais adequado quando a API externa tem uma estrutura bem definida de endpoints, como o ViaCEP. Você declara a interface e ele cuida de tudo. O `RestTemplate` é mais verboso porque você monta a URL, faz a chamada e converte manualmente. O projeto usa os dois intencionalmente — Feign para ViaCEP e RestTemplate para BrasilAPI — para demonstrar ambas as abordagens.

---

## PARTE 4 — CAMADA DE CONFIGURAÇÃO (config/)

### O que é e para que serve

A pasta `config/` contém classes que registram Beans globais e configurações que precisam existir antes que qualquer outra parte do sistema possa funcionar. Beans são objetos gerenciados pelo Spring — ele os cria uma vez, os armazena e os injeta onde for necessário ao longo de toda a aplicação.

### AppConfig

Registra dois Beans que são usados por diferentes camadas do projeto. O primeiro é o `BCryptPasswordEncoder`, que é injetado no `UsuarioService` para hashear senhas antes de salvar no banco. O segundo é o `RestTemplate`, que é injetado no `BrasilApiService` para fazer chamadas HTTP à API de feriados. Ao declarar esses dois objetos como `@Bean` aqui, o Spring garante que existe apenas uma instância de cada um em toda a aplicação, compartilhada por quem precisar.

### OpenApiConfig

Configura o Swagger com o título, a descrição e a versão da API. Sem esta configuração o Swagger funcionaria com informações genéricas. Com ela, a página do Swagger exibe o nome IESPFLIX API, a descrição do projeto e a versão 1.0.0.

### Anotações usadas

`@Configuration` marca a classe como fonte de configuração do Spring. O Spring a processa antes de qualquer Bean de aplicação, garantindo que os Beans declarados aqui estejam disponíveis para injeção em qualquer outra classe que os solicite.

`@Bean` declara que o método produz um objeto que deve ser registrado no contexto do Spring como um Bean gerenciado. O Spring chama o método uma vez, guarda o objeto retornado e o injeta onde for necessário pelo tipo. Isso é o que permite que `UsuarioService` declare `private final BCryptPasswordEncoder passwordEncoder` sem precisar criá-lo manualmente — o Spring sabe que existe um Bean desse tipo e o fornece.

---

## PARTE 5 — CAMADA DE CONTROLLERS (controller/)

### O que é e para que serve

A camada `controller/` é a porta de entrada HTTP do sistema. É aqui que chegam as requisições do client — seja o Swagger, o Postman, um frontend React ou qualquer outro. O Controller lê os dados que chegaram, aciona o Bean Validation para validar o DTO de entrada, chama o Service correspondente e devolve a resposta com o código HTTP correto.

O Controller não contém lógica de negócio. Ele não sabe como o usuário é criado, não sabe se o email é duplicado, não sabe como a senha é hasheada. Tudo isso é responsabilidade do Service. O Controller apenas orquestra: recebe, valida, delega, responde.

### Anotações usadas

`@RestController` combina duas anotações em uma. A primeira é `@Controller`, que registra a classe como um componente web do Spring MVC. A segunda é `@ResponseBody`, que instrui o Spring a serializar automaticamente o objeto retornado por cada método em JSON e colocá-lo no body da resposta HTTP. Sem `@ResponseBody`, o Spring tentaria interpretar o retorno como o nome de uma view HTML, o que não faz sentido em uma API REST.

`@RequestMapping` define o prefixo de URL compartilhado por todos os endpoints da classe. Todos os métodos herdam esse caminho base. Por exemplo, `@RequestMapping("/usuarios")` faz com que todos os endpoints da classe comecem com `/usuarios`.

`@RequiredArgsConstructor` do Lombok gera um construtor com todos os campos `final`, permitindo que o Spring injete o Service correspondente sem necessidade de `@Autowired` explícito.

`@Valid` ativa a validação Bean Validation no objeto anotado. Deve ser colocado imediatamente antes do `@RequestBody`. Se qualquer regra de validação falhar, o Spring lança `MethodArgumentNotValidException` antes de entrar no método, e o `GlobalExceptionHandler` captura e retorna um erro 400.

`@RequestBody` instrui o Spring a ler o body da requisição HTTP e converter o JSON recebido para o objeto Java correspondente, processo chamado de deserialização, realizado pelo Jackson.

`@PathVariable` extrai um valor dinâmico da URL. Em `GET /usuarios/{id}`, o valor entre chaves é capturado e atribuído ao parâmetro correspondente do método.

`@RequestParam` captura parâmetros da query string da URL. Em `GET /conteudos/top?n=5`, o `n=5` é capturado pelo parâmetro anotado com `@RequestParam`.

`@GetMapping`, `@PostMapping`, `@PutMapping` e `@DeleteMapping` são atalhos para `@RequestMapping` com o método HTTP já definido. São mais legíveis e diretos do que usar `@RequestMapping(method = RequestMethod.GET)`.

`@PageableDefault(size = 10)` define o tamanho padrão de página para endpoints que recebem um `Pageable`. Sem ele, o Spring usaria 20 como padrão. O client pode sobrescrever passando `?page=0&size=5&sort=nome` na URL.

### Códigos HTTP retornados e quando

`201 Created` é retornado em todo POST bem-sucedido. É construído com `ResponseEntity.created(location).body(dto)`, onde `location` é a URI do recurso recém-criado montada com `ServletUriComponentsBuilder`. O header `Location` na resposta informa ao client onde o novo recurso pode ser acessado.

`200 OK` é retornado em operações GET e PUT bem-sucedidas, construído com `ResponseEntity.ok(dto)`.

`204 No Content` é retornado em DELETE bem-sucedido. Não tem body. Construído com `ResponseEntity.noContent().build()`.

`400 Bad Request` é retornado pelo `GlobalExceptionHandler` quando a validação falha ou uma regra de negócio simples é violada.

`404 Not Found` é retornado pelo `GlobalExceptionHandler` quando o recurso solicitado não existe no banco.

`409 Conflict` é retornado pelo `GlobalExceptionHandler` quando há tentativa de criar um dado duplicado, como um email já cadastrado.

`500 Internal Server Error` é retornado pelo `GlobalExceptionHandler` para qualquer erro não previsto pelos handlers anteriores.

### Observação sobre o FuncionarioController

O `FuncionarioController` recebe e retorna a entidade `Funcionario` diretamente, sem usar DTOs. Isso é uma simplificação válida funcionalmente, mas não é a melhor prática, pois expõe a estrutura interna da entidade ao client. Todos os outros módulos usam DTOs separados para entrada e saída.

---

## PARTE 6 — CAMADA DE DTOs (dto/)

### O que é e para que serve

DTO significa Data Transfer Object, que em português significa Objeto de Transferência de Dados. São objetos simples usados exclusivamente para transportar dados entre o client e o servidor. O projeto usa dois tipos para cada módulo: os DTOs de Request, que representam o que chega na requisição enviada pelo client, e os DTOs de Response, que representam o que o servidor devolve como resposta.

### Por que não retornar a entidade diretamente?

Porque a entidade tem campos sensíveis que nunca devem ser expostos ao client. O campo `senhaHash` da entidade `Usuario`, por exemplo, jamais deve aparecer em uma resposta. Além disso, a entidade carrega anotações de persistência como `@Entity` e `@Table` que não fazem sentido na camada HTTP. O DTO permite controlar com precisão exatamente quais campos entram e quais saem, criando um contrato claro e seguro para a API.

### Anotações usadas nos DTOs

`@Data` do Lombok gera automaticamente getters, setters, `equals()`, `hashCode()` e `toString()` para todos os campos. Usado em todos os DTOs.

`@Builder` do Lombok habilita o padrão de construção por encadeamento de métodos. É usado extensivamente nos Mappers quando eles montam os objetos de resposta a partir das entidades.

`@NoArgsConstructor` do Lombok gera um construtor sem argumentos. O Jackson, biblioteca responsável por converter JSON em objetos Java e vice-versa, exige obrigatoriamente um construtor sem parâmetros para conseguir instanciar o DTO ao ler o body da requisição.

`@AllArgsConstructor` do Lombok gera um construtor com todos os campos. É necessário junto com `@Builder` para que o padrão builder funcione corretamente.

`@NotBlank` valida que o campo não é nulo, não está vazio e não contém apenas espaços. Usado nos campos `nome`, `email`, `senha`, `tipo` e `tokenizado`.

`@NotNull` valida que o campo não é nulo. Usado em campos numéricos e booleanos como `usuarioId`, `planoId` e `principal`, onde o valor vazio não faz sentido semântico.

`@Email` valida que o valor respeita o formato de email, verificando a presença do arroba e de um domínio válido.

`@Size(min = 8)` valida o comprimento mínimo de uma String. Usado no campo `senha` para garantir ao menos 8 caracteres.

`@Past` valida que a data informada é no passado. Usado em `dataNascimento` para impedir que alguém informe uma data de nascimento futura.

`@Min(1888)` e `@Max(2100)` definem limites numéricos inteiros. Usados no campo `ano` do `ConteudoRequestDTO`. O mínimo é 1888 porque é o ano do primeiro filme da história.

`@Positive` valida que o número é maior que zero. Usado em `duracaoMinutos`.

`@DecimalMin("0.0")` e `@DecimalMax("10.0")` definem limites para números decimais. Usados em `relevancia` para garantir que a nota fica entre zero e dez.

`@CpfCnpj` é a validação customizada criada pelo próprio projeto. Valida o CPF aplicando o algoritmo oficial de dígitos verificadores ou aceita CNPJ pelo tamanho de 14 dígitos. Explicada em detalhe na seção da camada `validation/`.

### DTOs externos (dto/externo/)

A subpasta `externo/` existe dentro de `dto/` para separar os DTOs que representam respostas de APIs de terceiros dos DTOs que fazem parte do contrato da própria API do IESPFLIX. Tudo que está em `externo/` não foi criado pelo projeto — foi modelado a partir do formato que uma API externa devolve, e você não tem controle sobre ele.

Os DTOs internos como `UsuarioRequestDTO` e `ConteudoResponseDTO` representam o contrato que o IESPFLIX oferece para quem o consome. Os DTOs externos representam o contrato que APIs de terceiros oferecem para o IESPFLIX. São direções opostas, e mantê-los separados torna essa distinção visível na estrutura de pastas. Se a BrasilAPI ou o ViaCEP mudarem o nome de um campo, só esses arquivos precisam ser alterados, sem afetar o restante do sistema.

`FeriadoResponse` é o DTO que modela o formato JSON que a BrasilAPI devolve ao consultar feriados. Tem três campos: `date`, `name` e `type`. Usa apenas `@Data` e `@NoArgsConstructor` porque só recebe dados — nunca é enviado pelo client. O client do IESPFLIX nunca vê esse DTO diretamente na forma bruta — o `FeriadoController` o recebe do `BrasilApiService` e devolve ao client como parte de uma lista.

`ViaCepResponseDTO` é o DTO que modela o formato JSON que o ViaCEP devolve ao consultar um endereço. Seus campos são `cep`, `logradouro`, `complemento`, `bairro`, `localidade`, `uf` e `erro`. O campo `erro` é um Boolean que o ViaCEP preenche com `true` quando o CEP informado não existe em sua base de dados. O `EnderecoController` e o `FuncionarioService` verificam esse campo antes de usar os dados do endereço, lançando uma exceção caso seja verdadeiro.

---

## PARTE 7 — CAMADA DE EXCEÇÕES (exception/)

### O que é e para que serve

A pasta `exception/` centraliza o tratamento de erros da aplicação. Sem ela, quando um erro ocorre o Spring devolve uma resposta padrão que pode expor detalhes técnicos internos ao client, como stack traces, ou retornar formatos inconsistentes de erro. Com o tratamento centralizado, todos os erros da aplicação devolvem um JSON com o mesmo formato, independentemente de onde o erro ocorreu.

### CustomBeanException

É uma exceção personalizada simples que estende `RuntimeException`. Por ser uma exceção unchecked, pode ser lançada de qualquer camada sem poluir as assinaturas dos métodos com cláusulas `throws`. É usada quando uma regra de negócio simples falha e precisa comunicar uma mensagem clara ao client — por exemplo, quando o CEP informado tem menos de 8 dígitos ou quando o ViaCEP retorna que o CEP não existe. Quem a cria passa a mensagem de erro no construtor, e o `GlobalExceptionHandler` a captura e devolve um erro 400 com essa mensagem.

### GlobalExceptionHandler

É a classe que centraliza o tratamento de todas as exceções da aplicação. Quando qualquer controller ou service lança uma exceção, o Spring a intercepta e procura nesta classe um método capaz de tratá-la. Se encontrar, executa o método e devolve a resposta definida nele. Se não encontrar um handler específico, o handler genérico do tipo `Exception` é acionado.

O formato padrão de todas as respostas de erro tem os campos `timestamp` com a data e hora do erro, `status` com o código HTTP numérico, `error` com o nome do status, `message` com uma descrição legível do problema e `path` com o endpoint que gerou o erro. Esse padrão é consistente em toda a API.

As exceções tratadas e seus respectivos códigos HTTP são: `MethodArgumentNotValidException` retorna 400 e é lançada automaticamente pelo Spring quando `@Valid` falha; `CustomBeanException` retorna 400 e é lançada manualmente pelos services e controllers para regras de negócio simples; `EntityNotFoundException` retorna 404 e é lançada pelos services quando um registro não é encontrado no banco; `DataIntegrityViolationException` retorna 409 e é lançada quando uma constraint do banco é violada, como email duplicado; e o handler genérico `Exception` retorna 500 para qualquer erro não previsto pelos handlers anteriores, sem expor detalhes técnicos.

### Anotações usadas

`@RestControllerAdvice` combina `@ControllerAdvice`, que define a classe como interceptora global de exceções de todos os controllers da aplicação, com `@ResponseBody`, que garante que os retornos dos métodos de tratamento sejam serializados como JSON.

`@ExceptionHandler` marca o método como tratador de um tipo específico de exceção. O Spring verifica os tipos na ordem de declaração e usa o primeiro que corresponde ao tipo lançado.

---

## PARTE 8 — CAMADA DE FILTROS (filter/)

### O que é e para que serve

A pasta `filter/` contém filtros HTTP que interceptam toda requisição antes que ela chegue ao controller. Um filtro tem acesso ao objeto da requisição e da resposta e pode executar qualquer lógica antes de passar a chamada adiante na cadeia de filtros. No projeto existe um único filtro, o `CorrelationIdFilter`, responsável pela rastreabilidade dos logs.

### CorrelationIdFilter

Para cada requisição HTTP que chega ao sistema, o `CorrelationIdFilter` gera um UUID — um identificador único universal — e o armazena no MDC com a chave `correlationId`. Em seguida, passa a requisição adiante para o controller. Ao final da requisição, independente de ter ocorrido erro ou não, o filtro limpa o MDC para não contaminar requisições futuras que possam reutilizar a mesma thread.

O MDC, ou Mapped Diagnostic Context, é um mapa de chave-valor fornecido pela biblioteca SLF4J que fica associado à thread atual. Como cada requisição HTTP roda em uma thread, os valores colocados no MDC aparecem automaticamente em todos os logs emitidos durante o processamento daquela requisição. O `application.properties` configura o padrão de log para incluir o `correlationId` em cada linha impressa.

O benefício prático é que em um sistema com múltiplas requisições paralelas, onde os logs se misturam no console, é possível filtrar todos os logs de uma requisição específica pelo seu UUID. Se um usuário reportar um erro às 10h32, basta procurar o `correlationId` daquela chamada para ver exatamente o que aconteceu, passo a passo.

O bloco `finally` que chama `MDC.clear()` é fundamental. Servidores web usam um pool de threads — a mesma thread que processou uma requisição pode processar outra logo depois. Sem o `finally`, o `correlationId` da requisição anterior vazaria para a próxima, misturando os logs de forma incorreta.

### Anotações usadas

`@Component` registra o filtro no contexto do Spring, que o adiciona automaticamente à cadeia de filtros do Servlet. Todo request HTTP passa por aqui antes de chegar ao controller, sem necessidade de configuração adicional.

---

## PARTE 9 — CAMADA DE MAPPERS (mapper/)

### O que é e para que serve

A pasta `mapper/` contém classes responsáveis por converter entidades JPA em DTOs e DTOs em entidades JPA. Essa conversão é necessária porque as entidades representam a estrutura do banco de dados e os DTOs representam o contrato da API. Os dois formatos não são idênticos — o DTO omite campos sensíveis como `senhaHash`, pode agrupar dados de entidades diferentes e pode ter campos calculados.

A conversão não é responsabilidade do Service, que cuida de regras de negócio, nem do Controller, que cuida do HTTP. O Mapper é uma camada dedicada exclusivamente a essa transformação, o que a centraliza e permite reuso por qualquer Service que precise converter o mesmo tipo.

Todos os Mappers do projeto são classes manuais anotadas com `@Component`. Isso significa que o Spring os gerencia como Beans e os injeta nos Services por construtor quando declarados como `private final`.

### Os 4 Mappers do projeto

`UsuarioMapper` tem dois métodos. O `toEntity` recebe um `UsuarioRequestDTO` e constrói uma entidade `Usuario` usando o Builder, copiando os campos de nome, email, CPF/CNPJ, data de nascimento e perfil. A senha não é copiada aqui — ela é hasheada pelo `UsuarioService` depois da conversão. O `toResponseDTO` recebe uma entidade `Usuario` e constrói um `UsuarioResponseDTO` com todos os campos públicos, incluindo `criadoEm`, mas excluindo `senhaHash`.

`ConteudoMapper` tem dois métodos. O `toEntity` e o `toResponseDTO` copiam todos os oito campos da entidade `Conteudo` usando o Builder. Se o objeto de entrada for nulo, retorna nulo.

`FavoritoMapper` tem apenas o método `toResponseDTO`. Ao montar o DTO de resposta, ele navega os relacionamentos do favorito para buscar o nome do usuário em `favorito.getUsuario().getNome()` e o título do conteúdo em `favorito.getConteudo().getTitulo()`, enriquecendo a resposta com dados das entidades relacionadas sem que o client precise fazer chamadas adicionais.

`FilmeMapper` converte entre a entidade `Filme` e o `FilmeDTO`, que no módulo de filmes é usado tanto como DTO de entrada quanto de saída.

### O AssinaturaService não usa Mapper separado

O `AssinaturaService` tem um método privado `toResponseDTO()` dentro do próprio Service em vez de um Mapper separado. É uma decisão de design válida para módulos onde a conversão é simples e não há necessidade de reuso em outros Services.

### Anotação usada

`@Component` registra a classe como um Bean genérico no contexto do Spring. É a anotação base de todas as outras — `@Service`, `@Repository` e `@Controller` são especializações de `@Component` com semântica adicional.

---

## PARTE 10 — CAMADA DE MODELO (model/)

### O que é e para que serve

A pasta `model/` contém as entidades JPA. Cada classe Java desta camada representa exatamente uma tabela no banco de dados H2. O Hibernate lê as anotações presentes nas classes e cria ou atualiza as tabelas automaticamente ao iniciar a aplicação, sem que você precise escrever nenhum SQL de criação de tabela. Esse comportamento é controlado pela configuração `spring.jpa.hibernate.ddl-auto=update` no `application.properties`.

Esta camada não conhece nenhuma outra camada. As entidades não chamam services, não chamam repositories e não chamam controllers. Elas apenas descrevem a estrutura dos dados: quais campos existem, quais são os tipos, quais são as restrições e quais são os relacionamentos com outras entidades.

Quem usa as entidades são os Repositories, que recebem e retornam entidades ao se comunicar com o banco. Os Services usam entidades internamente para aplicar regras de negócio. Os Mappers convertem entidades em DTOs e DTOs em entidades. O client nunca vê a entidade diretamente — sempre vê um DTO.

### Anotações usadas nas entidades

`@Entity` marca a classe Java como uma entidade JPA. O Hibernate a registra e cria a tabela correspondente no banco de dados. Sem essa anotação, a classe é completamente ignorada pelo JPA.

`@Table(name = "nome")` define o nome exato da tabela no banco. Se omitido, o Hibernate usa o nome da classe Java. No projeto as tabelas se chamam `usuarios`, `conteudo`, `filmes`, `favorito`, `plano`, `assinatura`, `metodo_pagamento` e `funcionarios`.

`@Id` marca o campo como chave primária da tabela. É obrigatório em toda entidade JPA. No projeto é sempre o campo `id` do tipo `Long`.

`@GeneratedValue(strategy = GenerationType.IDENTITY)` define que o valor do ID é gerado automaticamente pelo banco de dados. A estratégia `IDENTITY` instrui o banco a incrementar o valor a cada novo registro inserido. O código Java nunca precisa definir o `id` manualmente.

`@Column` personaliza como o campo Java é mapeado para a coluna no banco. O atributo `unique = true` cria uma constraint de unicidade, usado no campo `email` de `Usuario` para impedir dois usuários com o mesmo email. O atributo `nullable = false` torna o campo obrigatório no nível do banco. O atributo `length` define o tamanho máximo do VARCHAR, como `length = 100` no `titulo` de `Filme`. O atributo `columnDefinition = "TEXT"` define o tipo da coluna como TEXT, sem limite fixo de tamanho, usado em campos de sinopse. O atributo `name` renomeia a coluna no banco quando o nome Java usa camelCase mas o banco precisa de snake_case, como `dataLancamento` mapeado para `data_lancamento`.

`@ManyToOne` define um relacionamento onde muitos registros desta entidade apontam para um único registro de outra entidade. O JPA cria automaticamente uma coluna de chave estrangeira. No projeto, `Assinatura` tem `@ManyToOne` para `Usuario` e para `Plano`; `Favorito` tem `@ManyToOne` para `Usuario` e para `Conteudo`; `MetodoPagamento` tem `@ManyToOne` para `Usuario`.

`@JoinColumn(name = "coluna_fk")` define o nome exato da coluna de chave estrangeira criada no banco. É sempre usado junto com `@ManyToOne`. Por exemplo, `@JoinColumn(name = "usuario_id")` cria a coluna `usuario_id` na tabela da entidade que possui o relacionamento, apontando para o `id` da tabela de usuários.

`@NotBlank` do Bean Validation é usado diretamente na entidade `Conteudo` no campo `titulo`, funcionando como uma validação adicional no nível da entidade.

`@CreationTimestamp` é uma anotação do Hibernate que preenche o campo automaticamente com a data e hora exatas do momento em que o registro é inserido no banco. O código Java nunca precisa setar esse valor. É usado em `criadoEm` de `Usuario` e `adicionadoEm` de `Favorito`.

`@Data`, `@Builder`, `@NoArgsConstructor` e `@AllArgsConstructor` são anotações do Lombok usadas em todas as 8 entidades. O `@Data` gera getters, setters, `equals()`, `hashCode()` e `toString()`. O `@Builder` habilita a construção por encadeamento. O `@NoArgsConstructor` é obrigatório para o JPA instanciar a entidade. O `@AllArgsConstructor` é necessário para o `@Builder` funcionar.

### As 8 entidades com seus campos reais

`Usuario` representa a tabela `usuarios`. Campos: `id`, `nome`, `email` com restrição de unicidade, `senhaHash` que armazena o hash BCrypt nunca o texto puro, `cpfCnpj`, `dataNascimento`, `perfil` como texto livre com valores como ADMIN ou USUARIO, e `criadoEm` preenchido automaticamente pelo Hibernate.

`Conteudo` representa a tabela `conteudo`. Campos: `id`, `titulo`, `tipo`, `ano`, `duracaoMinutos`, `relevancia` do tipo Double, `sinopse` como TEXT, `trailerUrl` e `genero`.

`Filme` representa a tabela `filmes`. Campos: `id`, `titulo` com restrição de nulidade e limite de 100 caracteres, `sinopse` como TEXT, `dataLancamento`, `genero` com limite de 50 caracteres, `duracaoMinutos` e `classificacaoIndicativa` com limite de 10 caracteres.

`Favorito` representa a tabela `favorito`. Campos: `id`, `usuario` como ManyToOne para `Usuario`, `conteudo` como ManyToOne para `Conteudo`, e `adicionadoEm` preenchido automaticamente pelo Hibernate.

`Plano` representa a tabela `plano`. Campos: `id`, `nome`, `descricao`, `preco` do tipo Double e `limiteDispositivos`.

`Assinatura` representa a tabela `assinatura`. Campos: `id`, `usuario` como ManyToOne para `Usuario`, `plano` como ManyToOne para `Plano`, `dataInicio`, `dataFim` e `status` como texto que recebe os valores ATIVA ao criar ou CANCELADA ao cancelar.

`MetodoPagamento` representa a tabela `metodo_pagamento`. Campos: `id`, `usuario` como ManyToOne para `Usuario`, `tipo` com valores como CARTAO_CREDITO ou PIX, `tokenizado` com o token gerado pelo gateway de pagamento, e `principal` do tipo Boolean que indica se é o método padrão do usuário.

`Funcionario` representa a tabela `funcionarios`. Campos: `id`, `nome`, `cargo`, `cep` digitado pelo usuário, e os campos `logradouro`, `bairro`, `localidade` e `uf` que são preenchidos automaticamente pelo `FuncionarioService` ao consultar o ViaCEP com o CEP informado.

### Como o @ManyToOne se traduz no banco de dados

Quando o Hibernate vê `@ManyToOne` com `@JoinColumn(name = "usuario_id")` na entidade `Favorito`, ele cria no banco uma coluna `usuario_id` na tabela `favorito` com uma chave estrangeira apontando para a coluna `id` da tabela `usuarios`. O mesmo acontece para `conteudo_id` apontando para `conteudo`. Quando o código acessa `favorito.getUsuario()`, o JPA carrega o objeto `Usuario` completo do banco automaticamente.

---

## PARTE 11 — CAMADA DE REPOSITÓRIOS (repository/)

### O que é e para que serve

A pasta `repository/` é responsável por toda comunicação com o banco de dados. Cada interface representa o acesso aos dados de uma entidade específica. No Spring Data JPA, você apenas declara a interface estendendo `JpaRepository` e o Spring gera em memória, durante a inicialização da aplicação, uma implementação completa com todos os métodos de persistência.

Você não escreve nenhuma linha de código de implementação. Não abre conexão, não escreve SQL básico, não trata ResultSet. Tudo isso é gerado automaticamente pelo Spring Data JPA. Quando precisa de uma consulta mais específica, usa JPQL com `@Query`.

### Anotações usadas

`@Repository` marca a interface como um componente de acesso a dados do Spring. Além de registrá-la como Bean, essa anotação ativa a tradução automática de exceções de persistência do JPA para as exceções do Spring, tornando o tratamento de erros mais previsível e uniforme.

`@Query` define uma consulta JPQL customizada para o método. O Spring usa essa query em vez de tentar derivar a consulta pelo nome do método. É usada quando a consulta precisa de lógica que não cabe em um nome de método, como funções `LOWER()`, `LIKE`, `CONCAT` ou ordenações específicas.

`@Param` liga o parâmetro Java ao parâmetro nomeado na query JPQL, identificado por dois pontos seguido do nome. É obrigatório quando a query usa parâmetros nomeados.

### O que o JpaRepository fornece automaticamente

Ao estender `JpaRepository<Entidade, Long>`, todos os repositories herdam automaticamente: `save()` para inserir ou atualizar, `findById()` que retorna um `Optional`, `findAll()` que retorna todos os registros, `deleteById()` para remover por ID, `existsById()` para verificar existência sem carregar o objeto, e `count()` para contar registros. Tudo isso sem escrever uma linha de código.

### JPQL: o que é e como difere do SQL

JPQL é a Java Persistence Query Language. Ela usa os nomes das classes Java e dos campos Java, não os nomes das tabelas e colunas do banco. O Hibernate traduz a JPQL para o SQL específico do banco em uso, tornando o código independente do banco de dados. A mesma JPQL funciona igual em H2, PostgreSQL e MySQL.

A diferença prática: em SQL seria `SELECT * FROM conteudo WHERE LOWER(genero) = LOWER(?)`, enquanto em JPQL é `SELECT c FROM Conteudo c WHERE LOWER(c.genero) = LOWER(:genero)`. O `c` é um alias para a classe `Conteudo`, e `c.genero` é o campo Java `genero` da entidade, não o nome da coluna no banco.

### Três formas de criar consultas no projeto

A primeira forma são os métodos herdados automaticamente do `JpaRepository`, como `save()`, `findById()` e `deleteById()`, presentes em todos os repositories.

A segunda forma é a derivação por nome de método. O Spring lê o nome do método e gera o SQL automaticamente. No projeto: `findByEmail()` gera `WHERE email = ?`, `findByUsuarioId()` gera um JOIN com `WHERE usuario_id = ?`, `findAllByOrderByTituloAsc()` gera `ORDER BY titulo ASC`, e `findByGeneroAndTitulo()` gera `WHERE genero = ? AND titulo = ?`.

A terceira forma é a anotação `@Query` com JPQL manual, usada quando a consulta é mais complexa. Exemplos reais do projeto: o `ConteudoRepository` tem uma query que filtra por gênero ignorando maiúsculas e minúsculas usando `LOWER()`; tem outra que ordena por relevância decrescente para o top de conteúdos; tem uma que filtra conteúdos lançados após um determinado ano; e tem uma que busca simultaneamente em título e sinopse usando `LIKE` com `CONCAT`. O `FavoritoRepository` tem uma query que retorna os favoritos de um usuário ordenados do mais recente ao mais antigo navegando o relacionamento diretamente com `f.usuario.id`. O `MetodoPagamentoRepository` tem uma query que retorna os métodos de pagamento do usuário com o marcado como principal vindo primeiro.

---

## PARTE 12 — CAMADA DE SERVIÇOS (service/)

### O que é e para que serve

A pasta `service/` contém a camada de negócio da aplicação. É aqui que ficam as regras que definem o que o sistema pode ou não fazer. Os Services recebem os dados já validados do Controller, aplicam as regras de negócio, utilizam os Repositories para persistir dados e os Mappers para converter entre entidades e DTOs, e devolvem DTOs prontos para o Controller responder.

O Service não sabe nada sobre HTTP. Ele não conhece códigos de status, não conhece headers de resposta e não conhece o formato JSON. Toda essa responsabilidade fica no Controller. O Service sabe apenas sobre o domínio do negócio.

### Anotações comuns a todos os Services

`@Service` é uma especialização de `@Component` que registra a classe como Bean de serviço. A distinção semântica indica que a classe contém lógica de negócio, diferenciando-a de um `@Component` genérico ou de um `@Repository` de acesso a dados.

`@RequiredArgsConstructor` do Lombok gera um construtor com todos os campos `final`, permitindo que o Spring injete as dependências automaticamente. É a forma recomendada de injeção no Spring — mais segura e testável do que usar `@Autowired` diretamente nos campos.

`@Slf4j` do Lombok gera automaticamente um atributo `log` do tipo `Logger`. Permite usar `log.info()`, `log.warn()`, `log.error()` e `log.debug()` diretamente no código sem declarar o logger manualmente.

`@Transactional` envolve o método em uma transação de banco de dados. Se qualquer operação dentro do método lançar uma exceção, todas as operações de escrita realizadas até aquele ponto são desfeitas automaticamente, garantindo que o banco nunca fique em um estado inconsistente. É usado em todos os métodos que escrevem no banco. Métodos apenas de leitura não precisam da anotação.

### Regras de negócio implementadas em cada Service

`UsuarioService` tem três regras principais. Antes de criar um usuário, verifica se já existe outro com o mesmo email usando `findByEmail()` do repository. Se existir, lança `DataIntegrityViolationException` antes mesmo de tentar salvar, gerando uma resposta 409. A senha nunca é salva em texto puro — sempre passa pelo `passwordEncoder.encode()` antes de ir para o banco. Na atualização, a senha só é re-encriptada se o campo vier preenchido na requisição, permitindo atualizar nome, email e perfil sem alterar a senha existente.

`FuncionarioService` integra o ViaCEP diretamente na regra de negócio. Ao incluir um funcionário, se o CEP vier preenchido, o service chama o `ViaCepClient` para buscar os dados do endereço e preenche automaticamente os campos `logradouro`, `bairro`, `localidade` e `uf` na entidade antes de salvar. Se o ViaCEP retornar que o CEP não existe, lança `CustomBeanException`.

`AssinaturaService` valida a existência do usuário e do plano antes de criar a assinatura. Se qualquer um não existir no banco, lança `EntityNotFoundException` antes de tentar persistir. O cancelamento não apaga o registro — apenas altera o campo `status` de ATIVA para CANCELADA e salva novamente, preservando o histórico.

`FavoritoService` verifica a existência tanto do usuário quanto do conteúdo antes de adicionar o favorito, lançando `EntityNotFoundException` para qualquer dos dois que não for encontrado. A listagem usa a query JPQL ordenada por `adicionadoEm` descendente para retornar os mais recentes primeiro.

`ConteudoService` usa `Pageable.ofSize(n)` no método `topPorRelevancia()` para limitar dinamicamente quantos resultados a query retorna. Isso permite que o client informe quantos itens quer sem que o limite seja fixo no código.

### Por que service/externo/ existe

A subpasta `externo/` dentro de `service/` existe para separar serviços que contêm lógica de negócio do projeto de serviços que existem apenas para se comunicar com APIs de terceiros. Um service como `UsuarioService` conhece as regras do negócio do IESPFLIX. Já o `BrasilApiService` não tem nenhuma regra de negócio própria — ele só sabe como chamar a BrasilAPI e devolver a resposta.

Essa separação é uma decisão de organização que comunica imediatamente para qualquer desenvolvedor que tudo dentro de `externo/` depende de algo fora do controle do projeto. Se a BrasilAPI sair do ar, mudar sua URL ou mudar o formato da resposta, o impacto fica contido em `service/externo/` e `dto/externo/`, sem se propagar para os services centrais do sistema.

O ViaCEP não tem um service em `externo/` porque usa Feign. A interface `ViaCepClient` com `@FeignClient` já está na pasta `client/`, que por si só já comunica que se trata de integração externa. Criar um service adicional seria redundante.

### BrasilApiService (service/externo/)

É o único service da subpasta `externo/`. Sua única responsabilidade é montar a URL com o ano recebido como parâmetro, fazer a requisição GET usando o `RestTemplate` injetado por construtor, converter a resposta JSON em uma lista de objetos `FeriadoResponse` e devolvê-la ao `FeriadoController`.

O `getForObject()` do RestTemplate faz a requisição, recebe o JSON e converte automaticamente para o array de objetos usando o Jackson. Se a API retornar nulo, o service devolve uma lista vazia em vez de propagar uma `NullPointerException`, protegendo o sistema de falhas silenciosas da API externa.

---

## PARTE 13 — CAMADA DE VALIDAÇÃO (validation/)

### O que é e para que serve

A pasta `validation/` contém a validação customizada criada pelo projeto. O Jakarta Bean Validation permite criar novas anotações de validação que funcionam exatamente como as nativas `@NotBlank`, `@Email` e `@Past`, integrando-se naturalmente ao mecanismo de `@Valid` do Spring. Para criar uma validação customizada são necessários dois arquivos: a anotação e o validador.

### A anotação @CpfCnpj

É uma anotação Java personalizada que pode ser colocada em qualquer campo do tipo String de um DTO. Quando o Spring processa o `@Valid` no Controller, detecta o `@CpfCnpj` no campo e chama automaticamente o `CpfCnpjValidator` para executar a validação.

As meta-anotações que a definem têm funções específicas. `@Constraint(validatedBy = CpfCnpjValidator.class)` liga a anotação à classe que executa a lógica de validação real. `@Target(ElementType.FIELD)` define que a anotação só pode ser usada em campos de uma classe. `@Retention(RetentionPolicy.RUNTIME)` define que a anotação persiste em tempo de execução, o que é obrigatório para que o Bean Validation consiga lê-la quando está processando as validações. Os três atributos `message()`, `groups()` e `payload()` são obrigatórios pelo contrato do Bean Validation — o `message()` define a mensagem padrão de erro que aparece quando a validação falha.

### O validador CpfCnpjValidator

Implementa a interface `ConstraintValidator<CpfCnpj, String>`, onde o primeiro tipo genérico é a anotação e o segundo é o tipo do campo que será validado. O método `isValid()` é chamado pelo Bean Validation com o valor do campo e deve retornar `true` se o valor for válido ou `false` caso contrário.

A lógica do validador primeiro verifica se o valor é nulo e, nesse caso, retorna `true`, deixando para `@NotNull` ou `@NotBlank` o controle de campos obrigatórios. Em seguida remove todos os caracteres que não são dígitos usando uma expressão regular. Se restar 11 dígitos, aplica o algoritmo de verificação de CPF. Se restar 14 dígitos, considera válido pelo tamanho do CNPJ. Qualquer outro comprimento é inválido.

O algoritmo do CPF primeiro rejeita sequências de dígitos iguais como 111.111.111-11, que tecnicamente passariam pelo cálculo mas são CPFs inválidos amplamente conhecidos. Em seguida faz uma soma ponderada dos nove primeiros dígitos com pesos decrescentes de 10 até 2, calcula o primeiro dígito verificador como 11 menos o resto da divisão por 11, e compara com o décimo dígito informado. Repete o processo com os dez primeiros dígitos e pesos de 11 até 2 para obter o segundo dígito verificador e compara com o décimo primeiro dígito. Se ambos os dígitos calculados coincidirem com os informados, o CPF é válido.

---

## PARTE 14 — LOMBOK: TODAS AS ANOTAÇÕES USADAS

| Anotação | O que gera | Onde é usada no projeto |
|----------|------------|------------------------|
| `@Data` | getters, setters, equals, hashCode, toString | Todas as entidades e DTOs |
| `@Builder` | método `builder()` e classe interna Builder | Entidades, DTOs, Mappers, Services |
| `@NoArgsConstructor` | construtor sem parâmetros | Entidades (JPA exige), DTOs (Jackson exige) |
| `@AllArgsConstructor` | construtor com todos os parâmetros | Necessário junto com `@Builder` |
| `@RequiredArgsConstructor` | construtor com campos `final` | Controllers, Services, Mappers, Filters |
| `@Slf4j` | atributo `log` do tipo Logger | Services que emitem logs |

---

## PARTE 15 — SPRING DATA JPA: MÉTODOS AUTOMÁTICOS, DERIVADOS E JPQL

O Spring Data JPA oferece três formas de criar consultas nos repositories. A primeira são os métodos herdados automaticamente do `JpaRepository`, como `save()`, `findById()`, `findAll()`, `deleteById()` e `existsById()`, presentes em todos os repositories sem escrever nenhuma linha de código.

A segunda é a derivação por nome de método. O Spring lê o nome do método e gera o SQL automaticamente. Exemplos do projeto: `findByEmail()` gera `WHERE email = ?`, `findByUsuarioId()` gera um JOIN com `WHERE usuario_id = ?`, `findAllByOrderByTituloAsc()` gera `ORDER BY titulo ASC`, e `findByGeneroAndTitulo()` gera `WHERE genero = ? AND titulo = ?`.

A terceira é a anotação `@Query` com JPQL para consultas que não cabem em nomes de método ou que precisam de funções como `LOWER()`, `LIKE`, `CONCAT` ou navegação de relacionamentos.

---

## PARTE 16 — PERGUNTAS QUE O PROFESSOR PODE FAZER

"O que é REST?" — REST é um estilo arquitetural para sistemas distribuídos. Uma API REST usa os métodos HTTP para operações, retorna dados em JSON, é stateless onde cada requisição é independente e carrega tudo que precisa, e usa URLs para identificar recursos.

"O que é Spring Boot?" — É um framework que simplifica a criação de aplicações Spring. Elimina configurações XML, já vem com servidor embutido, configura automaticamente os componentes detectados no classpath e usa convenção sobre configuração.

"Por que separar em Controller, Service, Repository e Model?" — Separação de responsabilidades. O Controller não deve saber como buscar no banco. O Repository não deve conhecer regras de negócio. Essa separação facilita manutenção, testes e evolução. Se mudar o banco, só o Repository muda. Se mudar uma regra, só o Service muda.

"O que é injeção de dependências?" — É um padrão onde um objeto não cria suas próprias dependências, ele as recebe de fora. No Spring, o framework cria e gerencia os objetos chamados de Beans e os injeta onde forem necessários. No projeto usamos injeção por construtor com `@RequiredArgsConstructor`.

"Por que usar DTO em vez de retornar a entidade?" — A entidade tem campos sensíveis como `senhaHash`, carrega anotações de persistência que não fazem sentido na camada HTTP, e o formato do banco pode ser diferente do ideal para a API. O DTO controla exatamente o contrato da API.

"O que é @Transactional?" — Garante que todas as operações do método ocorrem dentro de uma transação de banco. Se qualquer operação falhar, todas as anteriores são desfeitas automaticamente em um processo chamado rollback. Usado em métodos que escrevem no banco.

"O que é JPQL?" — É a linguagem de consulta do JPA que usa nomes de classes e campos Java em vez de tabelas e colunas SQL. O Hibernate traduz para o SQL específico do banco em uso, tornando o código independente do banco de dados.

"Como o @Valid funciona internamente?" — Antes de executar o método do Controller, o Spring chama o Bean Validation para validar o objeto anotado. Se houver erros, lança `MethodArgumentNotValidException` sem entrar no método. O `GlobalExceptionHandler` captura e retorna 400.

"O que é o @CreationTimestamp?" — Anotação do Hibernate que preenche o campo automaticamente com a data e hora exata da inserção no banco. Usado em `criadoEm` do Usuario e `adicionadoEm` do Favorito.

"Qual a diferença entre @Component, @Service e @Repository?" — Tecnicamente as três registram o Bean. Semanticamente: `@Service` indica lógica de negócio, `@Repository` indica acesso a dados e ativa tradução de exceções de persistência, e `@Component` é o genérico usado quando nenhum dos outros se aplica.

"O que é o MDC e para que serve o CorrelationIdFilter?" — MDC é um mapa de chave-valor associado à thread atual. O filtro gera um UUID por requisição e o coloca no MDC. Todos os logs daquela requisição incluem automaticamente o UUID, permitindo rastrear todos os logs de uma chamada específica mesmo em logs de requisições paralelas.

"O que é o @FeignClient?" — Anotação do OpenFeign que transforma uma interface Java em um client HTTP automático. O Feign gera a implementação, monta as URLs, faz as requisições e deserializa as respostas. Usado para chamar o ViaCEP sem escrever código de HTTP manualmente.

"Como as senhas são protegidas?" — BCrypt hasheia a senha com salt aleatório antes de salvar no banco. O hash é unidirecional, não tem como reverter para descobrir a senha original. Para verificar a senha em um login usaria `passwordEncoder.matches()`.

"Por que o AssinaturaService busca Usuario e Plano antes de salvar?" — Para garantir integridade referencial com uma mensagem de erro clara. Se o usuário ou plano não existir, lança 404 com mensagem legível antes de tentar persistir. Sem essa verificação o JPA lançaria uma exceção de constraint de chave estrangeira com mensagem técnica menos informativa.

"O que é paginação e como funciona?" — Paginação retorna dados em pedaços chamados páginas. O `Pageable` carrega o número da página, o tamanho e a ordenação. O Repository retorna `Page<T>` com os dados e metadados como total de elementos e total de páginas. O `@PageableDefault(size=10)` define o tamanho padrão da página.

"O que é ddl-auto=update?" — Configura o Hibernate para comparar as entidades com o estado atual do banco e aplicar as diferenças automaticamente ao iniciar. Cria tabelas novas e adiciona colunas, mas nunca apaga dados nem remove colunas existentes.

"Por que dto/externo e service/externo existem?" — Para separar o que é do projeto do que vem de fora. DTOs externos modelam respostas de APIs de terceiros que você não controla. Services externos encapsulam a comunicação com APIs externas sem misturar essa lógica com as regras de negócio do IESPFLIX. Se uma API externa mudar, o impacto fica isolado nessas subpastas.

---

## PARTE 17 — DIVISÃO DO TRABALHO POR INTEGRANTE

### José Wilson Alves de Souza — Líder Técnico

Responsável pela estrutura base que viabilizou o trabalho dos outros quatro integrantes. Configurou o `AppConfig` com os Beans de BCrypt e RestTemplate, o `OpenApiConfig` para o Swagger, o `GlobalExceptionHandler` para tratamento centralizado de todos os erros com cinco tipos de handler, e o `CorrelationIdFilter` para rastreabilidade de logs por UUID. Criou a validação customizada `@CpfCnpj` com o algoritmo completo de verificação de dígitos do CPF. Implementou o módulo de Usuários completo com entidade, DTOs, mapper, repository com JPQL de filtro por perfil, service com BCrypt e verificação de email duplicado, e controller com CRUD completo e paginação. Implementou também o módulo de MetodoPagamento completo com DTOs, query JPQL no repository, service com logs e tratamento de erros, e controller com três endpoints. Manteve o AGENTES.md e a documentação do projeto.

### Ana Julya Rodrigues Dionizio — Conteúdos

Responsável pelo módulo mais rico em consultas do projeto. Implementou a entidade `Conteudo` com campo TEXT para sinopse. Criou quatro queries JPQL personalizadas: filtro por gênero case-insensitive com `LOWER()`, busca simultânea em título e sinopse com `LIKE` e `CONCAT`, top N por relevância com limite dinâmico via `Pageable.ofSize()`, e filtro por ano de lançamento. Criou os DTOs com validações `@Min`, `@Max`, `@Positive`, `@DecimalMin` e `@DecimalMax`, o `ConteudoMapper`, o `ConteudoService` com paginação e os nove endpoints do Controller incluindo os quatro filtros de busca avançada.

### Alex Júlio de Brito — Filmes

Responsável pelo módulo de Filmes. Implementou a entidade `Filme` com anotações de coluna detalhadas usando `nullable`, `length`, `columnDefinition` e `name`. Criou o `FilmeRepository` com JPQL de ordenação por título e query de busca por gênero e título combinados, além do método derivado por nome `findAllByOrderByTituloAsc()`. Implementou o `FilmeService` com logs extensivos em todos os métodos usando os quatro níveis: `info`, `debug`, `warn` e `error`. Criou o `FilmeController` com os seis endpoints incluindo a rota de listagem ordenada separada da listagem padrão.

### Everton Fernandes S. Da Silva — Assinaturas e Planos

Responsável pelos módulos centrais do negócio de streaming. Implementou as entidades `Plano` e `Assinatura` com os relacionamentos `@ManyToOne` para Usuario e Plano na assinatura. Criou o fluxo completo de negócio: criar plano, criar assinatura vinculando usuário ao plano com datas de vigência, e cancelar assinatura alterando o status sem deletar o registro, preservando o histórico. O `AssinaturaService` valida a existência do usuário e do plano antes de persistir. Criou os cinco endpoints combinados entre os dois módulos.

### Silvano Bernardino da S. Filho — Favoritos e Integrações Externas

Responsável pelo módulo de Favoritos e pelas duas integrações com APIs públicas externas. Implementou a entidade `Favorito` com `@CreationTimestamp` para registro automático do momento de adição. Criou a query JPQL ordenada por `adicionadoEm` descendente para listar os favoritos mais recentes primeiro. Implementou o `FavoritoMapper` que enriquece o response com nome do usuário e título do conteúdo navegando os relacionamentos. Criou o `ViaCepClient` com `@FeignClient` para integração declarativa com o ViaCEP e o `EnderecoController` com validação de CEP de oito dígitos. Implementou o `BrasilApiService` com `RestTemplate` em `service/externo/` para integração com a BrasilAPI e o `FeriadoController` com o `FeriadoResponse` como DTO externo em `dto/externo/`.

---

*IESPFLIX · UNIESP Sistemas para Internet 2025/P3 · Tecnologias para Backend — Prof. Rodrigo Fujioka*
*Gerado em 2026-05-25 com base no código real do projeto*
