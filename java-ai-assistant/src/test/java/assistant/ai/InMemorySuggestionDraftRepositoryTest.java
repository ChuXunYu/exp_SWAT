package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.task.TaskPriority;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemorySuggestionDraftRepositoryTest {
    @Test
    void saveAndFindByIdReturnStoredAggregateReference() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        SuggestionDraft draft = draft(1, "A");

        repository.save(draft);

        SuggestionDraft stored = repository.findById(new EntityId(1)).orElseThrow();
        assertSame(draft, stored);
        stored.cancel();
        assertEquals(SuggestionDraftStatus.CANCELLED, draft.getStatus());
    }

    @Test
    void saveReplacesSameIdAndKeepsInsertionOrder() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        repository.save(draft(1, "A"));
        repository.save(draft(2, "B"));
        SuggestionDraft replacement = draft(1, "A2");

        repository.save(replacement);

        List<SuggestionDraft> all = repository.findAll();
        assertEquals(List.of(new EntityId(1), new EntityId(2)), all.stream().map(SuggestionDraft::getId).toList());
        assertSame(replacement, all.get(0));
    }

    @Test
    void findAllReturnsUnmodifiableSnapshotList() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        repository.save(draft(1, "A"));

        List<SuggestionDraft> snapshot = repository.findAll();
        repository.save(draft(2, "B"));

        assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(draft(3, "C")));
    }

    @Test
    void findByIdAndDeleteHandleMissingIds() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();

        assertTrue(repository.findById(new EntityId(1)).isEmpty());
        assertFalse(repository.deleteById(new EntityId(1)));
    }

    @Test
    void deleteByIdRemovesExistingDraft() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();
        repository.save(draft(1, "A"));

        assertTrue(repository.deleteById(new EntityId(1)));
        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void rejectsNullArguments() {
        InMemorySuggestionDraftRepository repository = new InMemorySuggestionDraftRepository();

        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
    }

    private static SuggestionDraft draft(long id, String title) {
        return SuggestionDraft.forTasks(
                new EntityId(id),
                List.of(new TaskDraftItem(title, "", TaskPriority.LOW, null)));
    }
}
