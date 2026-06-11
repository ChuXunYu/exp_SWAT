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

## Integration Tests

Run integration tests from this directory:

```bash
mvn -Pintegration verify
```

Future tests that perform real AI calls should use the `integration` profile and follow the `*IT.java` naming convention.

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
