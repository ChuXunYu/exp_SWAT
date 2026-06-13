package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.summary.DashboardSummary;
import assistant.summary.LocalContext;
import assistant.testability.IdGenerator;
import assistant.task.TaskPriority;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StructuredSuggestionDraftServiceTest {
    @Test
    void generateTaskDraftParsesAssignsIdAndSavesDraft() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        FakeAiClient aiClient = new FakeAiClient(taskDraftJson("2026-06-30"));
        StructuredSuggestionDraftService service = service(repository, aiClient, configured());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("整理明天任务");

        SuggestionDraftView view = result.getPayload();
        TaskDraftItem task = view.tasks().get(0);
        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(new EntityId(100), view.id()),
                () -> assertEquals(SuggestionDraftType.TASK_DRAFT, view.type()),
                () -> assertEquals("写测试", task.title()),
                () -> assertEquals(TaskPriority.HIGH, task.priority()),
                () -> assertEquals(LocalDate.of(2026, 6, 30), task.dueDate()),
                () -> assertTrue(repository.findById(new EntityId(100)).isPresent()),
                () -> assertEquals(1, aiClient.calls));
    }

    @Test
    void generateStudyPlanDraftParsesAssignsIdSavesBreakdown() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        FakeAiClient aiClient = new FakeAiClient("""
                {
                  "type":"STUDY_PLAN_DRAFT",
                  "studyPlan":{
                    "goalName":"准备考试",
                    "startDate":"2026-07-01",
                    "endDate":"2026-07-31",
                    "expectedHours":30,
                    "initialProgress":5,
                    "breakdown":["复习基础","刷题"]
                  }
                }
                """);
        StructuredSuggestionDraftService service = service(repository, aiClient, configured());

        OperationResult<SuggestionDraftView> result = service.generateStudyPlanDraft("准备考试");

        SuggestionDraftView view = result.getPayload();
        StudyPlanDraftContent studyPlan = view.studyPlan().orElseThrow();
        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(new EntityId(100), view.id()),
                () -> assertEquals(SuggestionDraftType.STUDY_PLAN_DRAFT, view.type()),
                () -> assertEquals("准备考试", studyPlan.goalName()),
                () -> assertEquals(List.of("复习基础", "刷题"), studyPlan.breakdown()),
                () -> assertTrue(repository.findById(new EntityId(100)).isPresent()));
    }

    @Test
    void generateTaskDraftRejectsMissingDueDateWithoutSaving() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        StructuredSuggestionDraftService service = service(
                repository,
                new FakeAiClient(taskDraftJson(null)),
                configured());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("整理明天任务");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("task draft dueDate is required before saving", result.getMessage()),
                () -> assertTrue(repository.findAll().isEmpty()));
    }

    @Test
    void generateDraftPropagatesNotConfiguredWithoutSaving() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        FakeAiClient aiClient = new FakeAiClient(taskDraftJson("2026-06-30"));
        StructuredSuggestionDraftService service = service(repository, aiClient, AiConfiguration.defaultWithoutApiKey());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("整理明天任务");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_NOT_CONFIGURED, result.getErrorCode()),
                () -> assertEquals(0, aiClient.calls),
                () -> assertTrue(repository.findAll().isEmpty()));
    }

    @Test
    void generateDraftRejectsBlankGoalWithoutCallingAi() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        FakeAiClient aiClient = new FakeAiClient(taskDraftJson("2026-06-30"));
        StructuredSuggestionDraftService service = service(repository, aiClient, configured());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("   ");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("user goal must not be blank", result.getMessage()),
                () -> assertEquals(0, aiClient.calls),
                () -> assertTrue(repository.findAll().isEmpty()));
    }

    @Test
    void generateDraftPropagatesEmptyResponseWithoutSaving() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        FakeAiClient aiClient = new FakeAiClient(OperationResult.success((AiResponse) null));
        StructuredSuggestionDraftService service = service(repository, aiClient, configured());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("整理明天任务");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_EMPTY_RESPONSE, result.getErrorCode()),
                () -> assertTrue(repository.findAll().isEmpty()));
    }

    @Test
    void generateDraftRejectsMalformedJsonWithoutSaving() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        StructuredSuggestionDraftService service = service(repository, new FakeAiClient("not json"), configured());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("整理明天任务");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_MALFORMED_RESPONSE, result.getErrorCode()),
                () -> assertTrue(repository.findAll().isEmpty()));
    }

    @Test
    void generateDraftRejectsInvalidStructuredFieldsWithoutSaving() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        StructuredSuggestionDraftService service = service(
                repository,
                new FakeAiClient("{\"type\":\"TASK_DRAFT\",\"tasks\":[{\"title\":\"A\",\"priority\":\"urgent\"}]}"),
                configured());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("整理明天任务");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_MALFORMED_RESPONSE, result.getErrorCode()),
                () -> assertTrue(repository.findAll().isEmpty()));
    }

    @Test
    void generateTaskDraftRejectsMismatchedStudyPlanTypeWithoutSaving() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        StructuredSuggestionDraftService service = service(
                repository,
                new FakeAiClient("""
                        {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"A","startDate":"2026-07-01",
                        "endDate":"2026-07-31","expectedHours":1}}
                        """),
                configured());

        OperationResult<SuggestionDraftView> result = service.generateTaskDraft("整理明天任务");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("AI structured suggestion type does not match requested draft type", result.getMessage()),
                () -> assertTrue(repository.findAll().isEmpty()));
    }

    @Test
    void constructorRejectsNullDependencies() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        AiAssistantService aiAssistantService = aiAssistantService(new FakeAiClient("not used"), configured());
        StructuredSuggestionParser parser = new StructuredSuggestionParser();
        IdGenerator idGenerator = new FixedIdGenerator();

        assertAll(
                () -> assertNullFieldRejected("aiAssistantService",
                        () -> new StructuredSuggestionDraftService(null, parser, repository, idGenerator)),
                () -> assertNullFieldRejected("parser",
                        () -> new StructuredSuggestionDraftService(aiAssistantService, null, repository, idGenerator)),
                () -> assertNullFieldRejected("repository",
                        () -> new StructuredSuggestionDraftService(aiAssistantService, parser, null, idGenerator)),
                () -> assertNullFieldRejected("idGenerator",
                        () -> new StructuredSuggestionDraftService(aiAssistantService, parser, repository, null)));
    }

    private static StructuredSuggestionDraftService service(
            SuggestionDraftRepository repository,
            FakeAiClient aiClient,
            AiConfiguration configuration) {
        return new StructuredSuggestionDraftService(
                aiAssistantService(aiClient, configuration),
                new StructuredSuggestionParser(),
                repository,
                new FixedIdGenerator());
    }

    private static AiAssistantService aiAssistantService(FakeAiClient aiClient, AiConfiguration configuration) {
        return new AiAssistantService(
                configuration,
                new FakeContextProvider(),
                new PromptBuilder(),
                aiClient);
    }

    private static AiConfiguration configured() {
        return new AiConfiguration("https://api.example.com", "/chat", "model-a", "test-key", Duration.ofSeconds(5));
    }

    private static String taskDraftJson(String dueDate) {
        String dueDateField = dueDate == null ? "" : ",\"dueDate\":\"" + dueDate + "\"";
        return """
                {"type":"TASK_DRAFT","tasks":[{"title":"写测试","description":"覆盖服务","priority":"HIGH"%s}]}
                """.formatted(dueDateField);
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private static LocalContext context() {
        LocalDate today = LocalDate.of(2026, 6, 12);
        DashboardSummary summary = new DashboardSummary(
                today,
                today,
                today,
                today,
                today,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                0,
                0,
                assistant.finance.FinanceStatistics.zero(),
                List.of(),
                0,
                Map.of());
        return LocalContext.from(summary);
    }

    private static final class FakeContextProvider implements ContextProvider {
        @Override
        public OperationResult<LocalContext> getLocalContext() {
            return OperationResult.success(context());
        }
    }

    private static final class FakeAiClient implements AiClient {
        private OperationResult<AiResponse> result;
        private AiRequest receivedRequest;
        private int calls;

        private FakeAiClient(String content) {
            this(OperationResult.success(new AiResponse(content)));
        }

        private FakeAiClient(OperationResult<AiResponse> result) {
            this.result = result;
        }

        @Override
        public OperationResult<AiResponse> chat(AiRequest request) {
            calls++;
            receivedRequest = request;
            return result;
        }
    }

    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public EntityId nextId() {
            return new EntityId(100);
        }
    }
}
