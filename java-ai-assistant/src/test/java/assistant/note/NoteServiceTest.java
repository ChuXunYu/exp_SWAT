package assistant.note;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Tag;
import assistant.testability.FixedTimeProvider;
import assistant.testability.IncrementalIdGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NoteServiceTest {
    private static final LocalDate JUNE_12 = LocalDate.of(2026, 6, 12);
    private static final LocalDateTime JUNE_12_NOON = LocalDateTime.of(2026, 6, 12, 12, 0);

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(
                NullPointerException.class,
                () -> new NoteService(null, new IncrementalIdGenerator(100), fixedTimeProvider(), new NoteSearchPolicy()));
        assertThrows(
                NullPointerException.class,
                () -> new NoteService(new InMemoryNoteRepository(), null, fixedTimeProvider(), new NoteSearchPolicy()));
        assertThrows(
                NullPointerException.class,
                () -> new NoteService(new InMemoryNoteRepository(), new IncrementalIdGenerator(100), null, new NoteSearchPolicy()));
        assertThrows(
                NullPointerException.class,
                () -> new NoteService(new InMemoryNoteRepository(), new IncrementalIdGenerator(100), fixedTimeProvider(), null));
    }

    @Test
    void createNoteUsesGeneratedIdAndCurrentDate() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        NoteService service = new NoteService(
                repository,
                new IncrementalIdGenerator(100),
                fixedTimeProvider(),
                new NoteSearchPolicy());

        OperationResult<NoteView> result = service.createNote("Daily Review", "Summarize progress", Set.of());

        assertSuccess(result);
        NoteView view = result.getPayload();
        assertAll(
                () -> assertInstanceOf(NoteView.class, view),
                () -> assertEquals(new EntityId(100), view.id()),
                () -> assertEquals("Daily Review", view.title()),
                () -> assertEquals("Summarize progress", view.content()),
                () -> assertEquals(JUNE_12, view.createdDate()),
                () -> assertEquals(1, repository.findAll().size()));
    }

    @Test
    void createNoteConvertsTagsDeduplicatesAndKeepsFirstOrder() {
        NoteService service = newService(100);

        OperationResult<NoteView> result = service.createNote(
                "Daily Review",
                "Summarize progress",
                orderedTextTags("Work", " work ", "Journal"));

        assertSuccess(result);
        assertEquals(List.of(Tag.of("work"), Tag.of("journal")), List.copyOf(result.getPayload().tags()));
    }

    @Test
    void createNoteReturnsValidationErrorForInvalidInputsAndDoesNotSave() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        NoteService service = new NoteService(
                repository,
                new IncrementalIdGenerator(100),
                fixedTimeProvider(),
                new NoteSearchPolicy());

        assertFailure(service.createNote(" ", "Content", Set.of()), ErrorCode.VALIDATION_ERROR);
        assertFailure(service.createNote("Title", " ", Set.of()), ErrorCode.VALIDATION_ERROR);
        assertFailure(service.createNote("Title", "Content", null), ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.createNote("Title", "Content", orderedTextTags("work", null)),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.createNote("Title", "Content", orderedTextTags("work", " ")),
                ErrorCode.VALIDATION_ERROR);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createNoteReturnsStableValidationMessages() {
        NoteService service = newService(100);

        assertFailure(
                service.createNote(" ", "Content", Set.of()),
                ErrorCode.VALIDATION_ERROR,
                "title must not be blank");
        assertFailure(
                service.createNote("Title", "Content", null),
                ErrorCode.VALIDATION_ERROR,
                "tagTexts");
        assertFailure(
                service.createNote("Title", "Content", orderedTextTags("work", null)),
                ErrorCode.VALIDATION_ERROR,
                "tagText");
    }

    @Test
    void getNoteReturnsViewOrNotFound() {
        NoteService service = newService(100);
        service.createNote("Daily Review", "Summarize progress", Set.of());

        OperationResult<NoteView> existing = service.getNote(new EntityId(100));
        OperationResult<NoteView> missing = service.getNote(new EntityId(999));

        assertSuccess(existing);
        assertEquals("Daily Review", existing.getPayload().title());
        assertFailure(missing, ErrorCode.NOT_FOUND, "note not found: 999");
    }

    @Test
    void getNoteRejectsNullId() {
        NoteService service = newService(100);

        assertFailure(service.getNote(null), ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void listNotesReturnsUnmodifiableViewsInRepositoryOrder() {
        NoteService service = newService(100);
        service.createNote("First", "Content", Set.of());
        service.createNote("Second", "Content", Set.of());

        OperationResult<List<NoteView>> result = service.listNotes();

        assertSuccess(result);
        List<NoteView> views = result.getPayload();
        assertEquals(List.of(new EntityId(100), new EntityId(101)), idsOf(views));
        assertThrows(UnsupportedOperationException.class, () -> views.clear());
    }

    @Test
    void listNotesReturnsSuccessEmptyListWhenRepositoryIsEmpty() {
        NoteService service = newService(100);

        OperationResult<List<NoteView>> result = service.listNotes();

        assertSuccess(result);
        assertTrue(result.getPayload().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> result.getPayload().add(
                new NoteView(new EntityId(1), "Title", "Content", JUNE_12, Set.of())));
    }

    @Test
    void listNotesWithQueryFiltersUsingSearchPolicy() {
        NoteService service = serviceWithMixedNotes();

        OperationResult<List<NoteView>> result = service.listNotes(NoteQuery.of("progress", Tag.of("work")));

        assertSuccess(result);
        assertEquals(List.of(new EntityId(100), new EntityId(102)), idsOf(result.getPayload()));
    }

    @Test
    void listNotesWithQueryReturnsSuccessEmptyListWhenNoMatch() {
        NoteService service = serviceWithMixedNotes();

        OperationResult<List<NoteView>> result = service.listNotes(NoteQuery.byTag(Tag.of("missing")));

        assertSuccess(result);
        assertTrue(result.getPayload().isEmpty());
    }

    @Test
    void listNotesWithQueryReturnsSnapshotUnaffectedByLaterChanges() {
        NoteService service = serviceWithMixedNotes();

        List<NoteView> original = service.listNotes(NoteQuery.byTag(Tag.of("work"))).getPayload();
        NoteView first = original.get(0);

        service.updateNote(new EntityId(100), "Updated", "Updated content", Set.of("journal"));
        service.createNote("Later", "Later content", Set.of("work"));

        assertAll(
                () -> assertEquals(List.of(new EntityId(100), new EntityId(102)), idsOf(original)),
                () -> assertEquals("Daily Review", first.title()),
                () -> assertEquals(Set.of(Tag.of("work")), first.tags()));
    }

    @Test
    void listNotesRejectsNullQuery() {
        NoteService service = newService(100);

        assertFailure(service.listNotes(null), ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void searchByKeywordReturnsMatchesAndEmptyListWhenNoMatch() {
        NoteService service = serviceWithMixedNotes();

        OperationResult<List<NoteView>> matched = service.searchByKeyword("review");
        OperationResult<List<NoteView>> missing = service.searchByKeyword("absent");

        assertSuccess(matched);
        assertEquals(List.of(new EntityId(100)), idsOf(matched.getPayload()));
        assertSuccess(missing);
        assertTrue(missing.getPayload().isEmpty());
    }

    @Test
    void searchByKeywordUsesInjectedSearchPolicy() {
        NoteSearchPolicy searchPolicy = Mockito.mock(NoteSearchPolicy.class);
        NoteService service = new NoteService(
                new InMemoryNoteRepository(),
                new IncrementalIdGenerator(100),
                fixedTimeProvider(),
                searchPolicy);
        service.createNote("Daily Review", "Summarize progress", Set.of("work"));
        service.createNote("Travel", "Packing list", Set.of("personal"));
        when(searchPolicy.matchesKeyword(any(Note.class), eq("review"))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            return new EntityId(101).equals(note.getId());
        });

        OperationResult<List<NoteView>> result = service.searchByKeyword(" review ");

        assertSuccess(result);
        assertEquals(List.of(new EntityId(101)), idsOf(result.getPayload()));
        verify(searchPolicy, times(2)).matchesKeyword(any(Note.class), eq("review"));
    }

    @Test
    void searchByKeywordRejectsNullOrBlankKeyword() {
        NoteService service = newService(100);

        assertFailure(service.searchByKeyword(null), ErrorCode.VALIDATION_ERROR);
        assertFailure(service.searchByKeyword(" \t\n"), ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void searchByTagUsesTagSemantics() {
        NoteService service = serviceWithMixedNotes();

        OperationResult<List<NoteView>> result = service.searchByTag(" WORK ");

        assertSuccess(result);
        assertEquals(List.of(new EntityId(100), new EntityId(102)), idsOf(result.getPayload()));
    }

    @Test
    void searchByTagRejectsInvalidTagText() {
        NoteService service = newService(100);

        assertFailure(service.searchByTag(null), ErrorCode.VALIDATION_ERROR);
        assertFailure(service.searchByTag(" "), ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void updateNoteChangesContentAndTags() {
        NoteService service = newService(100);
        service.createNote("Daily Review", "Summarize progress", Set.of("work"));

        OperationResult<NoteView> result = service.updateNote(
                new EntityId(100),
                "Updated",
                "Updated content",
                orderedTextTags("Journal", "journal", "Work"));

        assertSuccess(result);
        NoteView view = result.getPayload();
        assertAll(
                () -> assertEquals("Updated", view.title()),
                () -> assertEquals("Updated content", view.content()),
                () -> assertEquals(List.of(Tag.of("journal"), Tag.of("work")), List.copyOf(view.tags())));
    }

    @Test
    void updateNoteReturnsNotFoundForMissingId() {
        NoteService service = newService(100);

        OperationResult<NoteView> result = service.updateNote(
                new EntityId(999),
                "Updated",
                "Updated content",
                Set.of("work"));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void updateNoteRejectsInvalidInputsAndKeepsStoredState() {
        NoteService service = newService(100);
        service.createNote("Daily Review", "Summarize progress", Set.of("work"));

        assertFailure(service.updateNote(null, "Updated", "Updated content", Set.of("journal")), ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateNote(new EntityId(100), " ", "Updated content", Set.of("journal")),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateNote(new EntityId(100), "Updated", " ", Set.of("journal")),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateNote(new EntityId(100), "Updated", "Updated content", null),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateNote(new EntityId(100), "Updated", "Updated content", orderedTextTags("journal", null)),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateNote(new EntityId(100), "Updated", "Updated content", orderedTextTags("journal", " ")),
                ErrorCode.VALIDATION_ERROR);

        NoteView stored = service.getNote(new EntityId(100)).getPayload();
        assertAll(
                () -> assertEquals("Daily Review", stored.title()),
                () -> assertEquals("Summarize progress", stored.content()),
                () -> assertEquals(Set.of(Tag.of("work")), stored.tags()));
    }

    @Test
    void deleteNoteRemovesExistingNote() {
        NoteService service = newService(100);
        service.createNote("Daily Review", "Summarize progress", Set.of());

        OperationResult<Void> result = service.deleteNote(new EntityId(100));

        assertSuccess(result);
        assertFailure(service.getNote(new EntityId(100)), ErrorCode.NOT_FOUND);
    }

    @Test
    void deleteNoteRejectsNullId() {
        NoteService service = newService(100);

        assertFailure(service.deleteNote(null), ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void deleteNoteReturnsNotFoundForMissingId() {
        NoteService service = newService(100);

        assertFailure(service.deleteNote(new EntityId(999)), ErrorCode.NOT_FOUND);
    }

    @Test
    void returnedViewsDoNotExposeMutableRepositoryState() {
        NoteService service = newService(100);
        service.createNote("Daily Review", "Summarize progress", Set.of("work"));

        NoteView view = service.getNote(new EntityId(100)).getPayload();

        assertThrows(UnsupportedOperationException.class, () -> view.tags().add(Tag.of("later")));
        service.updateNote(new EntityId(100), "Updated", "Updated content", Set.of("journal"));
        assertEquals(Set.of(Tag.of("work")), view.tags());
        assertEquals(Set.of(Tag.of("journal")), service.getNote(new EntityId(100)).getPayload().tags());
    }

    private static NoteService serviceWithMixedNotes() {
        NoteService service = newService(100);
        service.createNote("Daily Review", "Summarize progress", Set.of("work"));
        service.createNote("Travel", "Packing list", Set.of("personal"));
        service.createNote("Sprint", "Track progress", orderedTextTags("work", "planning"));
        return service;
    }

    private static NoteService newService(long firstId) {
        return new NoteService(
                new InMemoryNoteRepository(),
                new IncrementalIdGenerator(firstId),
                fixedTimeProvider(),
                new NoteSearchPolicy());
    }

    private static FixedTimeProvider fixedTimeProvider() {
        return new FixedTimeProvider(JUNE_12_NOON);
    }

    private static LinkedHashSet<String> orderedTextTags(String... tags) {
        LinkedHashSet<String> orderedTags = new LinkedHashSet<>();
        for (String tag : tags) {
            orderedTags.add(tag);
        }
        return orderedTags;
    }

    private static List<EntityId> idsOf(List<NoteView> views) {
        return views.stream().map(NoteView::id).toList();
    }

    private static void assertSuccess(OperationResult<?> result) {
        assertTrue(result.isSuccess());
    }

    private static void assertFailure(OperationResult<?> result, ErrorCode expectedErrorCode) {
        assertTrue(result.isFailure());
        assertEquals(expectedErrorCode, result.getErrorCode());
    }

    private static void assertFailure(
            OperationResult<?> result, ErrorCode expectedErrorCode, String expectedMessage) {
        assertFailure(result, expectedErrorCode);
        assertEquals(expectedMessage, result.getMessage());
    }
}
