package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.study.InMemoryStudyPlanRepository;
import assistant.study.StudyPlanAnalysisService;
import assistant.study.StudyPlanService;
import assistant.task.InMemoryTaskRepository;
import assistant.task.TaskPriority;
import assistant.task.TaskService;
import assistant.testability.FixedTimeProvider;
import assistant.testability.IncrementalIdGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class DraftLifecycleServiceTest {
    private static final LocalDate JUNE_20 = LocalDate.of(2026, 6, 20);
    private static final LocalDate JULY_1 = LocalDate.of(2026, 7, 1);
    private static final LocalDate JULY_31 = LocalDate.of(2026, 7, 31);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 12, 9, 0);

    @Test
    void getDraftReturnsViewSnapshot() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftLifecycleService lifecycleService = new DraftLifecycleService(repository, successfulImportService());
        SuggestionDraft draft = taskDraft(1, "Draft");
        repository.save(draft);

        OperationResult<SuggestionDraftView> result = lifecycleService.getDraft(new EntityId(1));
        draft.cancel();

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, result.getPayload().status()),
                () -> assertEquals(SuggestionDraftStatus.CANCELLED, draft.getStatus()));
    }

    @Test
    void getDraftReturnsNotFoundForMissingDraft() {
        DraftLifecycleService lifecycleService =
                new DraftLifecycleService(new InMemorySuggestionDraftRepository(), successfulImportService());

        OperationResult<SuggestionDraftView> result = lifecycleService.getDraft(new EntityId(404));

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.NOT_FOUND, result.getErrorCode()),
                () -> assertEquals("suggestion draft not found: 404", result.getMessage()));
    }

    @Test
    void getDraftRejectsNullId() {
        DraftLifecycleService lifecycleService =
                new DraftLifecycleService(new InMemorySuggestionDraftRepository(), successfulImportService());

        OperationResult<SuggestionDraftView> result = lifecycleService.getDraft(null);

        assertNullIdFailure(result);
    }

    @Test
    void listDraftsReturnsUnmodifiableViewSnapshots() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftLifecycleService lifecycleService = new DraftLifecycleService(repository, successfulImportService());
        SuggestionDraft first = taskDraft(1, "First");
        SuggestionDraft second = taskDraft(2, "Second");
        repository.save(first);
        repository.save(second);

        OperationResult<List<SuggestionDraftView>> result = lifecycleService.listDrafts();
        first.cancel();
        repository.save(taskDraft(3, "Third"));

        List<SuggestionDraftView> views = result.getPayload();
        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(List.of(new EntityId(1), new EntityId(2)),
                        views.stream().map(SuggestionDraftView::id).toList()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, views.get(0).status()),
                () -> assertThrows(UnsupportedOperationException.class, () -> views.clear()));
    }

    @Test
    void cancelDraftMarksDraftCancelledAndSaves() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftImportService importService = mock(DraftImportService.class);
        DraftLifecycleService lifecycleService = new DraftLifecycleService(repository, importService);
        repository.save(taskDraft(1, "Draft"));

        OperationResult<SuggestionDraftView> result = lifecycleService.cancelDraft(new EntityId(1));

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(SuggestionDraftStatus.CANCELLED, result.getPayload().status()),
                () -> assertEquals(SuggestionDraftStatus.CANCELLED,
                        repository.findById(new EntityId(1)).orElseThrow().getStatus()),
                () -> verify(importService, never()).importDraft(any()));
    }

    @Test
    void cancelDraftReturnsNotFoundForMissingDraft() {
        DraftLifecycleService lifecycleService =
                new DraftLifecycleService(new InMemorySuggestionDraftRepository(), successfulImportService());

        OperationResult<SuggestionDraftView> result = lifecycleService.cancelDraft(new EntityId(404));

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.NOT_FOUND, result.getErrorCode()),
                () -> assertEquals("suggestion draft not found: 404", result.getMessage()));
    }

    @Test
    void cancelDraftRejectsTerminalDrafts() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftImportService importService = mock(DraftImportService.class);
        DraftLifecycleService lifecycleService = new DraftLifecycleService(repository, importService);
        SuggestionDraft cancelled = taskDraft(1, "Cancelled");
        cancelled.cancel();
        SuggestionDraft imported = taskDraft(2, "Imported");
        imported.markImported();
        repository.save(cancelled);
        repository.save(imported);

        OperationResult<SuggestionDraftView> cancelCancelled = lifecycleService.cancelDraft(new EntityId(1));
        OperationResult<SuggestionDraftView> cancelImported = lifecycleService.cancelDraft(new EntityId(2));

        assertAll(
                () -> assertStateConflict(cancelCancelled),
                () -> assertStateConflict(cancelImported),
                () -> verify(importService, never()).importDraft(any()));
    }

    @Test
    void confirmDraftImportsAndMarksDraftImported() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftImportService importService = mock(DraftImportService.class);
        when(importService.importDraft(any())).thenReturn(OperationResult.success());
        DraftLifecycleService lifecycleService = new DraftLifecycleService(repository, importService);
        repository.save(taskDraft(1, "Draft"));

        OperationResult<SuggestionDraftView> result = lifecycleService.confirmDraft(new EntityId(1));

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(SuggestionDraftStatus.IMPORTED, result.getPayload().status()),
                () -> assertEquals(SuggestionDraftStatus.IMPORTED,
                        repository.findById(new EntityId(1)).orElseThrow().getStatus()),
                () -> verify(importService).importDraft(any(SuggestionDraft.class)));
    }

    @Test
    void confirmDraftReturnsNotFoundForMissingDraft() {
        DraftImportService importService = mock(DraftImportService.class);
        DraftLifecycleService lifecycleService =
                new DraftLifecycleService(new InMemorySuggestionDraftRepository(), importService);

        OperationResult<SuggestionDraftView> result = lifecycleService.confirmDraft(new EntityId(404));

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.NOT_FOUND, result.getErrorCode()),
                () -> assertEquals("suggestion draft not found: 404", result.getMessage()),
                () -> verify(importService, never()).importDraft(any()));
    }

    @Test
    void confirmDraftRejectsTerminalDraftsWithoutImporting() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftImportService importService = mock(DraftImportService.class);
        DraftLifecycleService lifecycleService = new DraftLifecycleService(repository, importService);
        SuggestionDraft imported = taskDraft(1, "Imported");
        imported.markImported();
        SuggestionDraft cancelled = taskDraft(2, "Cancelled");
        cancelled.cancel();
        repository.save(imported);
        repository.save(cancelled);

        OperationResult<SuggestionDraftView> repeatedConfirm = lifecycleService.confirmDraft(new EntityId(1));
        OperationResult<SuggestionDraftView> confirmCancelled = lifecycleService.confirmDraft(new EntityId(2));

        assertAll(
                () -> assertStateConflict(repeatedConfirm),
                () -> assertStateConflict(confirmCancelled),
                () -> verify(importService, never()).importDraft(any()));
    }

    @Test
    void confirmDraftKeepsDraftConfirmableWhenImportFails() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftImportService importService = mock(DraftImportService.class);
        when(importService.importDraft(any()))
                .thenReturn(OperationResult.failure(ErrorCode.SYSTEM_ERROR, "import failed"))
                .thenReturn(OperationResult.success());
        DraftLifecycleService lifecycleService = new DraftLifecycleService(repository, importService);
        repository.save(taskDraft(1, "Draft"));

        OperationResult<SuggestionDraftView> failed = lifecycleService.confirmDraft(new EntityId(1));
        OperationResult<SuggestionDraftView> retry = lifecycleService.confirmDraft(new EntityId(1));

        assertAll(
                () -> assertTrue(failed.isFailure()),
                () -> assertEquals(ErrorCode.SYSTEM_ERROR, failed.getErrorCode()),
                () -> assertEquals("import failed", failed.getMessage()),
                () -> assertEquals(SuggestionDraftStatus.IMPORTED, retry.getPayload().status()),
                () -> assertEquals(SuggestionDraftStatus.IMPORTED,
                        repository.findById(new EntityId(1)).orElseThrow().getStatus()));
    }

    @Test
    void confirmDraftRejectsNullId() {
        DraftLifecycleService lifecycleService =
                new DraftLifecycleService(new InMemorySuggestionDraftRepository(), successfulImportService());

        OperationResult<SuggestionDraftView> result = lifecycleService.confirmDraft(null);

        assertNullIdFailure(result);
    }

    @Test
    void cancelDraftRejectsNullId() {
        DraftLifecycleService lifecycleService =
                new DraftLifecycleService(new InMemorySuggestionDraftRepository(), successfulImportService());

        OperationResult<SuggestionDraftView> result = lifecycleService.cancelDraft(null);

        assertNullIdFailure(result);
    }

    @Test
    void constructorRejectsNullDependencies() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        DraftImportService importService = successfulImportService();

        assertAll(
                () -> assertNullFieldRejected("repository", () -> new DraftLifecycleService(null, importService)),
                () -> assertNullFieldRejected("importService", () -> new DraftLifecycleService(repository, null)));
    }

    private static SuggestionDraft taskDraft(long id, String title) {
        return SuggestionDraft.forTasks(new EntityId(id), List.of(
                new TaskDraftItem(title, "description", TaskPriority.MEDIUM, JUNE_20)));
    }

    private static StudyPlanDraftContent studyPlan() {
        return new StudyPlanDraftContent(
                "Learn Java",
                JULY_1,
                JULY_31,
                30,
                Progress.of(20),
                List.of("Syntax"));
    }

    private static DraftImportService successfulImportService() {
        return new DraftImportService(
                new TaskService(new InMemoryTaskRepository(), new IncrementalIdGenerator(100)),
                new StudyPlanService(
                        new InMemoryStudyPlanRepository(),
                        new IncrementalIdGenerator(200),
                        new FixedTimeProvider(NOW),
                        new StudyPlanAnalysisService()));
    }

    private static void assertNullIdFailure(OperationResult<SuggestionDraftView> result) {
        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("id must not be null", result.getMessage()));
    }

    private static void assertStateConflict(OperationResult<SuggestionDraftView> result) {
        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.STATE_CONFLICT, result.getErrorCode()),
                () -> assertEquals("suggestion draft is not confirmable", result.getMessage()));
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
