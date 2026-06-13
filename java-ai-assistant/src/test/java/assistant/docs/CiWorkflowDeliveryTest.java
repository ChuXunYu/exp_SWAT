package assistant.docs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CiWorkflowDeliveryTest {
    private static final Path WORKFLOW = repositoryRoot().resolve(".github/workflows/ci.yml");

    @Test
    void workflowFileExistsAtGithubActionsPathAndDeclaresCiWorkflow() {
        WorkflowYaml workflow = readWorkflow();

        assertAll(
                () -> assertTrue(Files.isRegularFile(WORKFLOW), "CI workflow file must exist"),
                () -> assertEquals("CI", workflow.scalar("name")),
                () -> assertTrue(workflow.map("on").containsKey("push"), "workflow must declare push trigger"),
                () -> assertTrue(workflow.map("jobs").containsKey("build"), "workflow must declare build job"),
                () -> assertFalse(workflow.source().contains("release:"),
                        "minimal CI gate must not define release automation"),
                () -> assertFalse(workflow.source().contains("upload-artifact"),
                        "minimal CI gate must not upload artifacts"));
    }

    @Test
    void workflowRunsForPushAndPullRequestEvents() {
        WorkflowYaml workflow = readWorkflow();
        Map<String, Object> triggers = workflow.map("on");

        assertAll(
                () -> assertTrue(triggers.containsKey("push"), "workflow must run for push events"),
                () -> assertTrue(triggers.containsKey("pull_request"),
                        "workflow must run for pull_request events"));
    }

    @Test
    void buildJobUsesUbuntuAndRunsInsideJavaAiAssistantModule() {
        Map<String, Object> buildJob = readWorkflow().buildJob();
        Map<String, Object> runDefaults = childMap(childMap(buildJob, "defaults"), "run");

        assertAll(
                () -> assertEquals("Build and test", buildJob.get("name")),
                () -> assertEquals("ubuntu-latest", buildJob.get("runs-on")),
                () -> assertEquals("java-ai-assistant", runDefaults.get("working-directory")));
    }

    @Test
    void workflowChecksOutCodeAndConfiguresTemurinJava17WithMavenCache() {
        List<Map<String, Object>> steps = readWorkflow().steps();
        Map<String, Object> checkout = steps.get(0);
        Map<String, Object> setupJava = steps.get(1);
        Map<String, Object> setupJavaWith = childMap(setupJava, "with");

        assertAll(
                () -> assertEquals("Checkout", checkout.get("name")),
                () -> assertEquals("actions/checkout@v4", checkout.get("uses")),
                () -> assertEquals("Set up JDK 17", setupJava.get("name")),
                () -> assertEquals("actions/setup-java@v4", setupJava.get("uses")),
                () -> assertEquals("temurin", setupJavaWith.get("distribution")),
                () -> assertEquals("17", setupJavaWith.get("java-version")),
                () -> assertEquals("maven", setupJavaWith.get("cache")));
    }

    @Test
    void workflowBuildsBeforeRunningDefaultUnitTests() {
        WorkflowYaml workflow = readWorkflow();
        List<Map<String, Object>> steps = workflow.steps();
        Map<String, Object> build = steps.get(2);
        Map<String, Object> test = steps.get(3);

        assertAll(
                () -> assertEquals(4, steps.size(), "workflow must keep the four designed steps"),
                () -> assertEquals("Build", build.get("name")),
                () -> assertEquals("mvn -B -DskipTests package", build.get("run")),
                () -> assertEquals("Test", test.get("name")),
                () -> assertEquals("mvn -B test", test.get("run")),
                () -> assertEquals(1, countStepRuns(steps, "mvn -B -DskipTests package")),
                () -> assertEquals(1, countStepRuns(steps, "mvn -B test")),
                () -> assertFalse(workflow.source().contains("-Pintegration"),
                        "default CI must not enable the integration profile"));
    }

    private static WorkflowYaml readWorkflow() {
        try {
            return WorkflowYaml.parse(Files.readString(WORKFLOW, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + WORKFLOW, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> childMap(Map<String, Object> parent, String key) {
        Object child = parent.get(key);
        assertTrue(child instanceof Map, () -> "Expected map at key: " + key);
        return (Map<String, Object>) child;
    }

    private static long countStepRuns(List<Map<String, Object>> steps, String command) {
        int count = 0;
        for (Map<String, Object> step : steps) {
            if (command.equals(step.get("run"))) {
                count++;
            }
        }
        return count;
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        if (Files.exists(current.resolve(".github/workflows"))) {
            return current;
        }
        Path parent = current.getParent();
        if (parent != null && Files.exists(parent.resolve(".github/workflows"))) {
            return parent;
        }
        throw new AssertionError("Unable to locate repository root from " + current);
    }

    private record WorkflowYaml(String source, Map<String, Object> document) {
        private static WorkflowYaml parse(String source) {
            Parser parser = new Parser(source);
            return new WorkflowYaml(source, parser.parse());
        }

        private String scalar(String key) {
            Object value = document.get(key);
            assertTrue(value instanceof String, () -> "Expected scalar at key: " + key);
            return (String) value;
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> map(String key) {
            Object value = document.get(key);
            assertTrue(value instanceof Map, () -> "Expected map at key: " + key);
            return (Map<String, Object>) value;
        }

        private Map<String, Object> buildJob() {
            return childMap(map("jobs"), "build");
        }

        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> steps() {
            Object steps = buildJob().get("steps");
            assertTrue(steps instanceof List, "build job must contain steps");
            return (List<Map<String, Object>>) steps;
        }
    }

    private static final class Parser {
        private final List<YamlLine> lines;
        private int index;

        private Parser(String source) {
            this.lines = source.lines()
                    .filter(line -> !line.isBlank())
                    .map(YamlLine::new)
                    .toList();
        }

        private Map<String, Object> parse() {
            Map<String, Object> document = parseMap(0);
            if (index != lines.size()) {
                throw new YamlParseException("Unexpected YAML content near line " + (index + 1));
            }
            return document;
        }

        private Map<String, Object> parseMap(int indent) {
            Map<String, Object> values = new LinkedHashMap<>();
            while (index < lines.size()) {
                YamlLine line = lines.get(index);
                if (line.indent < indent) {
                    break;
                }
                if (line.indent > indent || line.text.startsWith("- ")) {
                    throw new YamlParseException("Unexpected YAML indentation: " + line.original);
                }
                KeyValue keyValue = KeyValue.parse(line.text);
                index++;
                values.put(keyValue.key(), parseValue(indent, keyValue.value()));
            }
            return values;
        }

        private Object parseValue(int parentIndent, String value) {
            if (!value.isEmpty()) {
                return unquote(value);
            }
            if (index >= lines.size() || lines.get(index).indent <= parentIndent) {
                return new LinkedHashMap<String, Object>();
            }
            YamlLine child = lines.get(index);
            if (child.text.startsWith("- ")) {
                return parseList(child.indent);
            }
            return parseMap(child.indent);
        }

        private List<Map<String, Object>> parseList(int indent) {
            List<Map<String, Object>> values = new ArrayList<>();
            while (index < lines.size()) {
                YamlLine line = lines.get(index);
                if (line.indent < indent) {
                    break;
                }
                if (line.indent > indent || !line.text.startsWith("- ")) {
                    throw new YamlParseException("Unexpected YAML list item: " + line.original);
                }
                Map<String, Object> item = new LinkedHashMap<>();
                String firstEntry = line.text.substring(2);
                index++;
                if (!firstEntry.isBlank()) {
                    KeyValue keyValue = KeyValue.parse(firstEntry);
                    item.put(keyValue.key(), parseValue(indent, keyValue.value()));
                }
                if (index < lines.size() && lines.get(index).indent > indent) {
                    item.putAll(parseMap(lines.get(index).indent));
                }
                values.add(item);
            }
            return values;
        }

        private String unquote(String value) {
            if ((value.startsWith("'") && value.endsWith("'"))
                    || (value.startsWith("\"") && value.endsWith("\""))) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
    }

    private record YamlLine(String original, int indent, String text) {
        private YamlLine(String original) {
            this(original, countLeadingSpaces(original), original.stripLeading());
        }

        private static int countLeadingSpaces(String line) {
            int count = 0;
            while (count < line.length() && line.charAt(count) == ' ') {
                count++;
            }
            return count;
        }
    }

    private record KeyValue(String key, String value) {
        private static KeyValue parse(String line) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new YamlParseException("Expected YAML key/value line: " + line);
            }
            return new KeyValue(line.substring(0, separator), line.substring(separator + 1).trim());
        }
    }

    private static final class YamlParseException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        private YamlParseException(String message) {
            super(message);
        }
    }
}
