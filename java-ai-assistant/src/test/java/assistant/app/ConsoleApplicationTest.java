package assistant.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.finance.FinanceService;
import assistant.testability.FixedTimeProvider;
import assistant.task.TaskService;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConsoleApplicationTest {
    @Test
    void runPrintsMenuAndExitsOnQuit() {
        String output = runWithInput(servicesWithDemoData(), "q\n");

        assertAll(
                () -> assertContains(output, "Java AI Assistant"),
                () -> assertContains(output, "1. 汇总"),
                () -> assertContains(output, "已退出。"));
    }

    @Test
    void runExitsOnEndOfInput() {
        String output = runWithInput(servicesWithDemoData(), "");

        assertContains(output, "主菜单");
    }

    @Test
    void summaryCommandDisplaysDashboardSummary() {
        String output = runWithInput(servicesWithDemoData(), "1\nq\n");

        assertAll(
                () -> assertContains(output, "今日:"),
                () -> assertContains(output, "本月收入:"),
                () -> assertContains(output, "本月结余:"));
    }

    @Test
    void listCommandsDisplayEachCoreEntry() {
        String output = runWithInput(servicesWithDemoData(), "2\n3\n4\n5\n6\n8\nq\n");

        assertAll(
                () -> assertContains(output, "任务列表"),
                () -> assertContains(output, "日程列表"),
                () -> assertContains(output, "学习计划列表"),
                () -> assertContains(output, "收支统计"),
                () -> assertContains(output, "笔记列表"),
                () -> assertContains(output, "AI 草稿列表"));
    }

    @Test
    void listCommandsDisplayEmptyStateWithoutDemoData() {
        String output = runWithInput(servicesWithoutDemoData(), "2\n3\n4\n5\n6\n8\nq\n");

        assertAll(
                () -> assertContains(output, "暂无任务"),
                () -> assertContains(output, "暂无日程"),
                () -> assertContains(output, "暂无学习计划"),
                () -> assertContains(output, "暂无收支记录"),
                () -> assertContains(output, "暂无笔记"),
                () -> assertContains(output, "暂无 AI 草稿"));
    }

    @Test
    void taskCommandDisplaysFailureCodeAndMessage() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        TaskService taskService = mock(TaskService.class);
        when(taskService.listTasks())
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "task list failed"));
        ApplicationServices services = new ApplicationServices(
                taskService,
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "2\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "task list failed"));
    }

    @Test
    void transactionCommandDisplaysStatisticsFailureCodeAndMessage() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        when(financeService.listTransactions()).thenReturn(OperationResult.success(List.of()));
        when(financeService.calculateStatistics())
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "statistics failed"));
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "statistics failed"));
    }

    @Test
    void aiCommandShowsNotConfiguredAndContinues() {
        String output = runWithInput(servicesWithDemoData(), "7\n今天有什么安排？\n1\nq\n");

        assertAll(
                () -> assertContains(output, "AI_NOT_CONFIGURED"),
                () -> assertContains(output, "今日:"));
    }

    @Test
    void aiCommandRejectsBlankQuestionAndReturnsToMenu() {
        String output = runWithInput(servicesWithDemoData(), "7\n  \nq\n");

        assertAll(
                () -> assertContains(output, "问题不能为空。"),
                () -> assertContains(output, "主菜单"));
    }

    @Test
    void blankCommandPromptsAgain() {
        String output = runWithInput(servicesWithDemoData(), "  \nq\n");

        assertContains(output, "请输入命令。");
    }

    @Test
    void helpAndUnknownCommandDisplayHelp() {
        String output = runWithInput(servicesWithDemoData(), "x\nh\nq\n");

        assertAll(
                () -> assertContains(output, "未知命令"),
                () -> assertContains(output, "命令说明"));
    }

    @Test
    void constructorRejectsNullDependencies() {
        ApplicationServices services = servicesWithDemoData();

        assertAll(
                () -> assertNullRejected("services", () -> new ConsoleApplication(null, new StringReader(""), new StringWriter())),
                () -> assertNullRejected("input", () -> new ConsoleApplication(services, null, new StringWriter())),
                () -> assertNullRejected("output", () -> new ConsoleApplication(services, new StringReader(""), null)));
    }

    private static ApplicationServices servicesWithDemoData() {
        ApplicationServices services = new ApplicationFactory().create(
                Map.of(),
                new FixedTimeProvider(LocalDateTime.of(2026, 1, 15, 9, 0)));
        new DemoDataFactory().load(services);
        return services;
    }

    private static ApplicationServices servicesWithoutDemoData() {
        return new ApplicationFactory().create(
                Map.of(),
                new FixedTimeProvider(LocalDateTime.of(2026, 1, 15, 9, 0)));
    }

    private static String runWithInput(ApplicationServices services, String input) {
        StringWriter output = new StringWriter();
        new ConsoleApplication(services, new StringReader(input), output).run();
        return output.toString();
    }

    private static void assertContains(String text, String expected) {
        org.junit.jupiter.api.Assertions.assertTrue(
                text.contains(expected),
                () -> "expected output to contain <" + expected + "> but was:\n" + text);
    }

    private static void assertNullRejected(String expectedMessage, Executable executable) {
        NullPointerException exception = assertThrows(NullPointerException.class, executable::execute);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private interface Executable {
        void execute();
    }
}
