package assistant.app;

import assistant.ai.AiScenario;
import assistant.ai.SuggestionDraftView;
import assistant.common.OperationResult;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionView;
import assistant.note.NoteView;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanView;
import assistant.summary.DashboardSummary;
import assistant.task.TaskView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
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
            case "2" -> showTasks();
            case "3" -> showSchedules();
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

    private void showTasks() {
        OperationResult<List<TaskView>> result = services.taskService().listTasks();
        if (!printResult(result)) {
            return;
        }
        List<TaskView> tasks = result.getPayload();
        output.println("任务列表");
        if (tasks.isEmpty()) {
            output.println("暂无任务");
            return;
        }
        tasks.stream().limit(10).forEach(task -> output.println(task.id().value()
                + " | " + task.title()
                + " | " + task.priority()
                + " | " + task.status()
                + " | 截止 " + task.dueDate()));
    }

    private void showSchedules() {
        OperationResult<List<ScheduleView>> result = services.scheduleService().listSchedules();
        if (!printResult(result)) {
            return;
        }
        List<ScheduleView> schedules = result.getPayload();
        output.println("日程列表");
        if (schedules.isEmpty()) {
            output.println("暂无日程");
            return;
        }
        schedules.stream().limit(10).forEach(schedule -> output.println(schedule.id().value()
                + " | " + schedule.name()
                + " | " + schedule.status()
                + " | " + schedule.startDateTime()
                + " ~ " + schedule.endDateTime()
                + " | " + schedule.location()));
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
}
