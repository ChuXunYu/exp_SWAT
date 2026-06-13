package assistant.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import assistant.ai.DraftLifecycleService;
import assistant.ai.AiAssistantService;
import assistant.ai.AiClient;
import assistant.ai.AiConfiguration;
import assistant.ai.AiRequest;
import assistant.ai.AiResponse;
import assistant.ai.DraftImportService;
import assistant.ai.InMemorySuggestionDraftRepository;
import assistant.ai.PromptBuilder;
import assistant.ai.StudyPlanDraftContent;
import assistant.ai.StructuredSuggestionDraftService;
import assistant.ai.StructuredSuggestionParser;
import assistant.ai.SuggestionDraftRepository;
import assistant.ai.SuggestionDraftStatus;
import assistant.ai.SuggestionDraftType;
import assistant.ai.SuggestionDraftView;
import assistant.ai.TaskDraftItem;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.MoneyValue;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.common.Tag;
import assistant.common.TransactionAmount;
import assistant.finance.FinanceStatistics;
import assistant.finance.FinanceService;
import assistant.finance.TransactionType;
import assistant.finance.TransactionView;
import assistant.note.NoteQuery;
import assistant.note.NoteService;
import assistant.note.NoteView;
import assistant.schedule.ScheduleService;
import assistant.study.StudyPlanService;
import assistant.testability.FixedTimeProvider;
import assistant.testability.IncrementalIdGenerator;
import assistant.task.TaskPriority;
import assistant.task.TaskService;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        String output = runWithInput(servicesWithDemoData(), "2\nb\n3\nb\n4\nb\n5\nb\n6\nb\n8\nb\nq\n");

        assertAll(
                () -> assertContains(output, "任务菜单"),
                () -> assertContains(output, "l/list. 列表"),
                () -> assertContains(output, "日程菜单"),
                () -> assertContains(output, "学习计划菜单"),
                () -> assertContains(output, "收支菜单"),
                () -> assertContains(output, "笔记菜单"),
                () -> assertContains(output, "AI 草稿菜单"));
    }

    @Test
    void listCommandsDisplayEmptyStateWithoutDemoData() {
        String output = runWithInput(servicesWithoutDemoData(), "2\nl\nb\n3\nl\nb\n4\nl\nb\n5\nl\nb\n6\nl\nb\n8\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "暂无任务"),
                () -> assertContains(output, "暂无日程"),
                () -> assertContains(output, "暂无学习计划"),
                () -> assertContains(output, "暂无收支记录"),
                () -> assertContains(output, "暂无笔记"),
                () -> assertContains(output, "暂无 AI 草稿"));
    }

    @Test
    void scheduleCommandDisplaysFailureCodeAndMessage() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        ScheduleService scheduleService = mock(ScheduleService.class);
        when(scheduleService.listSchedules())
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "schedule list failed"));
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                scheduleService,
                baseServices.studyPlanService(),
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "3\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "schedule list failed"));
    }

    @Test
    void scheduleMenuAddsScheduleAndSummaryReflectsTodayScheduleCount() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n今日会议\n2026-01-15T09:30\n2026-01-15T10:00\n会议室\n备注\nl\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "名称: 今日会议"),
                () -> assertContains(output, "1 | 今日会议 | UPCOMING | 2026-01-15T09:30 ~ 2026-01-15T10:00 | 会议室"),
                () -> assertContains(output, "今日日程数: 1"));
    }

    @Test
    void scheduleMenuViewsUpdatesDeletesSchedule() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n旧日程\n2026-01-15T09:30\n2026-01-15T10:00\n旧地点\n旧备注\n"
                        + "v\n1\nu\n1\n新日程\n2026-01-15T10:00\n2026-01-15T10:30\n新地点\n新备注\n"
                        + "d\n1\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "名称: 旧日程"),
                () -> assertContains(output, "名称: 新日程"),
                () -> assertContains(output, "地点: 新地点"),
                () -> assertContains(output, "备注: 新备注"),
                () -> assertContains(output, "操作成功"),
                () -> assertContains(output, "NOT_FOUND"));
    }

    @Test
    void scheduleMenuRejectsOverlappingScheduleAndAllowsAdjacentSchedule() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n首个日程\n2026-01-15T09:30\n2026-01-15T10:00\nA\nN\n"
                        + "a\n冲突日程\n2026-01-15T09:45\n2026-01-15T10:15\nB\nN\n"
                        + "a\n相邻日程\n2026-01-15T10:00\n2026-01-15T10:30\nC\nN\n"
                        + "l\nb\nq\n");

        String list = between(output, "日程列表", "主菜单");
        assertAll(
                () -> assertContains(output, "SCHEDULE_CONFLICT"),
                () -> assertContains(list, "首个日程"),
                () -> assertContains(list, "相邻日程"),
                () -> assertNotContains(list, "冲突日程"));
    }

    @Test
    void scheduleMenuFiltersByDateAndStatus() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n匹配日程\n2026-01-15T08:30\n2026-01-15T09:30\nA\nN\n"
                        + "a\n非匹配日程\n2026-01-16T10:00\n2026-01-16T10:30\nB\nN\n"
                        + "f\n2026-01-15\nONGOING\nb\nq\n");

        String filtered = between(output, "日程筛选结果", "主菜单");
        assertAll(
                () -> assertContains(filtered, "匹配日程"),
                () -> assertNotContains(filtered, "非匹配日程"));
    }

    @Test
    void scheduleMenuFilterEmptyFieldsListAllSchedules() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n第一日程\n2026-01-15T09:30\n2026-01-15T10:00\nA\nN\n"
                        + "a\n第二日程\n2026-01-15T10:00\n2026-01-15T10:30\nB\nN\n"
                        + "f\n\n\nb\nq\n");

        String filtered = between(output, "日程筛选结果", "主菜单");
        assertAll(
                () -> assertContains(filtered, "第一日程"),
                () -> assertContains(filtered, "第二日程"));
    }

    @Test
    void scheduleMenuAcceptsLongCommandAliasesAndCaseInsensitiveStatus() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\nadd\n别名日程\n2026-01-15T09:30\n2026-01-15T10:00\n旧地点\n旧备注\n"
                        + "view\n1\nupdate\n1\n别名日程已改\n2026-01-15T10:00\n2026-01-15T10:30\n新地点\n新备注\n"
                        + "filter\n2026-01-15\nupcoming\nlist\ndelete\n1\nback\nquit\n");

        String filtered = between(output, "日程筛选结果", "日程列表");
        assertAll(
                () -> assertContains(output, "名称: 别名日程"),
                () -> assertContains(output, "名称: 别名日程已改"),
                () -> assertContains(output, "地点: 新地点"),
                () -> assertContains(output, "备注: 新备注"),
                () -> assertContains(filtered, "别名日程已改"),
                () -> assertContains(output, "操作成功"));
    }

    @Test
    void scheduleMenuRejectsInvalidIdWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n保留日程\n2026-01-15T09:30\n2026-01-15T10:00\nA\nN\nv\nabc\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "日程 id 必须是正整数"),
                () -> assertContains(output, "1 | 保留日程 | UPCOMING | 2026-01-15T09:30 ~ 2026-01-15T10:00 | A"));
    }

    @Test
    void scheduleMenuRejectsInvalidDateTimeWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n坏时间日程\nbad-time\n2026-01-15T10:00\nl\nb\nq\n");

        String list = between(output, "日程列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "日程时间格式必须是 yyyy-MM-ddTHH:mm"),
                () -> assertNotContains(list, "坏时间日程"));
    }

    @Test
    void scheduleMenuRejectsInvalidDateWithoutServiceCall() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n保留日程\n2026-01-15T09:30\n2026-01-15T10:00\nA\nN\nf\nbad-date\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "日程日期格式必须是 yyyy-MM-dd"),
                () -> assertContains(output, "1 | 保留日程 | UPCOMING | 2026-01-15T09:30 ~ 2026-01-15T10:00 | A"));
    }

    @Test
    void scheduleMenuRejectsInvalidDateBeforeCallingScheduleService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        ScheduleService scheduleService = mock(ScheduleService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                scheduleService,
                baseServices.studyPlanService(),
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "3\nf\nbad-date\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "日程日期格式必须是 yyyy-MM-dd"));
        org.mockito.Mockito.verifyNoInteractions(scheduleService);
    }

    @Test
    void scheduleMenuRejectsInvalidStatusWithoutServiceCall() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n保留日程\n2026-01-15T09:30\n2026-01-15T10:00\nA\nN\nf\n\nACTIVE\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "状态必须是 UPCOMING、ONGOING 或 EXPIRED"),
                () -> assertContains(output, "1 | 保留日程 | UPCOMING | 2026-01-15T09:30 ~ 2026-01-15T10:00 | A"));
    }

    @Test
    void scheduleMenuRejectsInvalidStatusBeforeCallingScheduleService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        ScheduleService scheduleService = mock(ScheduleService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                scheduleService,
                baseServices.studyPlanService(),
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "3\nf\n\nACTIVE\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "状态必须是 UPCOMING、ONGOING 或 EXPIRED"));
        org.mockito.Mockito.verifyNoInteractions(scheduleService);
    }

    @Test
    void scheduleMenuRejectsEndNotAfterStartWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "3\na\n坏范围日程\n2026-01-15T10:00\n2026-01-15T10:00\nl\nb\nq\n");

        String list = between(output, "日程列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "结束时间必须晚于开始时间"),
                () -> assertNotContains(list, "坏范围日程"));
    }

    @Test
    void scheduleMenuUnknownHelpBackAndMainMenuContinuation() {
        String output = runWithInput(servicesWithoutDemoData(), "3\n?\nh\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "未知日程命令，请输入 h 查看帮助。"),
                () -> assertContains(output, "日程菜单"),
                () -> assertContains(output, "今日日程数: 0"));
    }

    @Test
    void scheduleMenuBlankCommandPromptsAgainAndStaysInScheduleMenu() {
        String output = runWithInput(servicesWithoutDemoData(), "3\n  \nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "请输入日程命令。"),
                () -> assertContains(output, "日程列表"));
    }

    @Test
    void scheduleMenuExitsOnEofDuringCommandRead() {
        String output = runWithInput(servicesWithoutDemoData(), "3\n");

        assertContains(output, "日程菜单");
    }

    @Test
    void scheduleMenuExitsOnEofDuringAddFields() {
        String output = runWithInput(servicesWithoutDemoData(), "3\na\n半成品日程\n");

        assertAll(
                () -> assertContains(output, "日程菜单"),
                () -> assertNotContains(output, "日程详情"));
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
                baseServices.structuredSuggestionDraftService(),
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
    void studyPlanCommandDisplaysFailureCodeAndMessage() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        StudyPlanService studyPlanService = mock(StudyPlanService.class);
        when(studyPlanService.listStudyPlans())
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "study plan list failed"));
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                studyPlanService,
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "4\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "study plan list failed"));
    }

    @Test
    void studyPlanMenuAddsPlanAndSummaryReflectsWeekStudyPlanCount() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n本周学习\n2026-01-13\n2026-01-18\n8\n25\nl\nv\n1\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "目标: 本周学习"),
                () -> assertContains(output, "1 | 本周学习 | IN_PROGRESS | 进度 25% | 2026-01-13 ~ 2026-01-18 | 预期 8 小时"),
                () -> assertContains(output, "学习计划详情"),
                () -> assertContains(output, "本周学习计划数: 1"));
    }

    @Test
    void studyPlanMenuViewsUpdatesProgressAndDeletesPlan() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n旧学习\n2026-01-13\n2026-01-18\n8\n10\n"
                        + "v\n1\nu\n1\n新学习\n2026-01-14\n2026-01-20\n12\n"
                        + "p\n1\n60\nd\n1\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "目标: 旧学习"),
                () -> assertContains(output, "目标: 新学习"),
                () -> assertContains(output, "截止日期: 2026-01-20"),
                () -> assertContains(output, "预期投入小时数: 12"),
                () -> assertContains(output, "进度: 60%"),
                () -> assertContains(output, "操作成功"),
                () -> assertContains(output, "NOT_FOUND"));
    }

    @Test
    void studyPlanMenuFiltersByStatusAndPeriod() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n匹配学习\n2026-01-13\n2026-01-18\n8\n30\n"
                        + "a\n非匹配学习\n2026-02-01\n2026-02-05\n5\n0\n"
                        + "f\nin_progress\n2026-01-15\n2026-01-16\nb\nq\n");

        String filtered = between(output, "学习计划筛选结果", "主菜单");
        assertAll(
                () -> assertContains(filtered, "匹配学习"),
                () -> assertNotContains(filtered, "非匹配学习"));
    }

    @Test
    void studyPlanMenuFilterEmptyFieldsListAllPlans() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n第一学习\n2026-01-13\n2026-01-18\n8\n\n"
                        + "a\n第二学习\n2026-02-01\n2026-02-05\n5\n\n"
                        + "f\n\n\n\nb\nq\n");

        String filtered = between(output, "学习计划筛选结果", "主菜单");
        assertAll(
                () -> assertContains(filtered, "第一学习"),
                () -> assertContains(filtered, "第二学习"),
                () -> assertContains(filtered, "进度 0%"));
    }

    @Test
    void studyPlanMenuCreatesDefaultInitialProgressWhenBlank() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n默认进度学习\n2026-01-13\n2026-01-18\n8\n\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "目标: 默认进度学习"),
                () -> assertContains(output, "进度: 0%"));
    }

    @Test
    void studyPlanMenuCreatesCompletedPlanWithExplicitProgress100() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n完成学习\n2026-01-13\n2026-01-18\n8\n100\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "进度: 100%"),
                () -> assertContains(output, "状态: COMPLETED"));
    }

    @Test
    void studyPlanMenuAcceptsLongCommandAliasesAndCaseInsensitiveStatus() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\nadd\n别名学习\n2026-01-13\n2026-01-18\n8\n100\n"
                        + "view\n1\nupdate\n1\n别名学习已改\n2026-01-14\n2026-01-20\n10\n"
                        + "progress\n1\n100\nfilter\ncompleted\n2026-01-14\n2026-01-20\nlist\ndelete\n1\nback\nquit\n");

        String filtered = between(output, "学习计划筛选结果", "学习计划列表");
        assertAll(
                () -> assertContains(output, "目标: 别名学习"),
                () -> assertContains(output, "目标: 别名学习已改"),
                () -> assertContains(filtered, "别名学习已改"),
                () -> assertContains(output, "操作成功"));
    }

    @Test
    void studyPlanMenuRejectsInvalidIdWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n保留学习\n2026-01-13\n2026-01-18\n8\n\nv\nabc\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "学习计划 id 必须是正整数"),
                () -> assertContains(output, "1 | 保留学习 | IN_PROGRESS | 进度 0% | 2026-01-13 ~ 2026-01-18 | 预期 8 小时"));
    }

    @Test
    void studyPlanMenuRejectsInvalidDateWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n坏日期学习\nbad-date\n2026-01-18\nl\nb\nq\n");

        String list = between(output, "学习计划列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "学习计划日期格式必须是 yyyy-MM-dd"),
                () -> assertNotContains(list, "坏日期学习"));
    }

    @Test
    void studyPlanMenuRejectsUpdateEndBeforeStartWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n保留学习\n2026-01-13\n2026-01-18\n8\n\n"
                        + "u\n1\n不应修改学习\n2026-01-20\n2026-01-14\n"
                        + "l\nb\nq\n");

        String list = between(output, "学习计划列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "学习计划结束日期不能早于开始日期"),
                () -> assertContains(list, "保留学习"),
                () -> assertNotContains(list, "不应修改学习"));
    }

    @Test
    void studyPlanMenuRejectsAddEndBeforeStartWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n倒置学习\n2026-01-20\n2026-01-14\n8\n\nl\nb\nq\n");

        String list = between(output, "学习计划列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "学习计划结束日期不能早于开始日期"),
                () -> assertNotContains(list, "倒置学习"));
    }

    @Test
    void studyPlanMenuRejectsFilterSingleDateWithoutServiceCall() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        StudyPlanService studyPlanService = mock(StudyPlanService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                studyPlanService,
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "4\nf\n\n2026-01-15\n\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "学习计划开始日期和截止日期必须同时填写或同时为空"));
        org.mockito.Mockito.verifyNoInteractions(studyPlanService);
    }

    @Test
    void studyPlanMenuRejectsFilterEndBeforeStartWithoutServiceCall() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        StudyPlanService studyPlanService = mock(StudyPlanService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                studyPlanService,
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "4\nf\n\n2026-01-18\n2026-01-15\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "学习计划结束日期不能早于开始日期"));
        org.mockito.Mockito.verifyNoInteractions(studyPlanService);
    }

    @Test
    void studyPlanMenuRejectsInvalidHoursWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n坏小时学习\n2026-01-13\n2026-01-18\n0\nl\nb\nq\n");

        String list = between(output, "学习计划列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "预期投入小时数必须是正整数"),
                () -> assertNotContains(list, "坏小时学习"));
    }

    @Test
    void studyPlanMenuRejectsInvalidProgressWithoutWriteOperation() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "4\na\n保留进度\n2026-01-13\n2026-01-18\n8\n10\np\n1\n101\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "进度必须是 0 到 100 的整数"),
                () -> assertContains(output, "进度: 10%"));
    }

    @Test
    void studyPlanMenuRejectsInvalidStatusBeforeCallingStudyPlanService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        StudyPlanService studyPlanService = mock(StudyPlanService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                studyPlanService,
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "4\nf\nACTIVE\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "状态必须是 NOT_STARTED、IN_PROGRESS、COMPLETED 或 OVERDUE_INCOMPLETE"));
        org.mockito.Mockito.verifyNoInteractions(studyPlanService);
    }

    @Test
    void studyPlanMenuUnknownHelpBackAndMainMenuContinuation() {
        String output = runWithInput(servicesWithoutDemoData(), "4\n?\nh\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "未知学习计划命令，请输入 h 查看帮助。"),
                () -> assertContains(output, "学习计划菜单"),
                () -> assertContains(output, "本周学习计划数: 0"));
    }

    @Test
    void studyPlanMenuBlankCommandPromptsAgainAndStaysInStudyPlanMenu() {
        String output = runWithInput(servicesWithoutDemoData(), "4\n  \nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "请输入学习计划命令。"),
                () -> assertContains(output, "学习计划列表"));
    }

    @Test
    void studyPlanMenuExitsOnEofDuringCommandRead() {
        String output = runWithInput(servicesWithoutDemoData(), "4\n");

        assertContains(output, "学习计划菜单");
    }

    @Test
    void studyPlanMenuExitsOnEofDuringAddFields() {
        String output = runWithInput(servicesWithoutDemoData(), "4\na\n半成品学习计划\n");

        assertAll(
                () -> assertContains(output, "学习计划菜单"),
                () -> assertNotContains(output, "学习计划详情"));
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
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "statistics failed"));
    }

    @Test
    void financeMenuAddsIncomeExpenseAndSummaryReflectsBalance() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\ni\n1000.00\n工资\n2026-01-15\n一月工资\n"
                        + "e\n120.50\n餐饮\n2026-01-15\n午餐\n"
                        + "l\ns\n\n\n\n\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "1 | INCOME | 1000.00 | 工资 | 2026-01-15 | 一月工资"),
                () -> assertContains(output, "2 | EXPENSE | 120.50 | 餐饮 | 2026-01-15 | 午餐"),
                () -> assertContains(output, "收入: 1000.00"),
                () -> assertContains(output, "支出: 120.50"),
                () -> assertContains(output, "结余: 879.50"),
                () -> assertContains(output, "本月收入: 1000.00"),
                () -> assertContains(output, "本月支出: 120.50"),
                () -> assertContains(output, "本月结余: 879.50"));
    }

    @Test
    void financeMenuViewsUpdatesAndDeletesTransaction() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\ni\n100.00\n奖金\n2026-01-10\n旧备注\n"
                        + "v\n1\nu\n1\nexpense\n80.25\n交通\n2026-01-11\n新备注\n"
                        + "d\n1\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "类型: INCOME"),
                () -> assertContains(output, "金额: 100.00"),
                () -> assertContains(output, "类型: EXPENSE"),
                () -> assertContains(output, "金额: 80.25"),
                () -> assertContains(output, "类别: 交通"),
                () -> assertContains(output, "日期: 2026-01-11"),
                () -> assertContains(output, "备注: 新备注"),
                () -> assertContains(output, "操作成功"),
                () -> assertContains(output, "NOT_FOUND"));
    }

    @Test
    void financeMenuFiltersByTypeCategoryAndDateRange() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\ni\n300.00\n工资\n2026-01-15\n匹配收入\n"
                        + "e\n50.00\n餐饮\n2026-01-20\n非匹配支出\n"
                        + "f\nincome\n工资\n2026-01-01\n2026-01-31\nb\nq\n");

        String filtered = between(output, "收支筛选结果", "主菜单");
        assertAll(
                () -> assertContains(output, "收支筛选统计"),
                () -> assertContains(output, "收入: 300.00"),
                () -> assertContains(output, "支出: 0.00"),
                () -> assertContains(filtered, "匹配收入"),
                () -> assertNotContains(filtered, "非匹配支出"));
    }

    @Test
    void financeMenuFilterEmptyFieldsListAllTransactions() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\ni\n10.00\nA\n2026-01-10\n第一\n"
                        + "e\n20.00\nB\n2026-02-01\n第二\nf\n\n\n\n\nb\nq\n");

        String filtered = between(output, "收支筛选结果", "主菜单");
        assertAll(
                () -> assertContains(filtered, "第一"),
                () -> assertContains(filtered, "第二"));
    }

    @Test
    void financeMenuStatisticsSupportsEmptyAndFilteredQuery() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\ni\n200.00\n工资\n2026-01-10\n收入\n"
                        + "e\n60.00\n餐饮\n2026-01-15\n支出\n"
                        + "s\n\n\n\n\ns\nexpense\n餐饮\n2026-01-01\n2026-01-31\nb\nq\n");

        assertAll(
                () -> assertContains(output, "收入: 200.00"),
                () -> assertContains(output, "支出: 60.00"),
                () -> assertContains(output, "结余: 140.00"),
                () -> assertContains(output, "收入: 0.00"),
                () -> assertContains(output, "结余: -60.00"));
    }

    @Test
    void financeMenuAcceptsLongCommandAliasesAndCaseInsensitiveType() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\nincome\n55.00\n礼金\n2026-01-15\n别名记录\n"
                        + "view\n1\nupdate\n1\nExPeNsE\n12.00\n礼金\n2026-01-16\n已改\n"
                        + "filter\nexpense\n礼金\n2026-01-01\n2026-01-31\nlist\ndelete\n1\nstatistics\n\n\n\n\nback\nquit\n");

        String filtered = between(output, "收支筛选结果", "收支统计");
        assertAll(
                () -> assertContains(output, "类型: INCOME"),
                () -> assertContains(output, "类型: EXPENSE"),
                () -> assertContains(filtered, "已改"),
                () -> assertContains(output, "操作成功"));
    }

    @Test
    void financeMenuRejectsInvalidIdWithoutWriteOperation() {
        String output = runWithInput(servicesWithoutDemoData(), "5\nv\nabc\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支记录 id 必须是正整数"),
                () -> assertContains(output, "暂无收支记录"));
    }

    @Test
    void financeMenuRejectsInvalidIdBeforeCallingFinanceService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nv\n0\nu\nabc\nd\n1.5\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支记录 id 必须是正整数"));
        verifyNoInteractions(financeService);
    }

    @Test
    void financeMenuRejectsInvalidTypeBeforeCallingFinanceService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nf\ntransfer\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支类型必须是 INCOME 或 EXPENSE"));
        verifyNoInteractions(financeService);
    }

    @Test
    void financeMenuRejectsInvalidUpdateTypeBeforeCallingWriteService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nu\n1\ntransfer\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支类型必须是 INCOME 或 EXPENSE"));
        verify(financeService, never()).updateTransaction(
                any(),
                any(),
                anyString(),
                anyString(),
                any(),
                anyString());
    }

    @Test
    void financeMenuRejectsInvalidDateBeforeWriteOperation() {
        String output = runWithInput(servicesWithoutDemoData(), "5\ni\n10.00\n工资\nbad-date\nl\nb\nq\n");

        String list = between(output, "收支记录列表", "主菜单");
        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支日期格式必须是 yyyy-MM-dd"),
                () -> assertNotContains(list, "工资"));
    }

    @Test
    void financeMenuRejectsInvalidDateBeforeCallingFinanceService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\ni\n10.00\n工资\nbad-date\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支日期格式必须是 yyyy-MM-dd"));
        verifyNoInteractions(financeService);
    }

    @Test
    void financeMenuListsMoreThanTenTransactionsWithoutTruncation() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        List<TransactionView> transactions = java.util.stream.LongStream.rangeClosed(1, 12)
                .mapToObj(id -> transactionView(id, "记录" + id))
                .toList();
        when(financeService.listTransactions()).thenReturn(OperationResult.success(transactions));
        when(financeService.calculateStatistics()).thenReturn(OperationResult.success(statistics("78.00", "0.00")));
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nl\nb\nq\n");

        String list = between(output, "收支记录列表", "主菜单");
        assertAll(
                () -> assertContains(list, "1 | INCOME | 1.00 | 测试 | 2026-01-15 | 记录1"),
                () -> assertContains(list, "10 | INCOME | 10.00 | 测试 | 2026-01-15 | 记录10"),
                () -> assertContains(list, "11 | INCOME | 11.00 | 测试 | 2026-01-15 | 记录11"),
                () -> assertContains(list, "12 | INCOME | 12.00 | 测试 | 2026-01-15 | 记录12"));
    }

    @Test
    void financeMenuRejectsFilterSingleDateWithoutServiceCall() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nf\n\n\n2026-01-01\n\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支开始日期和结束日期必须同时填写或同时为空"));
        verifyNoInteractions(financeService);
    }

    @Test
    void financeMenuRejectsFilterEndBeforeStartWithoutServiceCall() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        FinanceService financeService = mock(FinanceService.class);
        ApplicationServices services = new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                financeService,
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        String output = runWithInput(services, "5\nf\n\n\n2026-01-20\n2026-01-10\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "收支结束日期不能早于开始日期"));
        verifyNoInteractions(financeService);
    }

    @Test
    void financeMenuShowsAmountValidationFailuresFromService() {
        String output = runWithInput(servicesWithoutDemoData(), "5\ni\n0\n工资\n2026-01-15\n坏金额\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "value must be positive"),
                () -> assertContains(output, "暂无收支记录"));
    }

    @Test
    void financeMenuShowsMissingTransactionFailures() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\nv\n99\nu\n99\nincome\n10.00\n工资\n2026-01-15\n无\nd\n99\nb\nq\n");

        assertAll(
                () -> assertContains(output, "NOT_FOUND"),
                () -> assertNotContains(output, "操作成功"));
    }

    @Test
    void financeMenuDeleteRecomputesStatistics() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "5\ni\n100.00\n工资\n2026-01-15\n收入\n"
                        + "e\n30.00\n餐饮\n2026-01-15\n支出\ns\n\n\n\n\nd\n2\ns\n\n\n\n\nb\nq\n");

        assertAll(
                () -> assertContains(output, "支出: 30.00"),
                () -> assertContains(output, "结余: 70.00"),
                () -> assertContains(output, "支出: 0.00"),
                () -> assertContains(output, "结余: 100.00"));
    }

    @Test
    void financeMenuUnknownHelpBackAndMainMenuContinuation() {
        String output = runWithInput(servicesWithoutDemoData(), "5\n?\nh\nb\n1\nq\n");

        assertAll(
                () -> assertContains(output, "未知收支命令，请输入 h 查看帮助。"),
                () -> assertContains(output, "收支菜单"),
                () -> assertContains(output, "今日日程数: 0"));
    }

    @Test
    void financeMenuBlankCommandPromptsAgainAndStaysInFinanceMenu() {
        String output = runWithInput(servicesWithoutDemoData(), "5\n  \nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "请输入收支命令。"),
                () -> assertContains(output, "收支记录列表"));
    }

    @Test
    void financeMenuExitsOnEofDuringCommandRead() {
        String output = runWithInput(servicesWithoutDemoData(), "5\n");

        assertContains(output, "收支菜单");
    }

    @Test
    void financeMenuExitsOnEofDuringIncomeFields() {
        String output = runWithInput(servicesWithoutDemoData(), "5\ni\n100.00\n工资\n");

        assertAll(
                () -> assertContains(output, "收支菜单"),
                () -> assertNotContains(output, "收支记录详情"));
    }

    @Test
    void noteMenuAddsListsViewsUpdatesDeletesNote() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "6\na\n旧笔记\n旧内容\nstudy, life\nl\nv\n1\n"
                        + "u\n1\n新笔记\n新内容\nwork\nd\n1\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "笔记详情"),
                () -> assertContains(output, "标题: 旧笔记"),
                () -> assertContains(output, "1 | 旧笔记 | 2026-01-15 | study, life"),
                () -> assertContains(output, "标题: 新笔记"),
                () -> assertContains(output, "内容: 新内容"),
                () -> assertContains(output, "标签: work"),
                () -> assertContains(output, "操作成功"),
                () -> assertContains(output, "NOT_FOUND"));
    }

    @Test
    void noteMenuSearchesByKeywordCaseInsensitively() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "6\na\nAlpha Plan\n正文\nwork\n"
                        + "a\n普通笔记\n没有匹配\nlife\nk\nalpha\nb\nq\n");

        String results = between(output, "笔记关键字搜索结果", "主菜单");
        assertAll(
                () -> assertContains(results, "Alpha Plan"),
                () -> assertNotContains(results, "普通笔记"));
    }

    @Test
    void noteMenuSearchesByTagUsingNormalizedTagSemantics() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "6\na\n标签笔记\n内容\nStudy\nt\nstudy\nb\nq\n");

        String results = between(output, "笔记标签搜索结果", "主菜单");
        assertAll(
                () -> assertContains(results, "标签笔记"),
                () -> assertContains(results, "study"));
    }

    @Test
    void noteMenuFiltersByKeywordAndTagTogether() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "6\na\n匹配笔记\n包含 Alpha\nstudy\n"
                        + "a\n只有关键字\nAlpha 内容\nlife\n"
                        + "a\n只有标签\n普通内容\nstudy\nf\nalpha\nstudy\nb\nq\n");

        String results = between(output, "笔记筛选结果", "主菜单");
        assertAll(
                () -> assertContains(results, "匹配笔记"),
                () -> assertNotContains(results, "只有关键字"),
                () -> assertNotContains(results, "只有标签"));
    }

    @Test
    void noteMenuCreatesEmptyTagListAndParsesCommaSeparatedTags() {
        String output = runWithInput(
                servicesWithoutDemoData(),
                "6\na\n无标签笔记\n内容\n\n"
                        + "a\n标签笔记\n内容\n Study, , life, study, LIFE \nl\nb\nq\n");

        String list = between(output, "笔记列表", "主菜单");
        assertAll(
                () -> assertContains(list, "1 | 无标签笔记 | 2026-01-15 | "),
                () -> assertContains(list, "2 | 标签笔记 | 2026-01-15 | study, life"));
    }

    @Test
    void noteMenuPassesParsedTagsToNoteServiceWithoutNormalizingCase() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        NoteService noteService = mock(NoteService.class);
        when(noteService.createNote(anyString(), anyString(), any()))
                .thenReturn(OperationResult.success(noteView(1, "新增笔记", "新增内容", "study")));
        when(noteService.updateNote(any(), anyString(), anyString(), any()))
                .thenReturn(OperationResult.success(noteView(7, "修改笔记", "修改内容", "study")));
        ApplicationServices services = withNoteService(baseServices, noteService);

        runWithInput(
                services,
                "6\na\n新增笔记\n新增内容\n Study, , life, study, LIFE \n"
                        + "u\n7\n修改笔记\n修改内容\n Study, , life, study, LIFE \nb\nq\n");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> createdTags = ArgumentCaptor.forClass(Set.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> updatedTags = ArgumentCaptor.forClass(Set.class);
        verify(noteService).createNote(eq("新增笔记"), eq("新增内容"), createdTags.capture());
        verify(noteService).updateNote(eq(new EntityId(7)), eq("修改笔记"), eq("修改内容"), updatedTags.capture());
        assertAll(
                () -> assertEquals(List.of("Study", "life", "study", "LIFE"), List.copyOf(createdTags.getValue())),
                () -> assertEquals(List.of("Study", "life", "study", "LIFE"), List.copyOf(updatedTags.getValue())));
    }

    @Test
    void noteMenuRejectsInvalidIdBeforeCallingNoteService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        NoteService noteService = mock(NoteService.class);
        ApplicationServices services = withNoteService(baseServices, noteService);

        String output = runWithInput(services, "6\nv\nabc\nu\n0\nd\n1.5\nv\n999999999999999999999\nb\nq\n");

        assertAll(
                () -> assertContains(output, "VALIDATION_ERROR"),
                () -> assertContains(output, "笔记 id 必须是正整数"));
        verify(noteService, never()).getNote(any());
        verify(noteService, never()).updateNote(any(), anyString(), anyString(), any());
        verify(noteService, never()).deleteNote(any());
    }

    @Test
    void noteMenuRejectsBlankRequiredKeywordAndTagBeforeCallingService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        NoteService noteService = mock(NoteService.class);
        ApplicationServices services = withNoteService(baseServices, noteService);

        String output = runWithInput(services, "6\nk\n  \nt\n \nb\nq\n");

        assertAll(
                () -> assertContains(output, "关键字不能为空"),
                () -> assertContains(output, "标签不能为空"));
        verify(noteService, never()).searchByKeyword(anyString());
        verify(noteService, never()).searchByTag(anyString());
    }

    @Test
    void noteMenuRejectsEmptyCombinedFilterBeforeCallingService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        NoteService noteService = mock(NoteService.class);
        ApplicationServices services = withNoteService(baseServices, noteService);

        String output = runWithInput(services, "6\nf\n \n\nb\nq\n");

        assertContains(output, "关键字和标签至少填写一个");
        verify(noteService, never()).listNotes(any(NoteQuery.class));
        verify(noteService, never()).searchByKeyword(anyString());
        verify(noteService, never()).searchByTag(anyString());
    }

    @Test
    void noteMenuDisplaysServiceValidationFailuresAndStaysInMenu() {
        String output = runWithInput(servicesWithoutDemoData(), "6\na\n \n内容\nstudy\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "失败: VALIDATION_ERROR"),
                () -> assertContains(output, "title must not be blank"),
                () -> assertContains(output, "笔记列表"),
                () -> assertContains(output, "暂无笔记"));
    }

    @Test
    void noteMenuHandlesUnknownBlankHelpBackAndEof() {
        String output = runWithInput(servicesWithoutDemoData(), "6\n?\n  \nh\nb\n1\nq\n");
        String eofOutput = runWithInput(servicesWithoutDemoData(), "6\n");

        assertAll(
                () -> assertContains(output, "未知笔记命令，请输入 h 查看帮助。"),
                () -> assertContains(output, "请输入笔记命令。"),
                () -> assertContains(output, "笔记菜单"),
                () -> assertContains(output, "今日任务数: 0"),
                () -> assertContains(eofOutput, "笔记菜单"));
    }

    @Test
    void noteMenuListsMoreThanTenNotesWithoutTruncation() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        NoteService noteService = mock(NoteService.class);
        List<NoteView> notes = java.util.stream.LongStream.rangeClosed(1, 12)
                .mapToObj(id -> noteView(id, "笔记" + id, "内容" + id, "tag" + id))
                .toList();
        when(noteService.listNotes()).thenReturn(OperationResult.success(notes));
        ApplicationServices services = withNoteService(baseServices, noteService);

        String output = runWithInput(services, "6\nl\nb\nq\n");

        String list = between(output, "笔记列表", "主菜单");
        assertAll(
                () -> assertContains(list, "1 | 笔记1 | 2026-01-15 | tag1"),
                () -> assertContains(list, "10 | 笔记10 | 2026-01-15 | tag10"),
                () -> assertContains(list, "11 | 笔记11 | 2026-01-15 | tag11"),
                () -> assertContains(list, "12 | 笔记12 | 2026-01-15 | tag12"));
    }

    @Test
    void noteMenuCombinedFilterSingleFieldUsesEquivalentSearch() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        NoteService noteService = mock(NoteService.class);
        when(noteService.searchByKeyword("alpha")).thenReturn(OperationResult.success(List.of()));
        when(noteService.searchByTag("study")).thenReturn(OperationResult.success(List.of()));
        ApplicationServices services = withNoteService(baseServices, noteService);

        runWithInput(services, "6\nf\nalpha\n\nf\n\nstudy\nb\nq\n");

        verify(noteService).searchByKeyword("alpha");
        verify(noteService).searchByTag("study");
        verify(noteService, never()).listNotes(any(NoteQuery.class));
    }

    @Test
    void noteMenuCombinedFilterUsesNoteQueryWhenBothFieldsPresent() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        NoteService noteService = mock(NoteService.class);
        when(noteService.listNotes(eq(NoteQuery.of("alpha", Tag.of("study")))))
                .thenReturn(OperationResult.success(List.of(noteView(1, "匹配", "alpha", "study"))));
        ApplicationServices services = withNoteService(baseServices, noteService);

        String output = runWithInput(services, "6\nf\nalpha\nstudy\nb\nq\n");

        assertContains(output, "笔记筛选结果");
        verify(noteService).listNotes(eq(NoteQuery.of("alpha", Tag.of("study"))));
    }

    @Test
    void draftMenuListsAllDraftsWithoutTruncation() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        List<SuggestionDraftView> drafts = java.util.stream.LongStream.rangeClosed(1, 11)
                .mapToObj(id -> taskDraftView(id, SuggestionDraftStatus.CONFIRMABLE, "草稿" + id))
                .toList();
        when(draftLifecycleService.listDrafts()).thenReturn(OperationResult.success(drafts));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nl\nb\nq\n");

        String list = between(output, "AI 草稿列表", "主菜单");
        assertAll(
                () -> assertContains(list, "1 | TASK_DRAFT | CONFIRMABLE | 任务 1 | 学习计划 false"),
                () -> assertContains(list, "11 | TASK_DRAFT | CONFIRMABLE | 任务 1 | 学习计划 false"));
    }

    @Test
    void draftMenuDisplaysListFailureWithoutListContent() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.listDrafts())
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "draft list failed"));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "失败: VALIDATION_ERROR - draft list failed"),
                () -> assertNotContains(output, "AI 草稿列表"),
                () -> assertNotContains(output, "暂无 AI 草稿"),
                () -> assertNotContains(output, "TASK_DRAFT"));
    }

    @Test
    void draftMenuViewsTaskDraftDetail() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.getDraft(new EntityId(1)))
                .thenReturn(OperationResult.success(taskDraftView(1, SuggestionDraftStatus.CONFIRMABLE, "任务草稿")));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "AI 草稿详情"),
                () -> assertContains(output, "ID: 1"),
                () -> assertContains(output, "类型: TASK_DRAFT"),
                () -> assertContains(output, "状态: CONFIRMABLE"),
                () -> assertContains(output, "标题: 任务草稿"),
                () -> assertContains(output, "优先级: MEDIUM"),
                () -> assertContains(output, "截止日期: 2026-01-20"),
                () -> assertContains(output, "描述: description"),
                () -> assertContains(output, "学习计划草稿: 无"));
    }

    @Test
    void draftMenuViewsStudyPlanDraftDetail() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.getDraft(new EntityId(2)))
                .thenReturn(OperationResult.success(studyPlanDraftView(2, SuggestionDraftStatus.CONFIRMABLE, "学习草稿")));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nv\n2\nb\nq\n");

        assertAll(
                () -> assertContains(output, "任务草稿: 无"),
                () -> assertContains(output, "目标名称: 学习草稿"),
                () -> assertContains(output, "开始日期: 2026-01-15"),
                () -> assertContains(output, "截止日期: 2026-02-15"),
                () -> assertContains(output, "预期小时: 20"),
                () -> assertContains(output, "初始进度: 10%"),
                () -> assertContains(output, "1. 阶段一"),
                () -> assertContains(output, "2. 阶段二"));
    }

    @Test
    void draftMenuDisplaysUnsetDueDateForTaskDraft() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.getDraft(new EntityId(1)))
                .thenReturn(OperationResult.success(taskDraftViewWithoutDueDate(
                        1, SuggestionDraftStatus.CONFIRMABLE, "无截止")));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nv\n1\nb\nq\n");

        assertContains(output, "截止日期: 未设置");
    }

    @Test
    void draftMenuConfirmsDraftAndDisplaysImportedStatus() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.confirmDraft(new EntityId(1)))
                .thenReturn(OperationResult.success(taskDraftView(1, SuggestionDraftStatus.IMPORTED, "已导入")));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nc\n1\nb\nq\n");

        assertAll(
                () -> verify(draftLifecycleService).confirmDraft(new EntityId(1)),
                () -> assertContains(output, "AI 草稿详情"),
                () -> assertContains(output, "状态: IMPORTED"));
    }

    @Test
    void draftMenuCancelsDraftAndDisplaysCancelledStatus() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.cancelDraft(new EntityId(1)))
                .thenReturn(OperationResult.success(taskDraftView(1, SuggestionDraftStatus.CANCELLED, "已取消")));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nx\n1\nb\nq\n");

        assertAll(
                () -> verify(draftLifecycleService).cancelDraft(new EntityId(1)),
                () -> assertContains(output, "AI 草稿详情"),
                () -> assertContains(output, "状态: CANCELLED"));
    }

    @Test
    void draftMenuDisplaysNotFoundAndStateConflictFailuresWithoutDetail() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.getDraft(new EntityId(1)))
                .thenReturn(OperationResult.failure(ErrorCode.NOT_FOUND, "draft missing"));
        when(draftLifecycleService.confirmDraft(new EntityId(2)))
                .thenReturn(OperationResult.failure(ErrorCode.STATE_CONFLICT, "terminal draft"));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nv\n1\nc\n2\nb\nq\n");

        assertAll(
                () -> assertContains(output, "失败: NOT_FOUND - draft missing"),
                () -> assertContains(output, "失败: STATE_CONFLICT - terminal draft"),
                () -> assertNotContains(output, "AI 草稿详情"));
    }

    @Test
    void draftMenuDisplaysImportFailureWithoutOldDetail() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.confirmDraft(new EntityId(1)))
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "import failed"));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nc\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "失败: VALIDATION_ERROR - import failed"),
                () -> assertNotContains(output, "AI 草稿详情"),
                () -> assertNotContains(output, "状态: CONFIRMABLE"));
    }

    @Test
    void draftMenuGeneratesTaskDraftAndListsAndViewsIt() {
        ApplicationServices services = servicesWithStructuredAi(taskDraftJson("整理任务", "2026-01-20"));

        String output = runWithInput(services, "8\ng\n整理明天任务\nl\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "AI 草稿详情"),
                () -> assertContains(output, "AI 草稿列表"),
                () -> assertContains(output, "1 | TASK_DRAFT | CONFIRMABLE | 任务 1 | 学习计划 false"),
                () -> assertContains(output, "标题: 整理任务"),
                () -> assertContains(output, "截止日期: 2026-01-20"));
    }

    @Test
    void draftMenuGeneratesStudyPlanDraftAndDisplaysBreakdown() {
        ApplicationServices services = servicesWithStructuredAi(studyPlanDraftJson("准备考试"));

        String output = runWithInput(services, "8\np\n准备考试\nv\n1\nb\nq\n");

        assertAll(
                () -> assertContains(output, "目标名称: 准备考试"),
                () -> assertContains(output, "1. 复习基础"),
                () -> assertContains(output, "2. 刷题"));
    }

    @Test
    void draftMenuGenerateRejectsBlankGoalWithoutCallingService() {
        ApplicationServices services = servicesWithStructuredAi(taskDraftJson("整理任务", "2026-01-20"));

        String output = runWithInput(services, "8\ng\n  \nl\nb\nq\n");

        assertAll(
                () -> assertContains(output, "目标不能为空。"),
                () -> assertContains(output, "暂无 AI 草稿"));
    }

    @Test
    void draftMenuGenerateFailureDisplaysStableMessage() {
        ApplicationServices services = servicesWithStructuredAi(AiConfiguration.defaultWithoutApiKey(), taskDraftJson("整理任务", "2026-01-20"));

        String output = runWithInput(services, "8\ng\n整理明天任务\nb\nq\n");

        assertContains(output, "失败: AI_NOT_CONFIGURED - DeepSeek API key is not configured");
    }

    @Test
    void generatedTaskDraftCanConfirmCancelAndRejectRepeatConfirm() {
        ApplicationServices services = servicesWithStructuredAi(
                taskDraftJson("确认任务", "2026-01-20"),
                taskDraftJson("取消任务", "2026-01-21"));

        String output = runWithInput(services, "8\ng\n确认\nc\n1\nc\n1\ng\n取消\nx\n2\nb\n2\nl\nb\nq\n");

        String taskList = between(output, "任务列表", "主菜单");
        assertAll(
                () -> assertContains(output, "失败: STATE_CONFLICT - suggestion draft is not confirmable"),
                () -> assertContains(taskList, "确认任务"),
                () -> assertNotContains(taskList, "取消任务"),
                () -> assertContains(output, "状态: CANCELLED"));
    }

    @Test
    void draftMenuRejectsInvalidIdBeforeCallingDraftLifecycleService() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        String output = runWithInput(services, "8\nv\n\nc\nabc\nx\n1.5\nv\n0\nc\n9223372036854775808\nb\nq\n");

        assertAll(
                () -> assertContains(output, "失败: VALIDATION_ERROR - AI 草稿 id 必须是正整数"),
                () -> verify(draftLifecycleService, never()).getDraft(any(EntityId.class)),
                () -> verify(draftLifecycleService, never()).confirmDraft(any(EntityId.class)),
                () -> verify(draftLifecycleService, never()).cancelDraft(any(EntityId.class)),
                () -> verify(draftLifecycleService, never()).listDrafts());
    }

    @Test
    void draftMenuHandlesUnknownBlankHelpBackAndEof() {
        String output = runWithInput(servicesWithoutDemoData(), "8\n?\n  \nh\nb\n1\nq\n");
        String eofOutput = runWithInput(servicesWithoutDemoData(), "8\n");

        assertAll(
                () -> assertContains(output, "未知 AI 草稿命令，请输入 h 查看帮助。"),
                () -> assertContains(output, "请输入 AI 草稿命令。"),
                () -> assertContains(output, "AI 草稿菜单"),
                () -> assertContains(output, "今日任务数: 0"),
                () -> assertContains(eofOutput, "AI 草稿菜单"));
    }

    @Test
    void draftMenuAcceptsLongCommandAliases() {
        ApplicationServices baseServices = servicesWithoutDemoData();
        DraftLifecycleService draftLifecycleService = mock(DraftLifecycleService.class);
        when(draftLifecycleService.listDrafts()).thenReturn(OperationResult.success(List.of()));
        when(draftLifecycleService.getDraft(new EntityId(1)))
                .thenReturn(OperationResult.success(taskDraftView(1, SuggestionDraftStatus.CONFIRMABLE, "查看")));
        when(draftLifecycleService.confirmDraft(new EntityId(1)))
                .thenReturn(OperationResult.success(taskDraftView(1, SuggestionDraftStatus.IMPORTED, "确认")));
        when(draftLifecycleService.cancelDraft(new EntityId(1)))
                .thenReturn(OperationResult.success(taskDraftView(1, SuggestionDraftStatus.CANCELLED, "取消")));
        ApplicationServices services = withDraftLifecycleService(baseServices, draftLifecycleService);

        runWithInput(services, "8\nlist\nview\n1\nconfirm\n1\ncancel\n1\nback\nq\n");

        assertAll(
                () -> verify(draftLifecycleService).listDrafts(),
                () -> verify(draftLifecycleService).getDraft(new EntityId(1)),
                () -> verify(draftLifecycleService).confirmDraft(new EntityId(1)),
                () -> verify(draftLifecycleService).cancelDraft(new EntityId(1)));
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

    private static ApplicationServices servicesWithStructuredAi(String... responses) {
        return servicesWithStructuredAi(configuredAi(), responses);
    }

    private static ApplicationServices servicesWithStructuredAi(AiConfiguration configuration, String... responses) {
        ApplicationServices baseServices = servicesWithoutDemoData();
        SuggestionDraftRepository draftRepository = new InMemorySuggestionDraftRepository();
        IncrementalIdGenerator idGenerator = new IncrementalIdGenerator();
        AiAssistantService aiAssistantService = new AiAssistantService(
                configuration,
                baseServices.summaryService()::buildLocalContext,
                new PromptBuilder(),
                new QueueAiClient(responses));
        StructuredSuggestionDraftService structuredSuggestionDraftService = new StructuredSuggestionDraftService(
                aiAssistantService,
                new StructuredSuggestionParser(),
                draftRepository,
                idGenerator);
        DraftLifecycleService draftLifecycleService = new DraftLifecycleService(
                draftRepository,
                new DraftImportService(baseServices.taskService(), baseServices.studyPlanService()));

        return new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                aiAssistantService,
                structuredSuggestionDraftService,
                draftLifecycleService,
                baseServices.timeProvider());
    }

    private static String runWithInput(ApplicationServices services, String input) {
        StringWriter output = new StringWriter();
        new ConsoleApplication(services, new StringReader(input), output).run();
        return output.toString();
    }

    private static TransactionView transactionView(long id, String note) {
        return new TransactionView(
                new EntityId(id),
                TransactionType.INCOME,
                TransactionAmount.of(id + ".00"),
                "测试",
                LocalDate.of(2026, 1, 15),
                note);
    }

    private static NoteView noteView(long id, String title, String content, String... tags) {
        return new NoteView(
                new EntityId(id),
                title,
                content,
                LocalDate.of(2026, 1, 15),
                tagSet(tags));
    }

    private static SuggestionDraftView taskDraftView(long id, SuggestionDraftStatus status, String title) {
        return new SuggestionDraftView(
                new EntityId(id),
                SuggestionDraftType.TASK_DRAFT,
                status,
                List.of(taskDraftItem(title, LocalDate.of(2026, 1, 20))),
                Optional.empty());
    }

    private static SuggestionDraftView taskDraftViewWithoutDueDate(
            long id, SuggestionDraftStatus status, String title) {
        return new SuggestionDraftView(
                new EntityId(id),
                SuggestionDraftType.TASK_DRAFT,
                status,
                List.of(taskDraftItem(title, null)),
                Optional.empty());
    }

    private static SuggestionDraftView studyPlanDraftView(long id, SuggestionDraftStatus status, String goalName) {
        return new SuggestionDraftView(
                new EntityId(id),
                SuggestionDraftType.STUDY_PLAN_DRAFT,
                status,
                List.of(),
                Optional.of(studyPlanDraftContent(goalName)));
    }

    private static TaskDraftItem taskDraftItem(String title, LocalDate dueDate) {
        return new TaskDraftItem(title, "description", TaskPriority.MEDIUM, dueDate);
    }

    private static AiConfiguration configuredAi() {
        return new AiConfiguration("https://api.example.com", "/chat", "model-a", "test-key", Duration.ofSeconds(5));
    }

    private static String taskDraftJson(String title, String dueDate) {
        return """
                {"type":"TASK_DRAFT","tasks":[{"title":"%s","description":"description","priority":"MEDIUM","dueDate":"%s"}]}
                """.formatted(title, dueDate);
    }

    private static String studyPlanDraftJson(String goalName) {
        return """
                {"type":"STUDY_PLAN_DRAFT","studyPlan":{"goalName":"%s","startDate":"2026-01-15",
                "endDate":"2026-02-15","expectedHours":20,"initialProgress":10,
                "breakdown":["复习基础","刷题"]}}
                """.formatted(goalName);
    }

    private static StudyPlanDraftContent studyPlanDraftContent(String goalName) {
        return new StudyPlanDraftContent(
                goalName,
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 2, 15),
                20,
                Progress.of(10),
                List.of("阶段一", "阶段二"));
    }

    private static LinkedHashSet<Tag> tagSet(String... values) {
        LinkedHashSet<Tag> tags = new LinkedHashSet<>();
        for (String value : values) {
            tags.add(Tag.of(value));
        }
        return tags;
    }

    private static ApplicationServices withNoteService(ApplicationServices baseServices, NoteService noteService) {
        return new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                baseServices.financeService(),
                noteService,
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());
    }

    private static ApplicationServices withDraftLifecycleService(
            ApplicationServices baseServices, DraftLifecycleService draftLifecycleService) {
        return new ApplicationServices(
                baseServices.taskService(),
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.structuredSuggestionDraftService(),
                draftLifecycleService,
                baseServices.timeProvider());
    }

    private static FinanceStatistics statistics(String income, String expense) {
        return FinanceStatistics.of(MoneyValue.of(income), MoneyValue.of(expense));
    }

    private static final class QueueAiClient implements AiClient {
        private final ArrayDeque<String> responses;

        private QueueAiClient(String... responses) {
            this.responses = new ArrayDeque<>(List.of(responses));
        }

        @Override
        public OperationResult<AiResponse> chat(AiRequest request) {
            String response = responses.isEmpty() ? "" : responses.removeFirst();
            return OperationResult.success(new AiResponse(response));
        }
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
