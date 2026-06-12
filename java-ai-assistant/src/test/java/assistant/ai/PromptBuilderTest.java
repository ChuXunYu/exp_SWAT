package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.common.Tag;
import assistant.common.TransactionAmount;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionType;
import assistant.finance.TransactionView;
import assistant.schedule.ScheduleStatus;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanStatus;
import assistant.study.StudyPlanView;
import assistant.summary.DashboardSummary;
import assistant.summary.LocalContext;
import assistant.task.TaskPriority;
import assistant.task.TaskStatus;
import assistant.task.TaskView;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptBuilderTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);
    private static final LocalDate WEEK_START = LocalDate.of(2026, 6, 8);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 6, 14);
    private static final LocalDate MONTH_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate MONTH_END = LocalDate.of(2026, 6, 30);

    private final PromptBuilder builder = new PromptBuilder();

    @Test
    void buildCreatesNonStreamingRequestWithConfiguredModel() {
        OperationResult<AiRequest> result =
                builder.build(AiScenario.GENERAL_QA, " 如何安排今天？ ", configuration(), populatedContext());

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals("model-a", result.getPayload().model()),
                () -> assertFalse(result.getPayload().stream()),
                () -> assertEquals(2, result.getPayload().messages().size()),
                () -> assertEquals(AiRole.SYSTEM, result.getPayload().messages().get(0).role()),
                () -> assertEquals(AiRole.USER, result.getPayload().messages().get(1).role()));
    }

    @Test
    void buildUsesTrimmedUserQuestionInUserMessage() {
        OperationResult<AiRequest> result =
                builder.build(AiScenario.GENERAL_QA, "  如何安排今天？  ", configuration(), emptyContext());

        String userMessage = result.getPayload().messages().get(1).content();

        assertAll(
                () -> assertTrue(userMessage.contains("用户问题：\n如何安排今天？")),
                () -> assertFalse(userMessage.contains("  如何安排今天？  ")));
    }

    @Test
    void buildIncludesOverviewAndAllContextSections() {
        OperationResult<AiRequest> result =
                builder.build(AiScenario.STUDY_ADVICE, "给我建议", configuration(), populatedContext());

        String userMessage = result.getPayload().messages().get(1).content();

        assertAll(
                () -> assertTrue(userMessage.contains("今日任务1项")),
                () -> assertTrue(userMessage.contains("今日任务：\n- 任务：Review")),
                () -> assertTrue(userMessage.contains("今日日程：\n- 日程：Standup")),
                () -> assertTrue(userMessage.contains("本周学习计划：\n- 学习：Java")),
                () -> assertTrue(userMessage.contains("本月收支：\n- 收支：EXPENSE")),
                () -> assertTrue(userMessage.contains("笔记标签：\n- 标签：java")));
    }

    @Test
    void buildUsesStableEmptyMarkerForEmptyDetailLists() {
        OperationResult<AiRequest> result =
                builder.build(AiScenario.GENERAL_QA, "给我建议", configuration(), emptyContext());

        String userMessage = result.getPayload().messages().get(1).content();

        assertAll(
                () -> assertTrue(userMessage.contains("今日任务0项")),
                () -> assertTrue(userMessage.contains("今日任务：\n（无）")),
                () -> assertTrue(userMessage.contains("今日日程：\n（无）")),
                () -> assertTrue(userMessage.contains("本周学习计划：\n（无）")),
                () -> assertTrue(userMessage.contains("本月收支：\n（无）")),
                () -> assertTrue(userMessage.contains("笔记标签：\n（无）")));
    }

    @Test
    void buildIncludesScenarioInstructionForGeneralStudyAndNoteScenarios() {
        assertAll(
                () -> assertSystemMessageContains(AiScenario.GENERAL_QA, "个人学习与生活助手"),
                () -> assertSystemMessageContains(AiScenario.STUDY_ADVICE, "学习建议"),
                () -> assertSystemMessageContains(AiScenario.NOTE_SUMMARY, "笔记总结建议"));
    }

    @Test
    void buildAddsStructuredJsonInstructionForTaskSuggestion() {
        String systemMessage = systemMessageFor(AiScenario.STRUCTURED_TASK_SUGGESTION);

        assertAll(
                () -> assertTrue(systemMessage.contains("只返回单个 JSON 对象")),
                () -> assertTrue(systemMessage.contains("TASK_DRAFT")));
    }

    @Test
    void buildAddsStructuredJsonInstructionForStudyPlanSuggestion() {
        String systemMessage = systemMessageFor(AiScenario.STRUCTURED_STUDY_PLAN_SUGGESTION);

        assertAll(
                () -> assertTrue(systemMessage.contains("只返回单个 JSON 对象")),
                () -> assertTrue(systemMessage.contains("STUDY_PLAN_DRAFT")));
    }

    @Test
    void buildReturnsValidationFailureForBlankQuestion() {
        assertBlankQuestionFails(null);
        assertBlankQuestionFails("");
        assertBlankQuestionFails("   ");
    }

    @Test
    void buildRejectsNullDependencies() {
        assertAll(
                () -> assertNullFieldRejected("scenario",
                        () -> builder.build(null, "question", configuration(), emptyContext())),
                () -> assertNullFieldRejected("configuration",
                        () -> builder.build(AiScenario.GENERAL_QA, "question", null, emptyContext())),
                () -> assertNullFieldRejected("localContext",
                        () -> builder.build(AiScenario.GENERAL_QA, "question", configuration(), null)));
    }

    private void assertSystemMessageContains(AiScenario scenario, String expectedText) {
        assertTrue(systemMessageFor(scenario).contains(expectedText));
    }

    private String systemMessageFor(AiScenario scenario) {
        OperationResult<AiRequest> result = builder.build(scenario, "question", configuration(), emptyContext());
        return result.getPayload().messages().get(0).content();
    }

    private void assertBlankQuestionFails(String question) {
        OperationResult<AiRequest> result = builder.build(AiScenario.GENERAL_QA, question, configuration(), emptyContext());

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("user question must not be blank", result.getMessage()));
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static AiConfiguration configuration() {
        return new AiConfiguration("https://api.example.com", "/chat", "model-a", "placeholder-key", Duration.ofSeconds(5));
    }

    private static LocalContext emptyContext() {
        return LocalContext.from(summaryWith(List.of(), List.of(), List.of(), List.of(), Map.of()));
    }

    private static LocalContext populatedContext() {
        LinkedHashMap<Tag, Integer> tags = new LinkedHashMap<>();
        tags.put(Tag.of("java"), 2);
        return LocalContext.from(summaryWith(
                List.of(task()),
                List.of(schedule()),
                List.of(plan()),
                List.of(transaction()),
                tags));
    }

    private static DashboardSummary summaryWith(
            List<TaskView> tasks,
            List<ScheduleView> schedules,
            List<StudyPlanView> plans,
            List<TransactionView> transactions,
            Map<Tag, Integer> tags) {
        int completed = (int) plans.stream().filter(plan -> plan.status() == StudyPlanStatus.COMPLETED).count();
        return new DashboardSummary(
                TODAY,
                WEEK_START,
                WEEK_END,
                MONTH_START,
                MONTH_END,
                tasks,
                schedules,
                plans,
                completed,
                plans.size() - completed,
                FinanceStatistics.zero(),
                transactions,
                tags.values().stream().mapToInt(Integer::intValue).sum(),
                tags);
    }

    private static TaskView task() {
        return new TaskView(new EntityId(1), "Review", "Description", TaskPriority.HIGH, TODAY, TaskStatus.TODO);
    }

    private static ScheduleView schedule() {
        return new ScheduleView(
                new EntityId(2),
                "Standup",
                new DateTimeRange(LocalDateTime.of(2026, 6, 12, 9, 0), LocalDateTime.of(2026, 6, 12, 10, 0)),
                LocalDateTime.of(2026, 6, 12, 9, 0),
                LocalDateTime.of(2026, 6, 12, 10, 0),
                "Room A",
                "Description",
                ScheduleStatus.UPCOMING);
    }

    private static StudyPlanView plan() {
        return new StudyPlanView(
                new EntityId(3),
                "Java",
                new DateRange(WEEK_START, WEEK_END),
                WEEK_START,
                WEEK_END,
                8,
                Progress.of(100),
                StudyPlanStatus.COMPLETED);
    }

    private static TransactionView transaction() {
        return new TransactionView(
                new EntityId(4),
                TransactionType.EXPENSE,
                TransactionAmount.of("8.50"),
                "Food",
                TODAY,
                "Lunch");
    }
}
