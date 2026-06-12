package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.BusinessException;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.Progress;
import assistant.task.TaskPriority;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SuggestionDraftTest {
    private static final EntityId ID = new EntityId(1);

    @Test
    void forTasksCreatesConfirmableTaskDraftWithTaskSnapshot() {
        List<TaskDraftItem> tasks = new ArrayList<>(List.of(task("A")));

        SuggestionDraft draft = SuggestionDraft.forTasks(ID, tasks);
        tasks.add(task("B"));

        assertAll(
                () -> assertEquals(ID, draft.getId()),
                () -> assertEquals(SuggestionDraftType.TASK_DRAFT, draft.getType()),
                () -> assertEquals(SuggestionDraftStatus.CONFIRMABLE, draft.getStatus()),
                () -> assertTrue(draft.isConfirmable()),
                () -> assertEquals(List.of(task("A")), draft.getTasks()),
                () -> assertTrue(draft.getStudyPlan().isEmpty()));
        assertThrows(UnsupportedOperationException.class, () -> draft.getTasks().add(task("C")));
    }

    @Test
    void forStudyPlanCreatesConfirmableStudyPlanDraft() {
        StudyPlanDraftContent studyPlan = studyPlan();

        SuggestionDraft draft = SuggestionDraft.forStudyPlan(ID, studyPlan);

        assertEquals(SuggestionDraftType.STUDY_PLAN_DRAFT, draft.getType());
        assertEquals(List.of(), draft.getTasks());
        assertEquals(studyPlan, draft.getStudyPlan().orElseThrow());
    }

    @Test
    void factoriesRejectInvalidContent() {
        assertThrows(NullPointerException.class, () -> SuggestionDraft.forTasks(null, List.of(task("A"))));
        assertThrows(NullPointerException.class, () -> SuggestionDraft.forTasks(ID, null));
        assertThrows(IllegalArgumentException.class, () -> SuggestionDraft.forTasks(ID, List.of()));
        assertThrows(NullPointerException.class, () -> SuggestionDraft.forTasks(ID, List.of(task("A"), null)));
        assertThrows(NullPointerException.class, () -> SuggestionDraft.forStudyPlan(ID, null));
    }

    @Test
    void cancelTransitionsOnlyFromConfirmable() {
        SuggestionDraft draft = SuggestionDraft.forTasks(ID, List.of(task("A")));

        draft.cancel();

        assertEquals(SuggestionDraftStatus.CANCELLED, draft.getStatus());
        BusinessException exception = assertThrows(BusinessException.class, draft::cancel);
        assertEquals(ErrorCode.STATE_CONFLICT, exception.getErrorCode());
        assertEquals("suggestion draft is not confirmable", exception.getMessage());
    }

    @Test
    void markImportedTransitionsOnlyFromConfirmable() {
        SuggestionDraft draft = SuggestionDraft.forTasks(ID, List.of(task("A")));

        draft.markImported();

        assertEquals(SuggestionDraftStatus.IMPORTED, draft.getStatus());
        assertFalse(draft.isConfirmable());
        BusinessException exception = assertThrows(BusinessException.class, draft::markImported);
        assertEquals(ErrorCode.STATE_CONFLICT, exception.getErrorCode());
    }

    @Test
    void terminalStatesRejectOppositeTransition() {
        SuggestionDraft cancelled = SuggestionDraft.forTasks(ID, List.of(task("A")));
        cancelled.cancel();
        SuggestionDraft imported = SuggestionDraft.forTasks(new EntityId(2), List.of(task("B")));
        imported.markImported();

        BusinessException importAfterCancel = assertThrows(BusinessException.class, cancelled::markImported);
        BusinessException cancelAfterImport = assertThrows(BusinessException.class, imported::cancel);

        assertAll(
                () -> assertEquals(ErrorCode.STATE_CONFLICT, importAfterCancel.getErrorCode()),
                () -> assertEquals("suggestion draft is not confirmable", importAfterCancel.getMessage()),
                () -> assertEquals(SuggestionDraftStatus.CANCELLED, cancelled.getStatus()),
                () -> assertEquals(ErrorCode.STATE_CONFLICT, cancelAfterImport.getErrorCode()),
                () -> assertEquals("suggestion draft is not confirmable", cancelAfterImport.getMessage()),
                () -> assertEquals(SuggestionDraftStatus.IMPORTED, imported.getStatus()));
    }

    private static TaskDraftItem task(String title) {
        return new TaskDraftItem(title, "", TaskPriority.LOW, null);
    }

    private static StudyPlanDraftContent studyPlan() {
        return new StudyPlanDraftContent(
                "Learn Java",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                20,
                Progress.zero(),
                List.of("Basics"));
    }
}
