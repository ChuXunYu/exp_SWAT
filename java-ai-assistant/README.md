# Java AI Assistant

## Project Overview

Java AI Assistant is a personal learning and life assistant console application. The project is a single-module Maven Java 17 application, and all commands in this README are intended to be run from the `java-ai-assistant/` directory.

The current application stores business data through in-memory services and repositories. That makes it suitable for local demos, development, and tests, but data is not persisted after the process exits. The runtime entry point is `assistant.app.Main`, which loads demo data by default before starting the console menu.

## Features

- Summary: shows today's tasks, today's schedules, this week's study plans, this month's income, expense, and balance, plus note and tag statistics.
- Tasks: list, add, view, filter, update, complete, reopen, and delete tasks.
- Schedules: list, add, view, filter, update, and delete schedule items, with filters that can use schedule status and time-derived state.
- Study plans: list, add, view, filter, update, update progress, and delete plans.
- Finance: list, add, view, filter, update, delete, and calculate transaction statistics.
- Notes: list, add, view, filter, update, and delete notes.
- AI Q&A: calls the DeepSeek client with local summary context; when no API key is configured, it returns an unconfigured error instead of calling the real service.
- AI drafts: view, confirm, or cancel structured suggestion drafts, with import support for tasks and study plans.

The main menu contains summary, tasks, schedules, study plans, finance, notes, AI Q&A, AI drafts, help, and exit.

## Requirements

- Java 17
- Maven, using the version installed on the development machine
- JUnit Jupiter 5.14.4
- Mockito 5.18.0
- Maven Surefire Plugin 3.5.6
- Maven Failsafe Plugin 3.5.6
- JaCoCo Maven Plugin 0.8.13

## Build

Build the project from this directory:

```bash
mvn clean package
```

This command runs the default Maven build lifecycle, including the default tests.

## Tests

The default test lifecycle is isolated from real DeepSeek calls. It does not access the real DeepSeek service, network resources, or an API key.

## Unit Tests

Run unit tests from this directory:

```bash
mvn clean test
```

Recent validation record from v28: `mvn clean test` passed 944 tests with 0 failures.

## Integration Tests

Run the optional integration-test entry point from this directory:

```bash
mvn -Pintegration verify
```

The current repository does not contain `*IT.java` classes. This command is the optional integration-test entry point; it does not mean that a real DeepSeek connectivity test already exists. Future tests that perform real DeepSeek connectivity checks should use the `integration` profile, follow the `*IT.java` naming convention, and require network access plus `DEEPSEEK_API_KEY`.

## Coverage

Run the default verification lifecycle and generate JaCoCo coverage output from this directory:

```bash
mvn clean verify
mvn jacoco:report
```

The HTML report is generated at `target/site/jacoco/index.html`. Generated coverage files under `target/site/jacoco/` are not committed.

## Run

Build a runtime classpath, compile the application, and start the console entry point:

```bash
mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
mvn -q -DskipTests compile
java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

For a non-interactive smoke run that starts the entry point and exits immediately:

```bash
printf 'q\n' | java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

Demo data is enabled by default. To start without demo data:

```bash
ASSISTANT_DEMO_DATA=false java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
java -DASSISTANT_DEMO_DATA=false -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

`ASSISTANT_DEMO_DATA=false`, `ASSISTANT_DEMO_DATA=0`, and `ASSISTANT_DEMO_DATA=no` disable demo data. Other values, or an unset value, enable demo data.

## Configuration

The application reads these values from environment variables or Java system properties. Java system properties override environment variables when both are present.

| Name | Source | Default | Behavior |
|------|--------|---------|----------|
| `DEEPSEEK_API_KEY` | Environment variable or Java system property | Empty string | When empty, AI Q&A returns an unconfigured error; default tests do not require it. |
| `DEEPSEEK_BASE_URL` | Environment variable or Java system property | `https://api.deepseek.com` | DeepSeek base URL. |
| `DEEPSEEK_MODEL` | Environment variable or Java system property | `deepseek-v4-flash` | DeepSeek model name. |
| `DEEPSEEK_TIMEOUT_SECONDS` | Environment variable or Java system property | Code default timeout | Must be a positive integer number of seconds; invalid values make the application fall back to the no-API-key default configuration. |
| `ASSISTANT_DEMO_DATA` | Environment variable or Java system property | Enabled | `false`, `0`, or `no` disables demo data; other values or an unset value enable demo data. |

The default DeepSeek chat completions path is `/chat/completions`.

Do not commit API keys to source code, tests, or documentation examples.

## Common Workflows

- First run: build the classpath, compile, then run `assistant.app.Main` with the commands in the Run section.
- Run default tests: `mvn clean test`.
- Run default verification: `mvn clean verify`.
- Generate a coverage report: `mvn jacoco:report`, then open `target/site/jacoco/index.html`.
- Configure real AI: set `DEEPSEEK_API_KEY`, and optionally `DEEPSEEK_BASE_URL`, `DEEPSEEK_MODEL`, and `DEEPSEEK_TIMEOUT_SECONDS`, before starting `assistant.app.Main`.
- Run without demo data: set `ASSISTANT_DEMO_DATA=false`, `ASSISTANT_DEMO_DATA=0`, or `ASSISTANT_DEMO_DATA=no`.

## Known Limitations

- The application is a single-user console application.
- Current business data is stored in memory and is not persisted after the process exits.
- There is no database, file export, real system notification, or background reminder service.
- Default tests and default verification do not access the real DeepSeek service.
- The current repository has no `*IT.java` real DeepSeek connectivity tests.
- Real AI calls depend on external network access, DeepSeek service availability, and a valid `DEEPSEEK_API_KEY`.
- This README does not claim a concrete coverage percentage.

## Test Documentation

- [Test plan](docs/test-plan.md)
- [White-box test cases](docs/test-cases.md)
- [Defect and regression record](docs/defect-regression.md)
- [Coverage evidence notes](docs/coverage/README.md)
- [Environment](docs/environment.md)
