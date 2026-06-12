package assistant.note;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.EntityId;
import assistant.common.Tag;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NoteQueryTest {
    private static final LocalDate CREATED_DATE = LocalDate.of(2026, 6, 12);

    @Test
    void allMatchesEveryNoteWithoutDelegatingKeywordPolicy() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));
        NoteSearchPolicy searchPolicy = Mockito.mock(NoteSearchPolicy.class);

        NoteQuery query = NoteQuery.all();

        assertAll(
                () -> assertFalse(query.hasKeywordFilter()),
                () -> assertFalse(query.hasTagFilter()),
                () -> assertTrue(query.matches(note, searchPolicy)));
        verify(searchPolicy, never()).matchesKeyword(Mockito.any(), Mockito.any());
    }

    @Test
    void byKeywordDelegatesToSearchPolicy() {
        Note naturalHit = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));
        Note naturalMiss = note("Travel", "Packing list", Set.of(Tag.of("personal")));
        NoteSearchPolicy searchPolicy = Mockito.mock(NoteSearchPolicy.class);
        when(searchPolicy.matchesKeyword(naturalHit, "review")).thenReturn(false);
        when(searchPolicy.matchesKeyword(naturalMiss, "review")).thenReturn(true);

        assertAll(
                () -> assertFalse(NoteQuery.byKeyword(" review ").matches(naturalHit, searchPolicy)),
                () -> assertTrue(NoteQuery.byKeyword(" review ").matches(naturalMiss, searchPolicy)));
        verify(searchPolicy).matchesKeyword(naturalHit, "review");
        verify(searchPolicy).matchesKeyword(naturalMiss, "review");
    }

    @Test
    void byTagUsesNoteTagSemantics() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("Work")));

        assertAll(
                () -> assertTrue(NoteQuery.byTag(Tag.of(" work ")).matches(note, new NoteSearchPolicy())),
                () -> assertFalse(NoteQuery.byTag(Tag.of("journal")).matches(note, new NoteSearchPolicy())));
    }

    @Test
    void combinedQueryRequiresKeywordAndTag() {
        NoteSearchPolicy searchPolicy = new NoteSearchPolicy();
        Note matching = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));
        Note wrongTag = note("Daily Review", "Summarize progress", Set.of(Tag.of("journal")));
        Note wrongKeyword = note("Travel", "Packing", Set.of(Tag.of("work")));

        NoteQuery query = NoteQuery.of("review", Tag.of("work"));

        assertAll(
                () -> assertTrue(query.matches(matching, searchPolicy)),
                () -> assertFalse(query.matches(wrongTag, searchPolicy)),
                () -> assertFalse(query.matches(wrongKeyword, searchPolicy)));
    }

    @Test
    void factoryMethodsRejectNullRequiredArguments() {
        Tag tag = Tag.of("work");

        assertAll(
                () -> assertThrowsWithMessage(NullPointerException.class, "keyword", () -> NoteQuery.byKeyword(null)),
                () -> assertThrowsWithMessage(NullPointerException.class, "tag", () -> NoteQuery.byTag(null)),
                () -> assertThrowsWithMessage(NullPointerException.class, "keyword", () -> NoteQuery.of(null, tag)),
                () -> assertThrowsWithMessage(NullPointerException.class, "tag", () -> NoteQuery.of("work", null)));
    }

    @Test
    void constructorRejectsBlankKeyword() {
        for (String blank : new String[] {"", " \t\n", "\u2003"}) {
            assertThrowsWithMessage(
                    IllegalArgumentException.class,
                    "keyword must not be blank",
                    () -> new NoteQuery(blank, null));
        }
    }

    @Test
    void matchesRejectsNullNoteAndSearchPolicy() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));
        NoteQuery query = NoteQuery.all();

        assertAll(
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "note",
                        () -> query.matches(null, new NoteSearchPolicy())),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "searchPolicy",
                        () -> query.matches(note, null)));
    }

    private static Note note(String title, String content, Set<Tag> tags) {
        return new Note(new EntityId(1), title, content, CREATED_DATE, tags);
    }

    private static <T extends Throwable> void assertThrowsWithMessage(
            Class<T> expectedType, String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        T exception = assertThrows(expectedType, executable);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
