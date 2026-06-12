package assistant.note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.common.Tag;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InMemoryNoteRepositoryTest {
    private static final LocalDate CREATED_DATE = LocalDate.of(2026, 6, 12);

    @Test
    void saveAndFindByIdReturnsDetachedSnapshot() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        Note note = note(1, "Daily Review", "Summarize progress", Set.of(Tag.of("work")));

        repository.save(note);

        Note stored = repository.findById(new EntityId(1)).orElseThrow();
        assertNotSame(note, stored);
        assertEquals(note.getTitle(), stored.getTitle());
    }

    @Test
    void saveCopiesInputNoteSoLaterCallerMutationsDoNotAffectRepository() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        Note note = note(1, "Daily Review", "Summarize progress", Set.of(Tag.of("work")));
        repository.save(note);

        note.updateContent("Changed", "Changed content");
        note.replaceTags(Set.of(Tag.of("later")));

        Note stored = repository.findById(new EntityId(1)).orElseThrow();
        assertEquals("Daily Review", stored.getTitle());
        assertEquals("Summarize progress", stored.getContent());
        assertEquals(Set.of(Tag.of("work")), stored.getTags());
    }

    @Test
    void findByIdReturnsEmptyWhenNoteDoesNotExist() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();

        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void saveReplacesNoteWithSameIdAndKeepsInsertionOrder() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        repository.save(note(1, "First", "Content", Set.of(Tag.of("work"))));
        repository.save(note(2, "Second", "Content", Set.of(Tag.of("journal"))));
        repository.save(note(1, "Updated", "Updated content", Set.of(Tag.of("later"))));

        List<Note> all = repository.findAll();
        assertEquals(List.of(new EntityId(1), new EntityId(2)), idsOf(all));
        assertEquals("Updated", all.get(0).getTitle());
    }

    @Test
    void findAllReturnsNotesInInsertionOrder() {
        InMemoryNoteRepository repository = repositoryWithMixedNotes();

        assertEquals(List.of(new EntityId(1), new EntityId(2), new EntityId(3)), idsOf(repository.findAll()));
    }

    @Test
    void findAllReturnsUnmodifiableDetachedSnapshotList() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        repository.save(note(1, "Daily Review", "Summarize progress", Set.of(Tag.of("work"))));

        List<Note> snapshot = repository.findAll();

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.add(note(2, "Second", "Content", Set.of(Tag.of("journal")))));
        snapshot.get(0).updateContent("Changed", "Changed content");
        assertEquals("Daily Review", repository.findById(new EntityId(1)).orElseThrow().getTitle());
    }

    @Test
    void findByFiltersUsingQueryAndSearchPolicy() {
        InMemoryNoteRepository repository = repositoryWithMixedNotes();

        List<Note> result = repository.findBy(NoteQuery.byKeyword("review"), new NoteSearchPolicy());

        assertEquals(List.of(new EntityId(1)), idsOf(result));
    }

    @Test
    void findByAppliesCombinedQueryInInsertionOrder() {
        InMemoryNoteRepository repository = repositoryWithMixedNotes();

        List<Note> result = repository.findBy(NoteQuery.of("progress", Tag.of("work")), new NoteSearchPolicy());

        assertEquals(List.of(new EntityId(1), new EntityId(3)), idsOf(result));
    }

    @Test
    void findByReturnsUnmodifiableDetachedSnapshotList() {
        InMemoryNoteRepository repository = repositoryWithMixedNotes();

        List<Note> snapshot = repository.findBy(NoteQuery.all(), new NoteSearchPolicy());

        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0));
        snapshot.get(0).replaceTags(Set.of(Tag.of("changed")));
        assertEquals(Set.of(Tag.of("work")), repository.findById(new EntityId(1)).orElseThrow().getTags());
    }

    @Test
    void mutatingNoteReturnedFromFindByIdDoesNotAffectStoredState() {
        InMemoryNoteRepository repository = repositoryWithMixedNotes();

        Note returned = repository.findById(new EntityId(1)).orElseThrow();
        returned.updateContent("Changed", "Changed content");

        assertEquals("Daily Review", repository.findById(new EntityId(1)).orElseThrow().getTitle());
    }

    @Test
    void deleteByIdRemovesExistingNote() {
        InMemoryNoteRepository repository = repositoryWithMixedNotes();

        assertTrue(repository.deleteById(new EntityId(1)));
        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void deleteByIdReturnsFalseWhenNoteDoesNotExist() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();

        assertFalse(repository.deleteById(new EntityId(1)));
    }

    @Test
    void methodsRejectNullArguments() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();

        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.findBy(null, new NoteSearchPolicy()));
        assertThrows(NullPointerException.class, () -> repository.findBy(NoteQuery.all(), null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
    }

    private static InMemoryNoteRepository repositoryWithMixedNotes() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        repository.save(note(1, "Daily Review", "Summarize progress", Set.of(Tag.of("work"))));
        repository.save(note(2, "Travel", "Packing list", Set.of(Tag.of("personal"))));
        repository.save(note(3, "Sprint", "Track progress", orderedTags(Tag.of("work"), Tag.of("planning"))));
        return repository;
    }

    private static Note note(long id, String title, String content, Set<Tag> tags) {
        return new Note(new EntityId(id), title, content, CREATED_DATE, tags);
    }

    private static LinkedHashSet<Tag> orderedTags(Tag... tags) {
        LinkedHashSet<Tag> orderedTags = new LinkedHashSet<>();
        for (Tag tag : tags) {
            orderedTags.add(tag);
        }
        return orderedTags;
    }

    private static List<EntityId> idsOf(List<Note> notes) {
        return notes.stream().map(Note::getId).toList();
    }
}
