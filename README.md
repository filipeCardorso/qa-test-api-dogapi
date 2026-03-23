# QA Test - API Automation (RestAssured + Java)

Automação de testes para a Dog API (https://dog.ceo/dog-api/).

## Stack

| Tecnologia | Versão |
|-----------|--------|
| Java | 17 |
| RestAssured | 5.5.0 |
| JUnit 5 | 5.11.4 |
| Gradle | 8.x |
| Allure Report | 2.29.1 |
| AssertJ | 3.27.3 |
| Jackson | 2.18.2 |

## Arquitetura

O projeto utiliza os seguintes Design Patterns:

- **Builder Pattern** — RequestSpecification e ResponseSpecification reutilizáveis
- **POJO/DTO** — deserialização type-safe das respostas da API
- **Singleton** — configuração centralizada
- **Template Method** — setup/teardown padronizado via BaseApiTest
- **Data-Driven** — testes parametrizados com JUnit 5

Padrão **AAA (Arrange-Act-Assert)** em todos os testes.

## Endpoints Testados

| Endpoint | Cenários |
|----------|---------|
| `GET /breeds/list/all` | Status 200, lista não vazia, raças conhecidas, status success |
| `GET /breed/{breed}/images` | Imagens para raça válida, URLs válidas, erro para raça inválida |
| `GET /breeds/image/random` | Status 200, URL de imagem válida, status success |

## Pré-requisitos

- Java 17+
- Git

## Executar os Testes

```bash
# Clonar o repositório
git clone https://github.com/SEU_USUARIO/qa-test-api-dogapi.git
cd qa-test-api-dogapi

# Executar todos os testes
./gradlew test

# Executar testes de um endpoint específico
./gradlew test --tests "com.qatest.api.tests.BreedListTest"

# Gerar relatório Allure
./gradlew allureReport

# Abrir relatório
./gradlew allureServe
```

## Estrutura do Projeto

```
src/
├── main/java/com/qatest/api/
│   ├── config/     # Configuração (Singleton)
│   ├── models/     # POJOs de resposta
│   ├── specs/      # Request/Response Specifications (Builder)
│   └── utils/      # Dados auxiliares
└── test/java/com/qatest/api/
    ├── base/       # BaseApiTest
    └── tests/      # Cenários de teste por endpoint
```

## CI/CD

O projeto possui pipeline GitHub Actions que:
1. Configura JDK 17
2. Executa todos os testes
3. Gera e salva o relatório Allure como artifact
