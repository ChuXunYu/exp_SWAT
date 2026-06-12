package assistant.ai;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.task.TaskPriority;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StructuredSuggestionParser {
    private static final String INVALID_MESSAGE = "AI structured suggestion format is invalid";

    private final ObjectMapper objectMapper;

    public StructuredSuggestionParser() {
        this(new ObjectMapper());
    }

    public StructuredSuggestionParser(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public OperationResult<SuggestionDraft> parse(String aiText, EntityId draftId) {
        Objects.requireNonNull(draftId, "draftId");
        if (aiText == null || aiText.isBlank()) {
            return malformed();
        }

        try {
            JsonNode root = parseRoot(aiText);
            if (!root.isObject()) {
                return malformed();
            }
            return OperationResult.success(toDraft(root, draftId));
        } catch (IOException | IllegalArgumentException e) {
            return malformed();
        }
    }

    private JsonNode parseRoot(String aiText) throws IOException {
        try {
            return parseSingleJsonValue(aiText);
        } catch (IOException e) {
            String fencedContent = extractSingleFencedContent(aiText);
            if (fencedContent == null) {
                throw e;
            }
            return parseSingleJsonValue(fencedContent);
        }
    }

    private JsonNode parseSingleJsonValue(String text) throws IOException {
        try (JsonParser parser = objectMapper.getFactory().createParser(text)) {
            JsonNode node = objectMapper.readTree(parser);
            if (node == null) {
                throw new IOException("empty JSON");
            }
            if (parser.nextToken() != null) {
                throw new IOException("trailing token");
            }
            return node;
        }
    }

    private static String extractSingleFencedContent(String aiText) {
        String stripped = aiText.strip();
        if (!stripped.startsWith("```json\n") && !stripped.startsWith("```\n")) {
            return null;
        }
        if (!stripped.endsWith("\n```")) {
            return null;
        }

        int firstLineEnd = stripped.indexOf('\n');
        String content = stripped.substring(firstLineEnd + 1, stripped.length() - "\n```".length());
        if (content.contains("\n```")) {
            return null;
        }
        return content;
    }

    private static SuggestionDraft toDraft(JsonNode root, EntityId draftId) {
        SuggestionDraftType type = parseType(root);
        if (type.isTaskDraft()) {
            return SuggestionDraft.forTasks(draftId, parseTasks(required(root, "tasks")));
        }
        return SuggestionDraft.forStudyPlan(draftId, parseStudyPlan(required(root, "studyPlan")));
    }

    private static SuggestionDraftType parseType(JsonNode root) {
        String text = requiredText(root, "type");
        return SuggestionDraftType.valueOf(text);
    }

    private static List<TaskDraftItem> parseTasks(JsonNode tasksNode) {
        if (!tasksNode.isArray() || tasksNode.isEmpty()) {
            throw new IllegalArgumentException("tasks must be a non-empty array");
        }

        List<TaskDraftItem> tasks = new ArrayList<>();
        for (JsonNode taskNode : tasksNode) {
            if (!taskNode.isObject()) {
                throw new IllegalArgumentException("task must be an object");
            }
            tasks.add(new TaskDraftItem(
                    requiredText(taskNode, "title"),
                    optionalText(taskNode, "description", ""),
                    parsePriority(requiredText(taskNode, "priority")),
                    optionalDate(taskNode, "dueDate")));
        }
        return tasks;
    }

    private static StudyPlanDraftContent parseStudyPlan(JsonNode studyPlanNode) {
        if (!studyPlanNode.isObject()) {
            throw new IllegalArgumentException("studyPlan must be an object");
        }

        return new StudyPlanDraftContent(
                requiredText(studyPlanNode, "goalName"),
                requiredDate(studyPlanNode, "startDate"),
                requiredDate(studyPlanNode, "endDate"),
                requiredInt(studyPlanNode, "expectedHours"),
                Progress.of(optionalInt(studyPlanNode, "initialProgress", 0)),
                parseBreakdown(studyPlanNode.get("breakdown")));
    }

    private static List<String> parseBreakdown(JsonNode breakdownNode) {
        if (breakdownNode == null || breakdownNode.isNull()) {
            return List.of();
        }
        if (!breakdownNode.isArray()) {
            throw new IllegalArgumentException("breakdown must be an array");
        }

        List<String> breakdown = new ArrayList<>();
        for (JsonNode itemNode : breakdownNode) {
            if (!itemNode.isTextual()) {
                throw new IllegalArgumentException("breakdown item must be a string");
            }
            breakdown.add(itemNode.textValue());
        }
        return breakdown;
    }

    private static JsonNode required(JsonNode objectNode, String fieldName) {
        JsonNode fieldNode = objectNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return fieldNode;
    }

    private static String requiredText(JsonNode objectNode, String fieldName) {
        JsonNode fieldNode = required(objectNode, fieldName);
        if (!fieldNode.isTextual()) {
            throw new IllegalArgumentException(fieldName + " must be a string");
        }
        return fieldNode.textValue();
    }

    private static String optionalText(JsonNode objectNode, String fieldName, String defaultValue) {
        JsonNode fieldNode = objectNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        if (!fieldNode.isTextual()) {
            throw new IllegalArgumentException(fieldName + " must be a string");
        }
        return fieldNode.textValue();
    }

    private static int requiredInt(JsonNode objectNode, String fieldName) {
        JsonNode fieldNode = required(objectNode, fieldName);
        if (!fieldNode.isInt()) {
            throw new IllegalArgumentException(fieldName + " must be an integer");
        }
        return fieldNode.intValue();
    }

    private static int optionalInt(JsonNode objectNode, String fieldName, int defaultValue) {
        JsonNode fieldNode = objectNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        if (!fieldNode.isInt()) {
            throw new IllegalArgumentException(fieldName + " must be an integer");
        }
        return fieldNode.intValue();
    }

    private static LocalDate requiredDate(JsonNode objectNode, String fieldName) {
        return parseDate(requiredText(objectNode, fieldName));
    }

    private static LocalDate optionalDate(JsonNode objectNode, String fieldName) {
        JsonNode fieldNode = objectNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        if (!fieldNode.isTextual()) {
            throw new IllegalArgumentException(fieldName + " must be a string");
        }
        return parseDate(fieldNode.textValue());
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("date must be ISO-8601", e);
        }
    }

    private static TaskPriority parsePriority(String text) {
        return TaskPriority.valueOf(text);
    }

    private static OperationResult<SuggestionDraft> malformed() {
        return OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, INVALID_MESSAGE);
    }
}
