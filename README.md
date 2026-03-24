# QA Test - API Automation (RestAssured + Java)

Automacao de testes para a [Dog API](https://dog.ceo/dog-api/), uma API REST publica que fornece dados e imagens de racas de caes. Desenvolvido como parte de um teste tecnico para QA.

## Sumario

- [Sobre o Projeto](#sobre-o-projeto)
- [Analise da API](#analise-da-api)
- [Cenarios de Teste](#cenarios-de-teste)
- [Arquitetura e Design Patterns](#arquitetura-e-design-patterns)
- [Stack Tecnologica](#stack-tecnologica)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pre-requisitos](#pre-requisitos)
- [Configuracao e Execucao](#configuracao-e-execucao)
- [CI/CD](#cicd)
- [Relatorios](#relatorios)
- [Decisoes Tecnicas](#decisoes-tecnicas)

---

## Sobre o Projeto

Este projeto valida a integracao com a Dog API, garantindo que os endpoints respondem corretamente, que os dados retornados estao no formato esperado e que a API se comporta adequadamente tanto em cenarios positivos quanto negativos.

A suite cobre os 3 endpoints solicitados com **13 cenarios de teste**, incluindo testes parametrizados que validam multiplas racas na mesma execucao.

## Analise da API

**Base URL:** `https://dog.ceo/api`

### Endpoints e Contratos

#### GET /breeds/list/all
Retorna todas as racas com suas sub-racas.

```json
{
  "message": {
    "bulldog": ["boston", "english", "french"],
    "labrador": [],
    "poodle": ["medium", "miniature", "standard", "toy"]
  },
  "status": "success"
}
```

- `message`: Map onde a key e a raca e o value e uma lista de sub-racas
- `status`: sempre "success" para respostas 200

#### GET /breed/{breed}/images
Retorna URLs de imagens para uma raca especifica.

```json
{
  "message": [
    "https://images.dog.ceo/breeds/labrador/n02099712_001.jpg",
    "https://images.dog.ceo/breeds/labrador/n02099712_002.jpg"
  ],
  "status": "success"
}
```

- `message`: Lista de URLs de imagens (formato .jpg/.png)
- Para racas inexistentes: retorna status 404 com `"status": "error"`

#### GET /breeds/image/random
Retorna uma imagem aleatoria de qualquer raca.

```json
{
  "message": "https://images.dog.ceo/breeds/terrier-lakeland/n02095570_4199.jpg",
  "status": "success"
}
```

- `message`: String com URL de uma unica imagem
- Cada chamada retorna uma imagem diferente (aleatorio)

## Cenarios de Teste

### GET /breeds/list/all (4 cenarios + 4 parametrizados)

#### CT-001: Retorna status 200 com status "success"
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Blocker |
| **Validacao** | Status HTTP 200, Content-Type JSON, campo `status` = "success" |
| **Por que e relevante** | Smoke test — se este endpoint nao responde, toda a aplicacao esta indisponivel |

#### CT-002: Retorna lista nao vazia de racas
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Validacao** | O campo `message` contem pelo menos 1 raca |
| **Por que e relevante** | Valida que a API retorna dados — uma lista vazia indica problema no backend |

#### CT-003: Contem racas conhecidas (parametrizado)
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Normal |
| **Dados** | bulldog, labrador, poodle, beagle (via `@ValueSource`) |
| **Validacao** | Cada raca fornecida existe como key no Map `message` |
| **Por que e relevante** | Valida integridade dos dados — racas populares devem sempre estar presentes na lista |

#### CT-004: Campo status contem "success"
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Validacao** | Deserializa para POJO e verifica `getStatus().equals("success")` |
| **Por que e relevante** | Valida o contrato da API via deserializacao type-safe (POJO), nao apenas via JSON path |

### GET /breed/{breed}/images (3 cenarios)

#### CT-005: Retorna imagens para raca valida
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Dados** | Raca: "labrador" |
| **Validacao** | Status 200, lista `message` nao vazia |
| **Por que e relevante** | Happy path — garante que racas validas retornam conteudo |

#### CT-006: URLs de imagem sao validas
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Normal |
| **Dados** | Raca: "labrador" |
| **Validacao** | Todas as URLs comecam com `https://` e terminam em `.jpg`, `.jpeg`, `.png` ou `.gif` |
| **Por que e relevante** | Valida formato dos dados — uma URL invalida quebraria a renderizacao de imagens na aplicacao cliente |

#### CT-007: Retorna erro para raca inexistente
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Dados** | Raca: "invalidbreed123" |
| **Validacao** | Status HTTP 404, Content-Type JSON, campo `status` = "error" |
| **Por que e relevante** | Cenario negativo — a API deve retornar erro adequado (404) e nao 200 com lista vazia |

### GET /breeds/image/random (3 cenarios)

#### CT-008: Retorna status 200 com status "success"
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Blocker |
| **Validacao** | Status HTTP 200, Content-Type JSON, campo `status` = "success" |
| **Por que e relevante** | Smoke test do endpoint de imagem aleatoria |

#### CT-009: Retorna URL de imagem valida
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Validacao** | `message` comeca com `https://` e contem extensao de imagem |
| **Por que e relevante** | Garante que a URL retornada e utilizavel pela aplicacao para renderizar a imagem |

#### CT-010: Campo status contem "success"
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Validacao** | Deserializa para POJO `RandomImageResponse` e verifica campo status |
| **Por que e relevante** | Consistencia — mesmo endpoint, mesma validacao de contrato via POJO |

## Arquitetura e Design Patterns

### Builder Pattern — RequestSpecs e ResponseSpecs

As especificacoes de request e response sao construidas via `RequestSpecBuilder` e `ResponseSpecBuilder` do RestAssured, permitindo reutilizacao e composicao.

```java
// Request — usado por todos os testes
public static RequestSpecification defaultSpec() {
    return new RequestSpecBuilder()
            .setBaseUri(ApiConfig.getInstance().getBaseUrl())
            .setContentType(ContentType.JSON)
            .addFilter(new AllureRestAssured())  // logging para Allure
            .build();
}

// Response — reutilizado para qualquer endpoint que retorna sucesso
public static ResponseSpecification successResponse() {
    return new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON)
            .expectBody("status", equalTo("success"))
            .build();
}
```

**Por que Builder:** evita duplicacao de configuracao em cada teste. Mudancas na URL base ou headers afetam todos os testes de uma vez.

### POJO/DTO — Deserializacao Type-Safe

Cada formato de resposta da API tem um POJO correspondente com anotacoes Jackson:

```
BreedListResponse   → message: Map<String, List<String>>, status: String
BreedImagesResponse → message: List<String>, status: String
RandomImageResponse → message: String, status: String
```

**Por que POJOs:** em vez de validar apenas via JSON path (`body("message.size()", greaterThan(0))`), a deserializacao para POJOs garante que:
- O formato do JSON e exatamente o esperado (falha se houver campos inesperados)
- As assertions sao type-safe (compilador detecta erros)
- O codigo e mais legivel e reflete o contrato da API

### Singleton — ApiConfig

Carrega `application.yml` uma unica vez e disponibiliza `baseUrl` e timeouts. Centraliza configuracao para que trocar de ambiente (staging, producao) seja apenas mudar o YAML.

### Template Method — BaseApiTest

Classe abstrata que configura o `RequestSpecification` no `@BeforeEach`. Todos os testes herdam e recebem o `request` pronto para uso, eliminando boilerplate.

```java
public abstract class BaseApiTest {
    protected RequestSpecification request;

    @BeforeEach
    void setUp() {
        request = given().spec(RequestSpecs.defaultSpec());
    }
}
```

### Data-Driven — @ParameterizedTest

O cenario de racas conhecidas usa `@ParameterizedTest` com `@ValueSource` para testar multiplas racas na mesma logica:

```java
@ParameterizedTest(name = "Should contain breed: {0}")
@ValueSource(strings = {"bulldog", "labrador", "poodle", "beagle"})
void shouldContainKnownBreed(String breed) { ... }
```

**Por que:** evita duplicacao de 4 testes identicos, gera relatorio com cada raca como caso separado.

### Padrao AAA (Arrange-Act-Assert)

Todos os testes seguem a estrutura:

```java
@Test
void shouldReturnImagesForValidBreed() {
    // Arrange
    String breed = DataHelper.validBreed();

    // Act
    BreedImagesResponse response = request
        .when()
            .get("/breed/{breed}/images", breed)
        .then()
            .spec(ResponseSpecs.successResponse())
            .extract().as(BreedImagesResponse.class);

    // Assert
    assertThat(response.getMessage()).isNotEmpty();
}
```

### Allure Annotations

Cada teste e anotado com metadados que enriquecem o relatorio:

- `@Feature("Breed List")` — agrupa testes por funcionalidade
- `@DisplayName("...")` — nome legivel no relatorio
- `@Description("...")` — descricao detalhada do que o teste valida
- `@Severity(...)` — Blocker, Critical ou Normal

O filtro `AllureRestAssured` no RequestSpec faz log automatico de cada request/response HTTP no Allure.

## Stack Tecnologica

| Tecnologia | Versao | Justificativa |
|-----------|--------|---------------|
| Java | 17 | LTS atual, suporte a switch expressions e text blocks |
| RestAssured | 5.5.0 | DSL fluente para testes de API, padrao de mercado em Java |
| JUnit 5 | 5.11.4 | Testes parametrizados, `@DisplayName`, extensoes modernas |
| Gradle | 8.12 | Build mais rapido e conciso que Maven |
| Allure Report | 2.29.1 | Relatorios visuais com request/response HTTP logado |
| AssertJ | 3.27.3 | Assertions fluentes — `assertThat(list).allSatisfy(...)` |
| Jackson | 2.18.2 | Deserializacao JSON para POJOs com anotacoes |

## Estrutura do Projeto

```
qa-test-api-dogapi/
├── .github/workflows/test.yml              # Pipeline CI/CD
├── build.gradle                            # Dependencias e configuracao
├── src/
│   ├── main/java/com/qatest/api/
│   │   ├── config/
│   │   │   └── ApiConfig.java              # Singleton — carrega application.yml
│   │   ├── models/
│   │   │   ├── BreedListResponse.java      # POJO — GET /breeds/list/all
│   │   │   ├── BreedImagesResponse.java    # POJO — GET /breed/{breed}/images
│   │   │   └── RandomImageResponse.java    # POJO — GET /breeds/image/random
│   │   ├── specs/
│   │   │   ├── RequestSpecs.java           # Builder — configuracao de request
│   │   │   └── ResponseSpecs.java          # Builder — validacao de response
│   │   └── utils/
│   │       └── DataHelper.java             # Dados de teste centralizados
│   ├── main/resources/
│   │   └── application.yml                 # Configuracoes (URL, timeouts)
│   └── test/java/com/qatest/api/
│       ├── base/
│       │   └── BaseApiTest.java            # Template Method — setup padrao
│       └── tests/
│           ├── BreedListTest.java          # 7 cenarios (4 + 4 parametrizados)
│           ├── BreedImagesTest.java        # 3 cenarios
│           └── RandomImageTest.java        # 3 cenarios
└── README.md
```

## Pre-requisitos

- **Java 17+** — [Download](https://adoptium.net/)
- **Git** — [Download](https://git-scm.com/)

> Nao requer browser, banco de dados ou Docker — a API e publica e os testes fazem requests HTTP diretamente.

## Configuracao e Execucao

```bash
# 1. Clonar o repositorio
git clone https://github.com/filipeCardorso/qa-test-api-dogapi.git
cd qa-test-api-dogapi

# 2. Executar todos os testes
./gradlew test

# 3. Executar testes de um endpoint especifico
./gradlew test --tests "com.qatest.api.tests.BreedListTest"
./gradlew test --tests "com.qatest.api.tests.BreedImagesTest"
./gradlew test --tests "com.qatest.api.tests.RandomImageTest"

# 4. Gerar relatorio Allure
./gradlew allureReport

# 5. Abrir relatorio no browser
./gradlew allureServe
```

### Configuracao da API

Editar `src/main/resources/application.yml`:

```yaml
api:
  base-url: https://dog.ceo/api
  timeout:
    connection: 5000    # ms
    response: 10000     # ms
```

## CI/CD

O projeto possui pipeline **GitHub Actions** que executa automaticamente a cada push ou pull request:

1. **Setup:** Configura JDK 17 e Gradle (com cache de dependencias)
2. **Testes:** Executa os 13 cenarios
3. **Relatorio de Resultados:** Publica graficos com contagem de testes (passed/failed/skipped) diretamente no Summary da pipeline
4. **Allure Report:** Gera e publica em GitHub Pages com request/response HTTP de cada teste
5. **Artifacts:** Salva o relatorio para download (retencao de 30 dias)

### Relatorio Online

Apos cada execucao, o Allure Report fica disponivel em:
**https://filipecardorso.github.io/qa-test-api-dogapi/allure-report**

## Relatorios

| Tipo | Onde Encontrar | O Que Mostra |
|------|---------------|-------------|
| **JUnit (terminal)** | Output do `./gradlew test` | Resultado de cada teste (PASSED/FAILED) |
| **Pipeline Summary** | GitHub Actions > run > Summary | Graficos com total de testes, taxa de sucesso, duracao |
| **Allure Report** | GitHub Pages ou `./gradlew allureServe` | Cenarios por feature, severidade, request/response HTTP, historico |

## Decisoes Tecnicas

| Decisao | Alternativa | Justificativa |
|---------|------------|---------------|
| RestAssured sobre HttpClient | Java HttpClient nativo | DSL fluente, integracao nativa com Allure, validacao inline |
| POJOs sobre JSON path puro | Validar apenas via `body("key", matcher)` | Type safety, reflete contrato da API, assertions mais claras |
| AssertJ sobre Hamcrest | Hamcrest | `allSatisfy()`, `containsPattern()`, mensagens de erro superiores |
| `@ParameterizedTest` sobre testes separados | 4 metodos identicos | DRY, cada raca aparece como caso separado no relatorio |
| Gradle sobre Maven | Maven | Menos boilerplate, DSL legivel, builds mais rapidos |
| YAML sobre properties | .properties | Suporta hierarquia, mais legivel para timeout configs |
| Allure filter no RequestSpec | Log manual | Logging automatico de request/response sem codigo extra |
