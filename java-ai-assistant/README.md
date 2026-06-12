# Java AI Assistant

Java AI Assistant is a personal learning and life assistant project. The current codebase is a single-module Maven project.

## Requirements

- Java 17
- Maven

## Unit Tests

Run unit tests from this directory:

```bash
mvn clean test
```

The default unit test lifecycle does not access the real DeepSeek service, network resources, or an API key.

Recent validation record from v28: `mvn clean test` passed 944 tests with 0 failures.

## Integration Tests

Run integration tests from this directory:

```bash
mvn -Pintegration verify
```

The current repository does not contain `*IT.java` classes. This command is the optional integration-test entry point; it does not mean that a real DeepSeek connectivity test already exists. Future tests that perform real AI calls should use the `integration` profile, follow the `*IT.java` naming convention, and require network access plus `DEEPSEEK_API_KEY`.

## Test Documentation

- [Test plan](docs/test-plan.md)
- [White-box test cases](docs/test-cases.md)
- [Defect and regression record](docs/defect-regression.md)
- [Coverage evidence notes](docs/coverage/README.md)
- [Environment](docs/environment.md)

## Coverage

Generate JaCoCo coverage output from this directory:

```bash
mvn clean verify
mvn jacoco:report
```

The HTML report is generated at `target/site/jacoco/index.html`. Generated coverage files under `target/site/jacoco/` are not committed.

## DeepSeek Configuration

Future AI client code will use these environment variables:

| Variable | Purpose |
|----------|---------|
| `DEEPSEEK_API_KEY` | DeepSeek API key. |
| `DEEPSEEK_BASE_URL` | DeepSeek base URL. |
| `DEEPSEEK_MODEL` | Model name. |
| `DEEPSEEK_TIMEOUT_SECONDS` | Request timeout in seconds. |

Default AI configuration:

| Setting | Value |
|---------|-------|
| Base URL | `https://api.deepseek.com` |
| Path | `/chat/completions` |
| Model | `deepseek-v4-flash` |

API keys must not be committed to source code, tests, or documentation examples. Without an API key, ordinary unit tests still run because this module does not read AI configuration during unit tests.
