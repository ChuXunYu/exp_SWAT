# Environment

## Development Environment

- Java 17 LTS
- Maven
- JUnit Jupiter 5.14.4
- Mockito 5.18.0

## Build And Test

Run unit tests:

```bash
mvn clean test
```

Run the default verification lifecycle:

```bash
mvn clean verify
```

Run integration tests:

```bash
mvn -Pintegration verify
```

JaCoCo reports are generated under `target/site/jacoco/` during the `verify` phase.

## DeepSeek Environment Variables

| Variable | Purpose |
|----------|---------|
| `DEEPSEEK_API_KEY` | API key used by future DeepSeek client code. |
| `DEEPSEEK_BASE_URL` | Override for the DeepSeek base URL. |
| `DEEPSEEK_MODEL` | Override for the model name. |
| `DEEPSEEK_TIMEOUT_SECONDS` | Request timeout in seconds. |

Do not place real API keys in source code, tests, or documentation.

## Test Isolation

Ordinary unit tests do not read real environment variables, access the network, or depend on a DeepSeek API key. Future tests that make real external calls belong in the `integration` profile and should use the `*IT.java` naming convention.
