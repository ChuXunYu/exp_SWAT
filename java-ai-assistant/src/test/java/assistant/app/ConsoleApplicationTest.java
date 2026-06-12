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
        String output = runWithInput(servicesWithDemoData(), "2\nb\n3\n4\n5\n6\n8\nq\n");

        assertAll(
                () -> assertContains(output, "任务菜单"),
                () -> assertContains(output, "l/list. 列表"),
                () -> assertContains(output, "日程列表"),
                () -> assertContains(output, "学习计划列表"),
                () -> assertContains(output, "收支统计"),
                () -> assertContains(output, "笔记列表"),
                () -> assertContains(output, "AI 草稿列表"));
    }

    @Test
    void listCommandsDisplayEmptyStateWithoutDemoData() {
        String output = runWithInput(servicesWithoutDemoData(), "2\nl\nb\n3\n4\n5\n6\n8\nq\n");

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

        String output = runWithInput(services, "2\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "task list failed"));
    }

    @Test
    void taskMenuAddsTaskAndSummaryReflectsTodayTaskCount() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n今日任务\n描述\nHIGH\n2026-01-15\nl\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "标题: 今日任务"),
                () -> assertContains(output, "1 | 今日任务 | HIGH | TODO | 截止 2026-01-15"),
                () -> assertContains(output, "今日任务数: 1"));
    }

    @Test
    void taskMenuViewsUpdatesDeletesTask() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n旧标题\n旧描述\nLOW\n2026-01-15\nv\n1\nu\n1\n新标题\n新描述\nMEDIUM\n2026-01-16\n"
                        + "d\n1\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "标题: 旧标题"),
                () -> assertContains(output, "标题: 新标题"),
                () -> assertContains(output, "描述: 新描述"),
                () -> assertContains(output, "操作成功"),
                () -> assertContains(output, "NOT_FOUND"));
    }

    @Test
    void taskMenuCompletesReportsConflictAndReopensTask() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n任务\n描述\nHIGH\n2026-01-15\nc\n1\nc\n1\nr\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "状态: COMPLETED"),
                () -> assertContains(output, "STATE_CONFLICT"),
                () -> assertContains(output, "状态: TODO"));
    }

    @Test
    void taskMenuFiltersByStatusPriorityAndDueDate() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n匹配任务\n描述\nHIGH\n2026-01-15\n"
                        + "a\n非匹配任务\n描述\nLOW\n2026-01-16\n"
                        + "c\n1\nf\nCOMPLETED\nHIGH\n2026-01-15\nb\nq\n");

        String filtered = between(output, "任务筛选结果", "主菜单");
        assertAll(
                () -> assertContains(filtered, "匹配任务"),
                () -> assertNotContains(filtered, "非匹配任务"));
    }

    @Test
    void taskMenuFilterEmptyFieldsListAllTasks() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n第一项\n描述\nHIGH\n2026-01-15\n"
                        + "a\n第二项\n描述\nLOW\n2026-01-16\n"
                        + "f\n\n\n\nb\nq\n");

        String filtered = between(output, "任务筛选结果", "主菜单");
        assertAll(
                () -> assertContains(filtered, "第一项"),
                () -> assertContains(filtered, "第二项"));
    }

    @Test
    void taskMenuRejectsInvalidIdWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n保留任务\n描述\nMEDIUM\n2026-01-15\nv\nabc\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "任务 id 必须是正整数"),
                () -> assertContains(output, "1 | 保留任务 | MEDIUM | TODO | 截止 2026-01-15"));
    }

    @Test
    void taskMenuRejectsInvalidDateWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n坏日期任务\n描述\nHIGH\nbad-date\nl\nb\nq\n");

        String list = between(output, "任务列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "截止日期格式必须是 yyyy-MM-dd"),
                () -> assertNotContains(list, "坏日期任务"));
    }

    @Test
    void taskMenuRejectsInvalidPriorityWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n坏优先级任务\n描述\nURGENT\nl\nb\nq\n");

        String list = between(output, "任务列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "优先级必须是 LOW、MEDIUM 或 HIGH"),
                () -> assertNotContains(list, "坏优先级任务"));
    }

    @Test
    void taskMenuRejectsInvalidStatusWithoutServiceCall() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "2\na\n保留任务\n描述\nMEDIUM\n2026-01-15\nf\nDONE\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "状态必须是 TODO 或 COMPLETED"),
                () -> assertContains(output, "1 | 保留任务 | MEDIUM | TODO | 截止 2026-01-15"));
    }

    @Test
    void taskMenuUnknownHelpBackAndMainMenuContinuation() {
        String output = runWithInput(servicesWithoutDemoData(), "2\n?\nh\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "未知任务命令，请输入 h 查看帮助。"),
                () -> assertContains(output, "任务菜单"),
                () -> assertContains(output, "今日任务数: 0"));
    }

    @Test
    void taskMenuBlankCommandPromptsAgainAndStaysInTaskMenu() {
        String output = runWithInput(servicesWithoutDemoData(), "2\n  \nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "请输入任务命令。"),
                () -> assertContains(output, "任务列表"));
    }

    @Test
    void taskMenuExitsOnEofDuringCommandRead() {
        String output = runWithInput(servicesWithoutDemoData(), "2\n");

        assertContains(output, "任务菜单");
    }

    @Test
    void taskMenuExitsOnEofDuringAddFields() {
        String output = runWithInput(servicesWithoutDemoData(), "2\na\n半成品\n");

        assertAll(
                () -> assertContains(output, "任务菜单"),
                () -> assertNotContains(output, "任务详情"));
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

    private static String between(String text, String startInclusive, String endExclusive) {
        int start = text.indexOf(startInclusive);
        org.junit.jupiter.api.Assertions.assertTrue(
                start >= 0,
                () -> "expected output to contain start <" + startInclusive + "> but was:\n" + text);
        int end = text.indexOf(endExclusive, start);
        org.junit.jupiter.api.Assertions.assertTrue(
                end >= 0,
                () -> "expected output to contain end <" + endExclusive + "> after start but was:\n" + text);
        return text.substring(start, end);
    }

    private static void assertNotContains(String text, String unexpected) {
        org.junit.jupiter.api.Assertions.assertFalse(
                text.contains(unexpected),
                () -> "expected output not to contain <" + unexpected + "> but was:\n" + text);
    }

    private static void assertNullRejected(String expectedMessage, Executable executable) {
        NullPointerException exception = assertThrows(NullPointerException.class, executable::execute);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private interface Executable {
        void execute();
    }
}
