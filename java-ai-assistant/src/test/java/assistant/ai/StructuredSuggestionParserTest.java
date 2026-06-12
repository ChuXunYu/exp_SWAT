package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.task.TaskPriority;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StructuredSuggestionParserTest {
    private static final EntityId DRAFT_ID = new EntityId(100);

    private final StructuredSuggestionParser parser = new StructuredSuggestionParser();

    @Test
    void parsesTaskDraftJson() {
        OperationResult<SuggestionDraft> result = parser.parse("""
                {
                  "type": "TASK_DRAFT",
                  "tasks": [
                    {
                      "title": " Write tests ",
                      "description": " Cover parser ",
                      "priority": "HIGH",
                      "dueDate": "2026-06-30"
                    }
                  ],
                  "extra": true
                }
                """, DRAFT_ID);

        assertTrue(result.isSuccess());
        SuggestionDraft draft = result.getPayload();
        TaskDraftItem task = draft.getTasks().get(0);
        assertAll(
                () -> assertEquals(DRAFT_ID, draft.getId()),
                () -> assertEquals(SuggestionDraftType.TASK_DRAFT, draft.getType()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()),
                () -> assertEquals("Write tests", task.title()),
                () -> assertEquals("Cover parser", task.description()),
                () -> assertEquals(TaskPriority.HIGH, task.priority()),
                () -> assertEquals(LocalDate.of(2026, 6, 30), task.dueDate()));
    }

    @Test
    void parsesTaskDraftDefaults() {
        OperationResult<SuggestionDraft> result = parser.parse("""
                {"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}
                """, DRAFT_ID);

        TaskDraftItem task = result.getPayload().getTasks().get(0);
        assertEquals("", task.description());
        assertNull(task.dueDate());
    }

    @Test
    void parsesStudyPlanDraftJson() {
        OperationResult<SuggestionDraft> result = parser.parse("""
                {
                  "type": "STUDY_PLAN_DRAFT",
                  "studyPlan": {
                    "goalName": " Learn Java ",
                    "startDate": "2026-07-01",
                    "endDate": "2026-07-31",
                    "expectedHours": 40,
                    "initialProgress": 15,
                    "breakdown": [" Basics ", " ", "Practice"]
                  }
                }
                """, DRAFT_ID);

        assertTrue(result.isSuccess());
        SuggestionDraft draft = result.getPayload();
        StudyPlanDraftContent studyPlan = draft.getStudyPlan().orElseThrow();
        assertAll(
                () -> assertEquals(SuggestionDraftType.STUDY_PLAN_DRAFT, draft.getType()),
                () -> assertEquals(List.of(), draft.getTasks()),
                () -> assertEquals("Learn Java", studyPlan.goalName()),
                () -> assertEquals(LocalDate.of(2026, 7, 1), studyPlan.startDate()),
                () -> assertEquals(LocalDate.of(2026, 7, 31), studyPlan.endDate()),
                () -> assertEquals(40, studyPlan.expectedHours()),
                () -> assertEquals(Progress.of(15), studyPlan.initialProgress()),
                () -> assertEquals(List.of("Basics", "Practice"), studyPlan.breakdown()));
    }

    @Test
    void parsesStudyPlanDefaults() {
        OperationResult<SuggestionDraft> result = parser.parse("""
                {
                  "type":"STUDY_PLAN_DRAFT",
                  "studyPlan":{
                    "goalName":"Learn Java",
                    "startDate":"2026-07-01",
                    "endDate":"2026-07-31",
                    "expectedHours":40
                  }
                }
                """, DRAFT_ID);

        StudyPlanDraftContent studyPlan = result.getPayload().getStudyPlan().orElseThrow();
        assertEquals(Progress.zero(), studyPlan.initialProgress());
        assertEquals(List.of(), studyPlan.breakdown());
    }

    @Test
    void parsesSingleFencedJsonBlock() {
        OperationResult<SuggestionDraft> result = parser.parse("""
                ```json
                {"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}
                ```
                """, DRAFT_ID);

        assertTrue(result.isSuccess());
        assertEquals("A", result.getPayload().getTasks().get(0).title());
    }

    @Test
    void parsesSingleUnlabeledFencedJsonBlock() {
        OperationResult<SuggestionDraft> result = parser.parse("""
                ```
                {"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}
                ```
                """, DRAFT_ID);

        assertTrue(result.isSuccess());
    }

    @Test
    void rejectsMalformedInputs() {
        assertMalformed(null);
        assertMalformed("   ");
        assertMalformed("[]");
        assertMalformed("{\"tasks\":[]}");
        assertMalformed("{\"type\":1,\"tasks\":[]}");
        assertMalformed("{\"type\":\"task_draft\",\"tasks\":[]}");
        assertMalformed("{\"type\":\"TASK_DRAFT\",\"tasks\":[]}");
        assertMalformed("{\"type\":\"TASK_DRAFT\",\"tasks\":[1]}");
        assertMalformed("{\"type\":\"TASK_DRAFT\",\"tasks\":[{\"title\":\"A\",\"priority\":\"low\"}]}");
        assertMalformed("{\"type\":\"TASK_DRAFT\",\"tasks\":[{\"title\":\"A\",\"priority\":\"LOW\",\"dueDate\":\"bad\"}]}");
        assertMalformed("{\"type\":\"TASK_DRAFT\",\"tasks\":[{\"title\":\"A\",\"description\":1,\"priority\":\"LOW\"}]}");
        assertMalformed("{\"type\":\"TASK_DRAFT\",\"tasks\":[{\"title\":\"A\",\"priority\":\"LOW\",\"dueDate\":1}]}");
        assertMalformed("""
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":"2026-07-01",
                "endDate":"2026-07-31"}}
                """);
        assertMalformed("""
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":"2026-07-01",
                "endDate":"2026-07-31","expectedHours":1.5}}
                """);
        assertMalformed("""
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":1,
                "endDate":"2026-07-31","expectedHours":1}}
                """);
        assertMalformed("""
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":"2026-07-01",
                "endDate":"2026-06-30","expectedHours":1}}
                """);
        assertMalformed("""
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":"2026-07-01",
                "endDate":"2026-07-31","expectedHours":0}}
                """);
        assertMalformed("""
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":"2026-07-01",
                "endDate":"2026-07-31","expectedHours":1,"initialProgress":101}}
                """);
        assertMalformed("""
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":"2026-07-01",
                "endDate":"2026-07-31","expectedHours":1,"breakdown":[1]}}
                """);
    }

    @Test
    void rejectsTextAroundJsonAndTrailingTokens() {
        assertMalformed("prefix {\"type\":\"TASK_DRAFT\",\"tasks\":[{\"title\":\"A\",\"priority\":\"LOW\"}]}");
        assertMalformed("{\"type\":\"TASK_DRAFT\",\"tasks\":[{\"title\":\"A\",\"priority\":\"LOW\"}]} extra text");
        assertMalformed("""
                {"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}
                {"type":"TASK_DRAFT","tasks":[{"title":"B","priority":"LOW"}]}
                """);
    }

    @Test
    void rejectsInvalidFencedJsonBlocks() {
        assertMalformed("""
                text
                ```json
                {"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}
                ```
                """);
        assertMalformed("""
                ```json
                {"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]} extra text
                ```
                """);
        assertMalformed("""
                ```json
                {"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}
                ```
                ```json
                {"type":"TASK_DRAFT","tasks":[{"title":"B","priority":"LOW"}]}
                ```
                """);
    }

    @Test
    void rejectsNullDraftId() {
        assertThrows(NullPointerException.class, () -> parser.parse("{}", null));
    }

    @Test
    void rejectsNullObjectMapper() {
        assertThrows(NullPointerException.class, () -> new StructuredSuggestionParser(null));
    }

    private void assertMalformed(String text) {
        OperationResult<SuggestionDraft> result = parser.parse(text, DRAFT_ID);

        assertTrue(result.isFailure());
        assertEquals(ErrorCode.AI_MALFORMED_RESPONSE, result.getErrorCode());
        assertEquals("AI structured suggestion format is invalid", result.getMessage());
        assertNull(result.getPayload());
    }
}
