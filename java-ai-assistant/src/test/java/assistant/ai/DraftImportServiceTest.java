package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.study.InMemoryStudyPlanRepository;
import assistant.study.StudyPlanAnalysisService;
import assistant.study.StudyPlanService;
import assistant.study.StudyPlanView;
import assistant.task.InMemoryTaskRepository;
import assistant.task.TaskItem;
import assistant.task.TaskPriority;
import assistant.task.TaskQuery;
import assistant.task.TaskRepository;
import assistant.task.TaskService;
import assistant.task.TaskStatus;
import assistant.task.TaskView;
import assistant.testability.FixedTimeProvider;
import assistant.testability.IncrementalIdGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DraftImportServiceTest {
    private static final LocalDate JUNE_12 = LocalDate.of(2026, 6, 12);
    private static final LocalDate JUNE_20 = LocalDate.of(2026, 6, 20);
    private static final LocalDate JUNE_21 = LocalDate.of(2026, 6, 21);
    private static final LocalDate JULY_1 = LocalDate.of(2026, 7, 1);
    private static final LocalDate JULY_31 = LocalDate.of(2026, 7, 31);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 12, 9, 0);

    @Test
    void importsAllTaskDraftItems() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        DraftImportService importService = new DraftImportService(taskService, studyPlanService());
        SuggestionDraft draft = SuggestionDraft.forTasks(new EntityId(1), List.of(
                task("Write tests", JUNE_20),
                task("Refactor service", JUNE_21)));

        OperationResult<Void> result = importService.importDraft(draft);

        OperationResult<List<TaskView>> tasks = taskService.listTasks();
        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(List.of("Write tests", "Refactor service"),
                        tasks.getPayload().stream().map(TaskView::title).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void rejectsTaskDraftMissingDueDateBeforeCreatingAnyTask() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        DraftImportService importService = new DraftImportService(taskService, studyPlanService());
        SuggestionDraft draft = SuggestionDraft.forTasks(new EntityId(1), List.of(
                task("Write tests", JUNE_20),
                task("Refactor service", null)));

        OperationResult<Void> result = importService.importDraft(draft);

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("task draft dueDate must not be null", result.getMessage()),
                () -> assertTrue(taskService.listTasks().getPayload().isEmpty()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void rollsBackCreatedTasksWhenTaskCreationFails() {
        FailingTaskRepository taskRepository = new FailingTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        DraftImportService importService = new DraftImportService(taskService, studyPlanService());
        taskService.createTask("Existing", "baseline", TaskPriority.LOW, JUNE_12);
        taskRepository.failOnSave(2, new IllegalArgumentException("planned task creation failure"));
        SuggestionDraft draft = SuggestionDraft.forTasks(new EntityId(1), List.of(
                task("First draft", JUNE_20),
                task("Second draft", JUNE_21)));

        OperationResult<Void> result = importService.importDraft(draft);

        List<TaskView> tasks = taskService.listTasks().getPayload();
        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("planned task creation failure", result.getMessage()),
                () -> assertEquals(List.of("Existing"), tasks.stream().map(TaskView::title).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void rollsBackCreatedTasksWhenTaskCreationThrowsRuntimeException() {
        FailingTaskRepository taskRepository = new FailingTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        DraftImportService importService = new DraftImportService(taskService, studyPlanService());
        taskService.createTask("Existing", "baseline", TaskPriority.LOW, JUNE_12);
        taskRepository.failOnSave(2, new IllegalStateException("planned task repository failure"));
        SuggestionDraft draft = SuggestionDraft.forTasks(new EntityId(1), List.of(
                task("First draft", JUNE_20),
                task("Second draft", JUNE_21)));

        OperationResult<Void> result = importService.importDraft(draft);

        List<TaskView> tasks = taskService.listTasks().getPayload();
        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.SYSTEM_ERROR, result.getErrorCode()),
                () -> assertEquals("failed to import suggestion draft", result.getMessage()),
                () -> assertEquals(List.of("Existing"), tasks.stream().map(TaskView::title).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void importsStudyPlanDraftCreatesTasksForBreakdown() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        InMemoryStudyPlanRepository studyPlanRepository = new InMemoryStudyPlanRepository();
        StudyPlanService studyPlanService = studyPlanService(studyPlanRepository);
        DraftImportService importService = new DraftImportService(taskService, studyPlanService);
        SuggestionDraft draft = SuggestionDraft.forStudyPlan(
                new EntityId(1), studyPlanWithBreakdown(List.of("Syntax", "Testing")));

        OperationResult<Void> result = importService.importDraft(draft);

        List<TaskView> tasks = taskService.listTasks().getPayload();
        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(1, studyPlanService.listStudyPlans().getPayload().size()),
                () -> assertEquals("Learn Java", studyPlanService.listStudyPlans().getPayload().get(0).goalName()),
                () -> assertEquals(20, studyPlanService.listStudyPlans().getPayload().get(0).progress().value()),
                () -> assertEquals(List.of("Syntax", "Testing"), tasks.stream().map(TaskView::title).toList()),
                () -> assertEquals(List.of("来自学习计划：Learn Java", "来自学习计划：Learn Java"),
                        tasks.stream().map(TaskView::description).toList()),
                () -> assertEquals(List.of(TaskPriority.MEDIUM, TaskPriority.MEDIUM),
                        tasks.stream().map(TaskView::priority).toList()),
                () -> assertEquals(List.of(JULY_31, JULY_31), tasks.stream().map(TaskView::dueDate).toList()),
                () -> assertEquals(List.of(TaskStatus.TODO, TaskStatus.TODO),
                        tasks.stream().map(TaskView::status).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void importsStudyPlanDraftWithoutBreakdownCreatesOnlyStudyPlan() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        InMemoryStudyPlanRepository studyPlanRepository = new InMemoryStudyPlanRepository();
        StudyPlanService studyPlanService = studyPlanService(studyPlanRepository);
        DraftImportService importService = new DraftImportService(taskService, studyPlanService);
        SuggestionDraft draft = SuggestionDraft.forStudyPlan(new EntityId(1), studyPlanWithoutBreakdown());

        OperationResult<Void> result = importService.importDraft(draft);

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(1, studyPlanService.listStudyPlans().getPayload().size()),
                () -> assertTrue(taskService.listTasks().getPayload().isEmpty()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void importsStudyPlanDraftIgnoresBlankBreakdownItemsCleanedByContent() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        DraftImportService importService = new DraftImportService(taskService, studyPlanService());
        SuggestionDraft draft = SuggestionDraft.forStudyPlan(
                new EntityId(1), studyPlanWithBreakdown(List.of(" Syntax ", " ", "Testing")));

        OperationResult<Void> result = importService.importDraft(draft);

        List<TaskView> tasks = taskService.listTasks().getPayload();
        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(List.of("Syntax", "Testing"), tasks.stream().map(TaskView::title).toList()),
                () -> assertEquals(List.of(JULY_31, JULY_31), tasks.stream().map(TaskView::dueDate).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void propagatesStudyPlanCreationFailureWithoutCreatingBreakdownTasks() {
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        StudyPlanService studyPlanService = mock(StudyPlanService.class);
        when(studyPlanService.createStudyPlan("Learn Java", JULY_1, JULY_31, 30, 20))
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "study plan failed"));
        DraftImportService importService = new DraftImportService(taskService, studyPlanService);
        SuggestionDraft draft = SuggestionDraft.forStudyPlan(
                new EntityId(1), studyPlanWithBreakdown(List.of("Syntax", "Testing")));

        OperationResult<Void> result = importService.importDraft(draft);

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("study plan failed", result.getMessage()),
                () -> assertTrue(taskService.listTasks().getPayload().isEmpty()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationFails() {
        FailingTaskRepository taskRepository = new FailingTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        InMemoryStudyPlanRepository studyPlanRepository = new InMemoryStudyPlanRepository();
        StudyPlanService studyPlanService = studyPlanService(studyPlanRepository);
        DraftImportService importService = new DraftImportService(taskService, studyPlanService);
        taskService.createTask("Existing", "baseline", TaskPriority.LOW, JUNE_12);
        taskRepository.failOnSave(2, new IllegalArgumentException("planned breakdown task creation failure"));
        SuggestionDraft draft = SuggestionDraft.forStudyPlan(
                new EntityId(1), studyPlanWithBreakdown(List.of("Syntax", "Testing")));

        OperationResult<Void> result = importService.importDraft(draft);

        List<TaskView> tasks = taskService.listTasks().getPayload();
        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("planned breakdown task creation failure", result.getMessage()),
                () -> assertTrue(studyPlanService.listStudyPlans().getPayload().isEmpty()),
                () -> assertEquals(List.of("Existing"), tasks.stream().map(TaskView::title).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationThrowsRuntimeException() {
        FailingTaskRepository taskRepository = new FailingTaskRepository();
        TaskService taskService = new TaskService(taskRepository, new IncrementalIdGenerator(100));
        InMemoryStudyPlanRepository studyPlanRepository = new InMemoryStudyPlanRepository();
        StudyPlanService studyPlanService = studyPlanService(studyPlanRepository);
        DraftImportService importService = new DraftImportService(taskService, studyPlanService);
        taskService.createTask("Existing", "baseline", TaskPriority.LOW, JUNE_12);
        taskRepository.failOnSave(2, new IllegalStateException("planned breakdown task repository failure"));
        SuggestionDraft draft = SuggestionDraft.forStudyPlan(
                new EntityId(1), studyPlanWithBreakdown(List.of("Syntax", "Testing")));

        OperationResult<Void> result = importService.importDraft(draft);

        List<TaskView> tasks = taskService.listTasks().getPayload();
        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.SYSTEM_ERROR, result.getErrorCode()),
                () -> assertEquals("failed to import suggestion draft", result.getMessage()),
                () -> assertTrue(studyPlanService.listStudyPlans().getPayload().isEmpty()),
                () -> assertEquals(List.of("Existing"), tasks.stream().map(TaskView::title).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()));
    }

    @Test
    void importDraftRejectsNullDraft() {
        DraftImportService importService = new DraftImportService(taskService(), studyPlanService());

        OperationResult<Void> result = importService.importDraft(null);

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("draft must not be null", result.getMessage()));
    }

    @Test
    void constructorRejectsNullDependencies() {
        TaskService taskService = taskService();
        StudyPlanService studyPlanService = studyPlanService();

        assertAll(
                () -> assertNullFieldRejected("taskService", () -> new DraftImportService(null, studyPlanService)),
                () -> assertNullFieldRejected("studyPlanService", () -> new DraftImportService(taskService, null)));
    }

    private static TaskDraftItem task(String title, LocalDate dueDate) {
        return new TaskDraftItem(title, "description", TaskPriority.MEDIUM, dueDate);
    }

    private static StudyPlanDraftContent studyPlanWithBreakdown(List<String> breakdown) {
        return new StudyPlanDraftContent(
                "Learn Java",
                JULY_1,
                JULY_31,
                30,
                Progress.of(20),
                breakdown);
    }

    private static StudyPlanDraftContent studyPlanWithoutBreakdown() {
        return studyPlanWithBreakdown(List.of());
    }

    private static TaskService taskService() {
        return new TaskService(new InMemoryTaskRepository(), new IncrementalIdGenerator(100));
    }

    private static StudyPlanService studyPlanService() {
        return studyPlanService(new InMemoryStudyPlanRepository());
    }

    private static StudyPlanService studyPlanService(InMemoryStudyPlanRepository repository) {
        return new StudyPlanService(
                repository,
                new IncrementalIdGenerator(200),
                new FixedTimeProvider(NOW),
                new StudyPlanAnalysisService());
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static final class FailingTaskRepository implements TaskRepository {
        private final InMemoryTaskRepository delegate = new InMemoryTaskRepository();
        private boolean failureEnabled;
        private int saveCallsAfterEnable;
        private int failOnSaveCall;
        private RuntimeException failure;

        void failOnSave(int saveCallAfterEnable, RuntimeException failure) {
            if (saveCallAfterEnable <= 0) {
                throw new IllegalArgumentException("saveCallAfterEnable must be positive");
            }
            this.failure = Objects.requireNonNull(failure, "failure");
            this.failOnSaveCall = saveCallAfterEnable;
            this.saveCallsAfterEnable = 0;
            this.failureEnabled = true;
        }

        @Override
        public void save(TaskItem task) {
            if (failureEnabled) {
                saveCallsAfterEnable++;
                if (saveCallsAfterEnable == failOnSaveCall) {
                    throw failure;
                }
            }
            delegate.save(task);
        }

        @Override
        public Optional<TaskItem> findById(EntityId id) {
            return delegate.findById(id);
        }

        @Override
        public List<TaskItem> findAll() {
            return delegate.findAll();
        }

        @Override
        public List<TaskItem> findBy(TaskQuery query) {
            return delegate.findBy(query);
        }

        @Override
        public boolean deleteById(EntityId id) {
            return delegate.deleteById(id);
        }
    }
}
