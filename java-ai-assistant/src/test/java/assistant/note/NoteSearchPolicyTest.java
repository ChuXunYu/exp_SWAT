package assistant.note;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.common.Tag;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NoteSearchPolicyTest {
    private static final LocalDate CREATED_DATE = LocalDate.of(2026, 6, 12);

    private final NoteSearchPolicy policy = new NoteSearchPolicy();

    @Test
    void matchesKeywordRejectsNullNoteAndKeyword() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));

        assertAll(
                () -> assertThrowsWithMessage(
                        NullPointerException.class, "note", () -> policy.matchesKeyword(null, "work")),
                () -> assertThrowsWithMessage(
                        NullPointerException.class, "keyword", () -> policy.matchesKeyword(note, null)));
    }

    @Test
    void matchesKeywordRejectsBlankKeyword() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));

        for (String blank : new String[] {"", " \t\n", "\u2003"}) {
            assertThrowsWithMessage(
                    IllegalArgumentException.class,
                    "keyword must not be blank",
                    () -> policy.matchesKeyword(note, blank));
        }
    }

    @Test
    void matchesTitleIgnoringCase() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));

        assertTrue(policy.matchesKeyword(note, "review"));
        assertTrue(policy.matchesKeyword(note, "DAILY"));
    }

    @Test
    void matchesContentIgnoringCase() {
        Note note = note("Daily Review", "Summarize Progress", Set.of(Tag.of("work")));

        assertTrue(policy.matchesKeyword(note, "progress"));
        assertTrue(policy.matchesKeyword(note, "SUMMARIZE"));
    }

    @Test
    void matchesTagUsingTagSemantics() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("Work")));

        assertTrue(policy.matchesKeyword(note, " work "));
        assertTrue(policy.matchesKeyword(note, "WORK"));
    }

    @Test
    void doesNotMatchTagBySubstring() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("journal")));

        assertFalse(policy.matchesKeyword(note, "journ"));
    }

    @Test
    void returnsTrueWhenMultipleFieldsMatch() {
        Note note = note("Work Review", "Work progress", Set.of(Tag.of("work")));

        assertTrue(policy.matchesKeyword(note, "work"));
    }

    @Test
    void returnsFalseWhenNoFieldMatches() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));

        assertFalse(policy.matchesKeyword(note, "travel"));
    }

    @Test
    void textMatchingUsesLocaleRootInsteadOfDefaultLocale() {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Note note = note("FILE Review", "Summarize progress", Set.of(Tag.of("work")));

            assertTrue(policy.matchesKeyword(note, "file"));
        } finally {
            Locale.setDefault(previousLocale);
        }
    }

    @Test
    void tagBranchDoesNotHideTextMatch() {
        Note note = note("Daily Review", "Summarize progress", Set.of(Tag.of("work")));

        assertTrue(policy.matchesKeyword(note, "review"));
        assertTrue(policy.matchesKeyword(note, "progress"));
    }

    @Test
    void invalidTagKeywordStillAllowsTextMatch() {
        Note note = note("Daily #Topic", "Summarize #Topic progress", Set.of(Tag.of("work")));

        assertTrue(policy.matchesKeyword(note, "#topic"));
    }

    private static Note note(String title, String content, Set<Tag> tags) {
        return new Note(new EntityId(1), title, content, CREATED_DATE, tags);
    }

    private static <T extends Throwable> void assertThrowsWithMessage(
            Class<T> expectedType, String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        T exception = assertThrows(expectedType, executable);

        org.junit.jupiter.api.Assertions.assertEquals(expectedMessage, exception.getMessage());
    }
}
