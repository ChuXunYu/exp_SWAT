# Environment

## Development Environment

- Java 17 LTS
- Maven
- JUnit Jupiter 5.14.4
- Mockito 5.18.0
- Maven Surefire Plugin 3.5.6
- Maven Failsafe Plugin 3.5.6
- JaCoCo Maven Plugin 0.8.13

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

Generate a JaCoCo report after tests:

```bash
mvn jacoco:report
```

JaCoCo reports are generated under `target/site/jacoco/` during the `verify` phase or by running `mvn jacoco:report`.

## DeepSeek Environment Variables

| Variable | Purpose |
|----------|---------|
| `DEEPSEEK_API_KEY` | API key used by future DeepSeek client code. |
| `DEEPSEEK_BASE_URL` | Override for the DeepSeek base URL. |
| `DEEPSEEK_MODEL` | Override for the model name. |
| `DEEPSEEK_TIMEOUT_SECONDS` | Request timeout in seconds. |

Do not place real API keys in source code, tests, or documentation.

## Test Isolation

Ordinary unit tests use fixed time, in-memory repositories, and mock or fake AI dependencies. They do not access the real DeepSeek service, network resources, a real API key, user files, or the real current time. Future tests that make real external calls belong in the `integration` profile and should use the `*IT.java` naming convention.

## Test Deliverables

- [Test plan](test-plan.md)
- [White-box test cases](test-cases.md)
- [Defect and regression record](defect-regression.md)
- [Coverage evidence notes](coverage/README.md)

## Current Test Baseline

The QA risk-fix v4 validation report records `mvn clean verify` passing 989 tests with 0 failures, 0 errors, and 0 skipped tests.

The current `src/test/java` tree does not contain `*IT.java` integration test classes. Real DeepSeek connectivity is not part of the default unit-test baseline.
