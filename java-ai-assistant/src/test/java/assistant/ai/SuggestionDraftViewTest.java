package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.task.TaskPriority;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SuggestionDraftViewTest {
    private static final EntityId ID = new EntityId(1);

    @Test
    void fromCopiesCurrentDraftState() {
        SuggestionDraft draft = SuggestionDraft.forTasks(ID, List.of(task("A")));

        SuggestionDraftView view = SuggestionDraftView.from(draft);
        draft.cancel();

        assertEquals(ID, view.id());
        assertEquals(SuggestionDraftType.TASK_DRAFT, view.type());
        assertEquals(SuggestionDraftStatus.CONFIRMABLE, view.status());
        assertTrue(view.isConfirmable());
        assertEquals(List.of(task("A")), view.tasks());
        assertTrue(view.studyPlan().isEmpty());
        assertEquals(SuggestionDraftStatus.CANCELLED, draft.getStatus());
    }

    @Test
    void constructorCopiesTaskList() {
        ArrayList<TaskDraftItem> tasks = new ArrayList<>(List.of(task("A")));

        SuggestionDraftView view = new SuggestionDraftView(
                ID,
                SuggestionDraftType.TASK_DRAFT,
                SuggestionDraftStatus.CANCELLED,
                tasks,
                Optional.empty());
        tasks.add(task("B"));

        assertEquals(List.of(task("A")), view.tasks());
        assertFalse(view.isConfirmable());
        assertThrows(UnsupportedOperationException.class, () -> view.tasks().add(task("C")));
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> SuggestionDraftView.from(null));
        assertThrows(NullPointerException.class,
                () -> new SuggestionDraftView(null, SuggestionDraftType.TASK_DRAFT,
                        SuggestionDraftStatus.CONFIRMABLE, List.of(), Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> new SuggestionDraftView(ID, null,
                        SuggestionDraftStatus.CONFIRMABLE, List.of(), Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> new SuggestionDraftView(ID, SuggestionDraftType.TASK_DRAFT,
                        null, List.of(), Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> new SuggestionDraftView(ID, SuggestionDraftType.TASK_DRAFT,
                        SuggestionDraftStatus.CONFIRMABLE, null, Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> new SuggestionDraftView(ID, SuggestionDraftType.TASK_DRAFT,
                        SuggestionDraftStatus.CONFIRMABLE, List.of(task("A"), null), Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> new SuggestionDraftView(ID, SuggestionDraftType.TASK_DRAFT,
                        SuggestionDraftStatus.CONFIRMABLE, List.of(), null));
    }

    private static TaskDraftItem task(String title) {
        return new TaskDraftItem(title, "", TaskPriority.LOW, null);
    }
}
