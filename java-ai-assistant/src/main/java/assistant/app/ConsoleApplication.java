package assistant.app;

import assistant.ai.AiScenario;
import assistant.ai.SuggestionDraftView;
import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import assistant.common.OperationResult;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionView;
import assistant.note.NoteView;
import assistant.schedule.ScheduleQuery;
import assistant.schedule.ScheduleStatus;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanView;
import assistant.summary.DashboardSummary;
import assistant.task.TaskPriority;
import assistant.task.TaskQuery;
import assistant.task.TaskStatus;
import assistant.task.TaskView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ConsoleApplication {
    private final ApplicationServices services;
    private final BufferedReader input;
    private final PrintWriter output;
    private boolean running;

    public ConsoleApplication(ApplicationServices services, Reader input, Writer output) {
        this.services = Objects.requireNonNull(services, "services");
        this.input = new BufferedReader(Objects.requireNonNull(input, "input"));
        this.output = new PrintWriter(Objects.requireNonNull(output, "output"), true);
    }

    public void run() {
        running = true;
        printWelcome();
        printMainMenu();
        while (running) {
            String command = readLine("请输入命令: ");
            dispatch(command);
            output.flush();
            if (running) {
                printMainMenu();
            }
        }
    }

    private void printWelcome() {
        output.println("Java AI Assistant");
    }

    private void printMainMenu() {
        output.println();
        output.println("主菜单");
        output.println("1. 汇总");
        output.println("2. 任务");
        output.println("3. 日程");
        output.println("4. 学习计划");
        output.println("5. 收支");
        output.println("6. 笔记");
        output.println("7. AI 问答");
        output.println("8. AI 草稿");
        output.println("h. 帮助");
        output.println("q. 退出");
    }

    private void dispatch(String rawCommand) {
        if (rawCommand == null) {
            running = false;
            return;
        }
        String command = rawCommand.strip().toLowerCase();
        if (command.isEmpty()) {
            output.println("请输入命令。");
            return;
        }
        switch (command) {
            case "1" -> showSummary();
            case "2" -> runTaskMenu();
            case "3" -> runScheduleMenu();
            case "4" -> showStudyPlans();
            case "5" -> showTransactions();
            case "6" -> showNotes();
            case "7" -> askAi();
            case "8" -> showDrafts();
            case "h", "help" -> printHelp();
            case "q", "quit", "exit" -> stop();
            default -> {
                output.println("未知命令，请输入 h 查看帮助。");
                printHelp();
            }
        }
    }

    private void showSummary() {
        OperationResult<DashboardSummary> result = services.summaryService().getDashboardSummary();
        if (!printResult(result)) {
            return;
        }
        DashboardSummary summary = result.getPayload();
        FinanceStatistics statistics = summary.monthFinanceStatistics();
        output.println("今日: " + summary.today());
        output.println("今日任务数: " + summary.todayTasks().size());
        output.println("今日日程数: " + summary.todaySchedules().size());
        output.println("本周学习计划数: " + summary.weekStudyPlans().size());
        output.println("本月收入: " + statistics.totalIncome().toPlainString());
        output.println("本月支出: " + statistics.totalExpense().toPlainString());
        output.println("本月结余: " + statistics.balance().toPlainString());
        output.println("笔记数: " + summary.noteCount());
        output.println("标签数: " + summary.noteTagDistribution().size());
    }

    private void runTaskMenu() {
        printTaskMenu();
        boolean inTaskMenu = true;
        while (running && inTaskMenu) {
            String command = readLine("请输入任务命令: ");
            if (command == null) {
                running = false;
                return;
            }
            inTaskMenu = dispatchTaskCommand(command);
            output.flush();
        }
    }

    private void printTaskMenu() {
        output.println();
        output.println("任务菜单");
        output.println("l/list. 列表");
        output.println("a/add. 新增");
        output.println("v/view. 查看");
        output.println("f/filter. 筛选");
        output.println("u/update. 修改");
        output.println("c/complete. 标记完成");
        output.println("r/reopen. 撤销完成");
        output.println("d/delete. 删除");
        output.println("b/back. 返回主菜单");
        output.println("h/help. 帮助");
    }

    private boolean dispatchTaskCommand(String rawCommand) {
        String command = rawCommand.strip().toLowerCase(Locale.ROOT);
        if (command.isEmpty()) {
            output.println("请输入任务命令。");
            return true;
        }
        switch (command) {
            case "l", "list" -> listTasks();
            case "a", "add" -> addTask();
            case "v", "view" -> viewTask();
            case "f", "filter" -> filterTasks();
            case "u", "update" -> updateTask();
            case "c", "complete" -> completeTask();
            case "r", "reopen" -> reopenTask();
            case "d", "delete" -> deleteTask();
            case "b", "back" -> {
                return false;
            }
            case "h", "help" -> printTaskMenu();
            default -> {
                output.println("未知任务命令，请输入 h 查看帮助。");
                printTaskMenu();
            }
        }
        return running;
    }

    private void listTasks() {
        OperationResult<List<TaskView>> result = services.taskService().listTasks();
        if (!printResult(result)) {
            return;
        }
        printTaskList("任务列表", result.getPayload());
    }

    private void addTask() {
        String title = readTaskRawField("标题: ");
        if (title == null) {
            return;
        }
        String description = readTaskRawField("描述: ");
        if (description == null) {
            return;
        }
        ParsedInput<TaskPriority> priority = readRequiredTaskPriority("优先级(LOW/MEDIUM/HIGH): ");
        if (!priority.hasValue()) {
            return;
        }
        ParsedInput<LocalDate> dueDate = readRequiredTaskDueDate("截止日期(yyyy-MM-dd): ");
        if (!dueDate.hasValue()) {
            return;
        }
        printTaskResult(services.taskService().createTask(title, description, priority.value(), dueDate.value()));
    }

    private void viewTask() {
        ParsedInput<EntityId> id = readTaskId("任务 id: ");
        if (!id.hasValue()) {
            return;
        }
        printTaskResult(services.taskService().getTask(id.value()));
    }

    private void filterTasks() {
        ParsedInput<TaskStatus> status = readOptionalTaskStatus("状态(TODO/COMPLETED，可空): ");
        if (status.isInvalid() || status.isEof()) {
            return;
        }
        ParsedInput<TaskPriority> priority = readOptionalTaskPriority("优先级(LOW/MEDIUM/HIGH，可空): ");
        if (priority.isInvalid() || priority.isEof()) {
            return;
        }
        ParsedInput<LocalDate> dueDate = readOptionalTaskDueDate("截止日期(yyyy-MM-dd，可空): ");
        if (dueDate.isInvalid() || dueDate.isEof()) {
            return;
        }
        TaskQuery query = TaskQuery.of(
                status.hasValue() ? status.value() : null,
                priority.hasValue() ? priority.value() : null,
                dueDate.hasValue() ? dueDate.value() : null);
        OperationResult<List<TaskView>> result = services.taskService().listTasks(query);
        if (!printResult(result)) {
            return;
        }
        printTaskList("任务筛选结果", result.getPayload());
    }

    private void updateTask() {
        ParsedInput<EntityId> id = readTaskId("任务 id: ");
        if (!id.hasValue()) {
            return;
        }
        String title = readTaskRawField("标题: ");
        if (title == null) {
            return;
        }
        String description = readTaskRawField("描述: ");
        if (description == null) {
            return;
        }
        ParsedInput<TaskPriority> priority = readRequiredTaskPriority("优先级(LOW/MEDIUM/HIGH): ");
        if (!priority.hasValue()) {
            return;
        }
        ParsedInput<LocalDate> dueDate = readRequiredTaskDueDate("截止日期(yyyy-MM-dd): ");
        if (!dueDate.hasValue()) {
            return;
        }
        printTaskResult(services.taskService().updateTask(
                id.value(), title, description, priority.value(), dueDate.value()));
    }

    private void completeTask() {
        ParsedInput<EntityId> id = readTaskId("任务 id: ");
        if (!id.hasValue()) {
            return;
        }
        printTaskResult(services.taskService().markTaskCompleted(id.value()));
    }

    private void reopenTask() {
        ParsedInput<EntityId> id = readTaskId("任务 id: ");
        if (!id.hasValue()) {
            return;
        }
        printTaskResult(services.taskService().reopenTask(id.value()));
    }

    private void deleteTask() {
        ParsedInput<EntityId> id = readTaskId("任务 id: ");
        if (!id.hasValue()) {
            return;
        }
        printResult(services.taskService().deleteTask(id.value()));
    }

    private void printTaskResult(OperationResult<TaskView> result) {
        if (!printResult(result)) {
            return;
        }
        printTaskDetail(result.getPayload());
    }

    private void printTaskList(String heading, List<TaskView> tasks) {
        output.println(heading);
        if (tasks.isEmpty()) {
            output.println("暂无任务");
            return;
        }
        tasks.forEach(task -> output.println(task.id().value()
                + " | " + task.title()
                + " | " + task.priority()
                + " | " + task.status()
                + " | 截止 " + task.dueDate()));
    }

    private void printTaskDetail(TaskView task) {
        output.println("任务详情");
        output.println("ID: " + task.id().value());
        output.println("标题: " + task.title());
        output.println("优先级: " + task.priority());
        output.println("状态: " + task.status());
        output.println("截止日期: " + task.dueDate());
        output.println("描述: " + task.description());
    }

    private String readTaskRawField(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
        }
        return value;
    }

    private ParsedInput<EntityId> readTaskId(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        EntityId id = parseTaskId(value);
        return id == null ? ParsedInput.invalid() : ParsedInput.value(id);
    }

    private ParsedInput<TaskPriority> readRequiredTaskPriority(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        TaskPriority priority = parseTaskPriority(value);
        return priority == null ? ParsedInput.invalid() : ParsedInput.value(priority);
    }

    private ParsedInput<LocalDate> readRequiredTaskDueDate(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        LocalDate dueDate = parseTaskDueDate(value);
        return dueDate == null ? ParsedInput.invalid() : ParsedInput.value(dueDate);
    }

    private ParsedInput<TaskStatus> readOptionalTaskStatus(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        if (value.isBlank()) {
            return ParsedInput.empty();
        }
        TaskStatus status = parseTaskStatus(value);
        return status == null ? ParsedInput.invalid() : ParsedInput.value(status);
    }

    private ParsedInput<TaskPriority> readOptionalTaskPriority(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        if (value.isBlank()) {
            return ParsedInput.empty();
        }
        TaskPriority priority = parseTaskPriority(value);
        return priority == null ? ParsedInput.invalid() : ParsedInput.value(priority);
    }

    private ParsedInput<LocalDate> readOptionalTaskDueDate(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        if (value.isBlank()) {
            return ParsedInput.empty();
        }
        LocalDate dueDate = parseTaskDueDate(value);
        return dueDate == null ? ParsedInput.invalid() : ParsedInput.value(dueDate);
    }

    private EntityId parseTaskId(String rawValue) {
        try {
            long value = Long.parseLong(rawValue.strip());
            if (value <= 0) {
                printValidationError("任务 id 必须是正整数");
                return null;
            }
            return new EntityId(value);
        } catch (NumberFormatException exception) {
            printValidationError("任务 id 必须是正整数");
            return null;
        }
    }

    private TaskPriority parseTaskPriority(String rawValue) {
        try {
            return TaskPriority.valueOf(rawValue.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            printValidationError("优先级必须是 LOW、MEDIUM 或 HIGH");
            return null;
        }
    }

    private TaskStatus parseTaskStatus(String rawValue) {
        try {
            return TaskStatus.valueOf(rawValue.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            printValidationError("状态必须是 TODO 或 COMPLETED");
            return null;
        }
    }

    private LocalDate parseTaskDueDate(String rawValue) {
        try {
            return LocalDate.parse(rawValue.strip());
        } catch (DateTimeParseException exception) {
            printValidationError("截止日期格式必须是 yyyy-MM-dd");
            return null;
        }
    }

    private void printValidationError(String message) {
        output.println("失败: VALIDATION_ERROR - " + message);
    }

    private void runScheduleMenu() {
        printScheduleMenu();
        boolean inScheduleMenu = true;
        while (running && inScheduleMenu) {
            String command = readLine("请输入日程命令: ");
            if (command == null) {
                running = false;
                return;
            }
            inScheduleMenu = dispatchScheduleCommand(command);
            output.flush();
        }
    }

    private void printScheduleMenu() {
        output.println();
        output.println("日程菜单");
        output.println("l/list. 列表");
        output.println("a/add. 新增");
        output.println("v/view. 查看");
        output.println("f/filter. 筛选");
        output.println("u/update. 修改");
        output.println("d/delete. 删除");
        output.println("b/back. 返回主菜单");
        output.println("h/help. 帮助");
    }

    private boolean dispatchScheduleCommand(String rawCommand) {
        String command = rawCommand.strip().toLowerCase(Locale.ROOT);
        if (command.isEmpty()) {
            output.println("请输入日程命令。");
            return true;
        }
        switch (command) {
            case "l", "list" -> listSchedules();
            case "a", "add" -> addSchedule();
            case "v", "view" -> viewSchedule();
            case "f", "filter" -> filterSchedules();
            case "u", "update" -> updateSchedule();
            case "d", "delete" -> deleteSchedule();
            case "b", "back" -> {
                return false;
            }
            case "h", "help" -> printScheduleMenu();
            default -> {
                output.println("未知日程命令，请输入 h 查看帮助。");
                printScheduleMenu();
            }
        }
        return running;
    }

    private void listSchedules() {
        OperationResult<List<ScheduleView>> result = services.scheduleService().listSchedules();
        if (!printResult(result)) {
            return;
        }
        printScheduleList("日程列表", result.getPayload());
    }

    private void addSchedule() {
        String name = readScheduleRawField("名称: ");
        if (name == null) {
            return;
        }
        ParsedInput<DateTimeRange> timeRange = readScheduleTimeRange(
                "开始时间(yyyy-MM-ddTHH:mm): ",
                "结束时间(yyyy-MM-ddTHH:mm): ");
        if (!timeRange.hasValue()) {
            return;
        }
        String location = readScheduleRawField("地点: ");
        if (location == null) {
            return;
        }
        String note = readScheduleRawField("备注: ");
        if (note == null) {
            return;
        }
        printScheduleResult(services.scheduleService().createSchedule(name, timeRange.value(), location, note));
    }

    private void viewSchedule() {
        ParsedInput<EntityId> id = readScheduleId("日程 id: ");
        if (!id.hasValue()) {
            return;
        }
        printScheduleResult(services.scheduleService().getSchedule(id.value()));
    }

    private void filterSchedules() {
        ParsedInput<LocalDate> date = readOptionalScheduleDate("日期(yyyy-MM-dd，可空): ");
        if (date.isInvalid() || date.isEof()) {
            return;
        }
        ParsedInput<ScheduleStatus> status = readOptionalScheduleStatus("状态(UPCOMING/ONGOING/EXPIRED，可空): ");
        if (status.isInvalid() || status.isEof()) {
            return;
        }
        ScheduleQuery query = ScheduleQuery.of(
                date.hasValue() ? date.value() : null,
                status.hasValue() ? status.value() : null);
        OperationResult<List<ScheduleView>> result = services.scheduleService().listSchedules(query);
        if (!printResult(result)) {
            return;
        }
        printScheduleList("日程筛选结果", result.getPayload());
    }

    private void updateSchedule() {
        ParsedInput<EntityId> id = readScheduleId("日程 id: ");
        if (!id.hasValue()) {
            return;
        }
        String name = readScheduleRawField("名称: ");
        if (name == null) {
            return;
        }
        ParsedInput<DateTimeRange> timeRange = readScheduleTimeRange(
                "开始时间(yyyy-MM-ddTHH:mm): ",
                "结束时间(yyyy-MM-ddTHH:mm): ");
        if (!timeRange.hasValue()) {
            return;
        }
        String location = readScheduleRawField("地点: ");
        if (location == null) {
            return;
        }
        String note = readScheduleRawField("备注: ");
        if (note == null) {
            return;
        }
        printScheduleResult(services.scheduleService().updateSchedule(
                id.value(), name, timeRange.value(), location, note));
    }

    private void deleteSchedule() {
        ParsedInput<EntityId> id = readScheduleId("日程 id: ");
        if (!id.hasValue()) {
            return;
        }
        printResult(services.scheduleService().deleteSchedule(id.value()));
    }

    private void printScheduleResult(OperationResult<ScheduleView> result) {
        if (!printResult(result)) {
            return;
        }
        printScheduleDetail(result.getPayload());
    }

    private void printScheduleList(String heading, List<ScheduleView> schedules) {
        output.println(heading);
        if (schedules.isEmpty()) {
            output.println("暂无日程");
            return;
        }
        schedules.forEach(schedule -> output.println(schedule.id().value()
                + " | " + schedule.name()
                + " | " + schedule.status()
                + " | " + schedule.startDateTime()
                + " ~ " + schedule.endDateTime()
                + " | " + schedule.location()));
    }

    private void printScheduleDetail(ScheduleView schedule) {
        output.println("日程详情");
        output.println("ID: " + schedule.id().value());
        output.println("名称: " + schedule.name());
        output.println("状态: " + schedule.status());
        output.println("开始时间: " + schedule.startDateTime());
        output.println("结束时间: " + schedule.endDateTime());
        output.println("地点: " + schedule.location());
        output.println("备注: " + schedule.note());
    }

    private String readScheduleRawField(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
        }
        return value;
    }

    private ParsedInput<EntityId> readScheduleId(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        EntityId id = parseScheduleId(value);
        return id == null ? ParsedInput.invalid() : ParsedInput.value(id);
    }

    private ParsedInput<LocalDateTime> readRequiredScheduleDateTime(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        LocalDateTime dateTime = parseScheduleDateTime(value);
        return dateTime == null ? ParsedInput.invalid() : ParsedInput.value(dateTime);
    }

    private ParsedInput<LocalDate> readOptionalScheduleDate(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        if (value.isBlank()) {
            return ParsedInput.empty();
        }
        LocalDate date = parseScheduleDate(value);
        return date == null ? ParsedInput.invalid() : ParsedInput.value(date);
    }

    private ParsedInput<ScheduleStatus> readOptionalScheduleStatus(String prompt) {
        String value = readLine(prompt);
        if (value == null) {
            running = false;
            return ParsedInput.eof();
        }
        if (value.isBlank()) {
            return ParsedInput.empty();
        }
        ScheduleStatus status = parseScheduleStatus(value);
        return status == null ? ParsedInput.invalid() : ParsedInput.value(status);
    }

    private ParsedInput<DateTimeRange> readScheduleTimeRange(String startPrompt, String endPrompt) {
        ParsedInput<LocalDateTime> start = readRequiredScheduleDateTime(startPrompt);
        if (!start.hasValue()) {
            return start.isEof() ? ParsedInput.eof() : ParsedInput.invalid();
        }
        ParsedInput<LocalDateTime> end = readRequiredScheduleDateTime(endPrompt);
        if (!end.hasValue()) {
            return end.isEof() ? ParsedInput.eof() : ParsedInput.invalid();
        }
        try {
            return ParsedInput.value(new DateTimeRange(start.value(), end.value()));
        } catch (IllegalArgumentException exception) {
            printValidationError("结束时间必须晚于开始时间");
            return ParsedInput.invalid();
        }
    }

    private EntityId parseScheduleId(String rawValue) {
        try {
            long value = Long.parseLong(rawValue.strip());
            if (value <= 0) {
                printValidationError("日程 id 必须是正整数");
                return null;
            }
            return new EntityId(value);
        } catch (NumberFormatException exception) {
            printValidationError("日程 id 必须是正整数");
            return null;
        }
    }

    private LocalDateTime parseScheduleDateTime(String rawValue) {
        try {
            return LocalDateTime.parse(rawValue.strip());
        } catch (DateTimeParseException exception) {
            printValidationError("日程时间格式必须是 yyyy-MM-ddTHH:mm");
            return null;
        }
    }

    private LocalDate parseScheduleDate(String rawValue) {
        try {
            return LocalDate.parse(rawValue.strip());
        } catch (DateTimeParseException exception) {
            printValidationError("日程日期格式必须是 yyyy-MM-dd");
            return null;
        }
    }

    private ScheduleStatus parseScheduleStatus(String rawValue) {
        try {
            return ScheduleStatus.valueOf(rawValue.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            printValidationError("状态必须是 UPCOMING、ONGOING 或 EXPIRED");
            return null;
        }
    }

    private void showStudyPlans() {
        OperationResult<List<StudyPlanView>> result = services.studyPlanService().listStudyPlans();
        if (!printResult(result)) {
            return;
        }
        List<StudyPlanView> plans = result.getPayload();
        output.println("学习计划列表");
        if (plans.isEmpty()) {
            output.println("暂无学习计划");
            return;
        }
        plans.stream().limit(10).forEach(plan -> output.println(plan.id().value()
                + " | " + plan.goalName()
                + " | " + plan.status()
                + " | 进度 " + plan.progress().value() + "%"
                + " | " + plan.startDate()
                + " ~ " + plan.endDate()));
    }

    private void showTransactions() {
        OperationResult<List<TransactionView>> transactionsResult = services.financeService().listTransactions();
        if (!printResult(transactionsResult)) {
            return;
        }
        OperationResult<FinanceStatistics> statisticsResult = services.financeService().calculateStatistics();
        if (!printResult(statisticsResult)) {
            return;
        }
        FinanceStatistics statistics = statisticsResult.getPayload();
        output.println("收支统计");
        output.println("收入: " + statistics.totalIncome().toPlainString());
        output.println("支出: " + statistics.totalExpense().toPlainString());
        output.println("结余: " + statistics.balance().toPlainString());
        List<TransactionView> transactions = transactionsResult.getPayload();
        if (transactions.isEmpty()) {
            output.println("暂无收支记录");
            return;
        }
        transactions.stream().limit(10).forEach(transaction -> output.println(transaction.id().value()
                + " | " + transaction.type()
                + " | " + transaction.amount().value().toPlainString()
                + " | " + transaction.category()
                + " | " + transaction.date()));
    }

    private void showNotes() {
        OperationResult<List<NoteView>> result = services.noteService().listNotes();
        if (!printResult(result)) {
            return;
        }
        List<NoteView> notes = result.getPayload();
        output.println("笔记列表");
        if (notes.isEmpty()) {
            output.println("暂无笔记");
            return;
        }
        notes.stream().limit(10).forEach(note -> output.println(note.id().value()
                + " | " + note.title()
                + " | " + note.createdDate()
                + " | " + note.tags()));
    }

    private void askAi() {
        String question = readLine("请输入问题: ");
        if (question == null) {
            running = false;
            return;
        }
        if (question.isBlank()) {
            output.println("问题不能为空。");
            return;
        }
        OperationResult<String> result = services.aiAssistantService().ask(AiScenario.GENERAL_QA, question);
        if (!printResult(result)) {
            return;
        }
        output.println(result.getPayload());
    }

    private void showDrafts() {
        OperationResult<List<SuggestionDraftView>> result = services.draftLifecycleService().listDrafts();
        if (!printResult(result)) {
            return;
        }
        List<SuggestionDraftView> drafts = result.getPayload();
        output.println("AI 草稿列表");
        if (drafts.isEmpty()) {
            output.println("暂无 AI 草稿");
            return;
        }
        drafts.stream().limit(10).forEach(draft -> output.println(draft.id().value()
                + " | " + draft.type()
                + " | " + draft.status()
                + " | 任务 " + draft.tasks().size()
                + " | 学习计划 " + draft.studyPlan().isPresent()));
    }

    private void printHelp() {
        output.println("命令说明: 1 汇总, 2 任务, 3 日程, 4 学习计划, 5 收支, 6 笔记, 7 AI 问答, 8 AI 草稿, h 帮助, q 退出。");
    }

    private void stop() {
        running = false;
        output.println("已退出。");
    }

    private <T> boolean printResult(OperationResult<T> result) {
        Objects.requireNonNull(result, "result");
        if (result.isFailure()) {
            output.println("失败: " + result.getErrorCode() + " - " + result.getMessage());
            return false;
        }
        if (result.getPayload() == null) {
            output.println("操作成功");
        }
        return true;
    }

    private String readLine(String prompt) {
        output.print(prompt);
        output.flush();
        try {
            return input.readLine();
        } catch (IOException exception) {
            output.println("输入读取失败，程序退出。");
            running = false;
            return null;
        }
    }

    private record ParsedInput<T>(State state, T value) {
        private ParsedInput {
            Objects.requireNonNull(state, "state");
        }

        static <T> ParsedInput<T> value(T value) {
            return new ParsedInput<>(State.VALUE, Objects.requireNonNull(value, "value"));
        }

        static <T> ParsedInput<T> empty() {
            return new ParsedInput<>(State.EMPTY, null);
        }

        static <T> ParsedInput<T> invalid() {
            return new ParsedInput<>(State.INVALID, null);
        }

        static <T> ParsedInput<T> eof() {
            return new ParsedInput<>(State.EOF, null);
        }

        boolean hasValue() {
            return state == State.VALUE;
        }

        boolean isEmpty() {
            return state == State.EMPTY;
        }

        boolean isInvalid() {
            return state == State.INVALID;
        }

        boolean isEof() {
            return state == State.EOF;
        }

        private enum State {
            VALUE,
            EMPTY,
            INVALID,
            EOF
        }
    }
}
