# QA Test - API Automation (RestAssured + Java)

Automação de testes para a [Dog API](https://dog.ceo/dog-api/), uma API REST pública que fornece dados e imagens de raças de cães. Desenvolvido como parte de um teste técnico para QA.

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Análise da API](#análise-da-api)
- [Cenários de Teste](#cenários-de-teste)
- [Arquitetura e Design Patterns](#arquitetura-e-design-patterns)
- [Stack Tecnológica](#stack-tecnológica)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configuração e Execução](#configuração-e-execução)
- [CI/CD](#cicd)
- [Relatórios](#relatórios)
- [Decisões Técnicas](#decisões-técnicas)

---

## Sobre o Projeto

Este projeto valida a integração com a Dog API, garantindo que os endpoints respondem corretamente, que os dados retornados estão no formato esperado e que a API se comporta adequadamente tanto em cenários positivos quanto negativos.

A suíte cobre os 3 endpoints solicitados com **13 cenários de teste**, incluindo testes parametrizados que validam múltiplas raças na mesma execução.

## Análise da API

**Base URL:** `https://dog.ceo/api`

### Endpoints e Contratos

#### GET /breeds/list/all
Retorna todas as raças com suas sub-raças.

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

- `message`: Map onde a key é a raça e o value é uma lista de sub-raças
- `status`: sempre "success" para respostas 200

#### GET /breed/{breed}/images
Retorna URLs de imagens para uma raça específica.

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
- Para raças inexistentes: retorna status 404 com `"status": "error"`

#### GET /breeds/image/random
Retorna uma imagem aleatória de qualquer raça.

```json
{
  "message": "https://images.dog.ceo/breeds/terrier-lakeland/n02095570_4199.jpg",
  "status": "success"
}
```

- `message`: String com URL de uma única imagem
- Cada chamada retorna uma imagem diferente (aleatório)

## Cenários de Teste

### GET /breeds/list/all (4 cenários + 4 parametrizados)

#### CT-001: Retorna status 200 com status "success"
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Blocker |
| **Validação** | Status HTTP 200, Content-Type JSON, campo `status` = "success" |
| **Por que é relevante** | Smoke test — se este endpoint não responde, toda a aplicação está indisponível |

#### CT-002: Retorna lista não vazia de raças
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Validação** | O campo `message` contém pelo menos 1 raça |
| **Por que é relevante** | Valida que a API retorna dados — uma lista vazia indica problema no backend |

#### CT-003: Contém raças conhecidas (parametrizado)
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Normal |
| **Dados** | bulldog, labrador, poodle, beagle (via `@ValueSource`) |
| **Validação** | Cada raça fornecida existe como key no Map `message` |
| **Por que é relevante** | Valida integridade dos dados — raças populares devem sempre estar presentes na lista |

#### CT-004: Campo status contém "success"
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Validação** | Deserializa para POJO e verifica `getStatus().equals("success")` |
| **Por que é relevante** | Valida o contrato da API via deserialização type-safe (POJO), não apenas via JSON path |

### GET /breed/{breed}/images (3 cenários)

#### CT-005: Retorna imagens para raça válida
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Dados** | Raça: "labrador" |
| **Validação** | Status 200, lista `message` não vazia |
| **Por que é relevante** | Happy path — garante que raças válidas retornam conteúdo |

#### CT-006: URLs de imagem são válidas
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Normal |
| **Dados** | Raça: "labrador" |
| **Validação** | Todas as URLs começam com `https://` e terminam em `.jpg`, `.jpeg`, `.png` ou `.gif` |
| **Por que é relevante** | Valida formato dos dados — uma URL inválida quebraria a renderização de imagens na aplicação cliente |

#### CT-007: Retorna erro para raça inexistente
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Dados** | Raça: "invalidbreed123" |
| **Validação** | Status HTTP 404, Content-Type JSON, campo `status` = "error" |
| **Por que é relevante** | Cenário negativo — a API deve retornar erro adequado (404) e não 200 com lista vazia |

### GET /breeds/image/random (3 cenários)

#### CT-008: Retorna status 200 com status "success"
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Blocker |
| **Validação** | Status HTTP 200, Content-Type JSON, campo `status` = "success" |
| **Por que é relevante** | Smoke test do endpoint de imagem aleatória |

#### CT-009: Retorna URL de imagem válida
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Validação** | `message` começa com `https://` e contém extensão de imagem |
| **Por que é relevante** | Garante que a URL retornada é utilizável pela aplicação para renderizar a imagem |

#### CT-010: Campo status contém "success"
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Validação** | Deserializa para POJO `RandomImageResponse` e verifica campo status |
| **Por que é relevante** | Consistência — mesmo endpoint, mesma validação de contrato via POJO |

## Arquitetura e Design Patterns

### Builder Pattern — RequestSpecs e ResponseSpecs

As especificações de request e response são construídas via `RequestSpecBuilder` e `ResponseSpecBuilder` do RestAssured, permitindo reutilização e composição.

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

**Por que Builder:** evita duplicação de configuração em cada teste. Mudanças na URL base ou headers afetam todos os testes de uma vez.

### POJO/DTO — Deserialização Type-Safe

Cada formato de resposta da API tem um POJO correspondente com anotações Jackson:

```
BreedListResponse   → message: Map<String, List<String>>, status: String
BreedImagesResponse → message: List<String>, status: String
RandomImageResponse → message: String, status: String
```

**Por que POJOs:** em vez de validar apenas via JSON path (`body("message.size()", greaterThan(0))`), a deserialização para POJOs garante que:
- O formato do JSON é exatamente o esperado (falha se houver campos inesperados)
- As assertions são type-safe (compilador detecta erros)
- O código é mais legível e reflete o contrato da API

### Singleton — ApiConfig

Carrega `application.yml` uma única vez e disponibiliza `baseUrl` e timeouts. Centraliza configuração para que trocar de ambiente (staging, produção) seja apenas mudar o YAML.

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

O cenário de raças conhecidas usa `@ParameterizedTest` com `@ValueSource` para testar múltiplas raças na mesma lógica:

```java
@ParameterizedTest(name = "Should contain breed: {0}")
@ValueSource(strings = {"bulldog", "labrador", "poodle", "beagle"})
void shouldContainKnownBreed(String breed) { ... }
```

**Por que:** evita duplicação de 4 testes idênticos, gera relatório com cada raça como caso separado.

### Padrão AAA (Arrange-Act-Assert)

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

Cada teste é anotado com metadados que enriquecem o relatório:

- `@Feature("Breed List")` — agrupa testes por funcionalidade
- `@DisplayName("...")` — nome legível no relatório
- `@Description("...")` — descrição detalhada do que o teste valida
- `@Severity(...)` — Blocker, Critical ou Normal

O filtro `AllureRestAssured` no RequestSpec faz log automático de cada request/response HTTP no Allure.

## Stack Tecnológica

| Tecnologia | Versão | Justificativa |
|-----------|--------|---------------|
| Java | 17 | LTS atual, suporte a switch expressions e text blocks |
| RestAssured | 5.5.0 | DSL fluente para testes de API, padrão de mercado em Java |
| JUnit 5 | 5.11.4 | Testes parametrizados, `@DisplayName`, extensões modernas |
| Gradle | 8.12 | Build mais rápido e conciso que Maven |
| Allure Report | 2.29.1 | Relatórios visuais com request/response HTTP logado |
| AssertJ | 3.27.3 | Assertions fluentes — `assertThat(list).allSatisfy(...)` |
| Jackson | 2.18.2 | Deserialização JSON para POJOs com anotações |

## Estrutura do Projeto

```
qa-test-api-dogapi/
├── .github/workflows/test.yml              # Pipeline CI/CD
├── build.gradle                            # Dependências e configuração
├── src/
│   ├── main/java/com/qatest/api/
│   │   ├── config/
│   │   │   └── ApiConfig.java              # Singleton — carrega application.yml
│   │   ├── models/
│   │   │   ├── BreedListResponse.java      # POJO — GET /breeds/list/all
│   │   │   ├── BreedImagesResponse.java    # POJO — GET /breed/{breed}/images
│   │   │   └── RandomImageResponse.java    # POJO — GET /breeds/image/random
│   │   ├── specs/
│   │   │   ├── RequestSpecs.java           # Builder — configuração de request
│   │   │   └── ResponseSpecs.java          # Builder — validação de response
│   │   └── utils/
│   │       └── DataHelper.java             # Dados de teste centralizados
│   ├── main/resources/
│   │   └── application.yml                 # Configurações (URL, timeouts)
│   └── test/java/com/qatest/api/
│       ├── base/
│       │   └── BaseApiTest.java            # Template Method — setup padrão
│       └── tests/
│           ├── BreedListTest.java          # 7 cenários (4 + 4 parametrizados)
│           ├── BreedImagesTest.java        # 3 cenários
│           └── RandomImageTest.java        # 3 cenários
└── README.md
```

## Pré-requisitos

- **Java 17+** — [Download](https://adoptium.net/)
- **Git** — [Download](https://git-scm.com/)

> Não requer browser, banco de dados ou Docker — a API é pública e os testes fazem requests HTTP diretamente.

## Configuração e Execução

```bash
# 1. Clonar o repositório
git clone https://github.com/filipeCardorso/qa-test-api-dogapi.git
cd qa-test-api-dogapi

# 2. Executar todos os testes
./gradlew test

# 3. Executar testes de um endpoint específico
./gradlew test --tests "com.qatest.api.tests.BreedListTest"
./gradlew test --tests "com.qatest.api.tests.BreedImagesTest"
./gradlew test --tests "com.qatest.api.tests.RandomImageTest"

# 4. Gerar relatório Allure
./gradlew allureReport

# 5. Abrir relatório no browser
./gradlew allureServe
```

### Configuração da API

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

1. **Setup:** Configura JDK 17 e Gradle (com cache de dependências)
2. **Testes:** Executa os 13 cenários
3. **Relatório de Resultados:** Publica gráficos com contagem de testes (passed/failed/skipped) diretamente no Summary da pipeline
4. **Allure Report:** Gera e publica em GitHub Pages com request/response HTTP de cada teste
5. **Artifacts:** Salva o relatório para download (retenção de 30 dias)

## Relatórios

| Tipo | Onde Encontrar | O Que Mostra |
|------|---------------|-------------|
| **JUnit (terminal)** | Output do `./gradlew test` | Resultado de cada teste (PASSED/FAILED) |
| **Pipeline Summary** | GitHub Actions > run > Summary | Gráficos com total de testes, taxa de sucesso, duração |
| **Allure Report** | GitHub Pages ou `./gradlew allureServe` | Cenários por feature, severidade, request/response HTTP, histórico |

## Decisões Técnicas

| Decisão | Alternativa | Justificativa |
|---------|------------|---------------|
| RestAssured sobre HttpClient | Java HttpClient nativo | DSL fluente, integração nativa com Allure, validação inline |
| POJOs sobre JSON path puro | Validar apenas via `body("key", matcher)` | Type safety, reflete contrato da API, assertions mais claras |
| AssertJ sobre Hamcrest | Hamcrest | `allSatisfy()`, `containsPattern()`, mensagens de erro superiores |
| `@ParameterizedTest` sobre testes separados | 4 métodos idênticos | DRY, cada raça aparece como caso separado no relatório |
| Gradle sobre Maven | Maven | Menos boilerplate, DSL legível, builds mais rápidos |
| YAML sobre properties | .properties | Suporta hierarquia, mais legível para timeout configs |
| Allure filter no RequestSpec | Log manual | Logging automático de request/response sem código extra |
