package assistant.note;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.EntityId;
import assistant.common.Tag;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NoteViewTest {
    private static final EntityId ID = new EntityId(1);
    private static final LocalDate CREATED_DATE = LocalDate.of(2026, 6, 12);

    @Test
    void fromProjectsNoteFields() {
        Note note = note("Daily Review", "Summarize progress", orderedTags(Tag.of("work"), Tag.of("journal")));

        NoteView view = NoteView.from(note);

        assertAll(
                () -> assertEquals(ID, view.id()),
                () -> assertEquals("Daily Review", view.title()),
                () -> assertEquals("Summarize progress", view.content()),
                () -> assertEquals(CREATED_DATE, view.createdDate()),
                () -> assertEquals(orderedTags(Tag.of("work"), Tag.of("journal")), view.tags()));
    }

    @Test
    void constructorNormalizesTitleAndContent() {
        NoteView view = new NoteView(ID, "\u2003Title\t", " Content\u2003", CREATED_DATE, Set.of());

        assertAll(
                () -> assertEquals("Title", view.title()),
                () -> assertEquals("Content", view.content()));
    }

    @Test
    void constructorRejectsNullRequiredFields() {
        Set<Tag> tags = Set.of(Tag.of("work"));

        assertAll(
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "id",
                        () -> new NoteView(null, "Title", "Content", CREATED_DATE, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "title",
                        () -> new NoteView(ID, null, "Content", CREATED_DATE, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "content",
                        () -> new NoteView(ID, "Title", null, CREATED_DATE, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "createdDate",
                        () -> new NoteView(ID, "Title", "Content", null, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "tags",
                        () -> new NoteView(ID, "Title", "Content", CREATED_DATE, null)));
    }

    @Test
    void constructorRejectsBlankTitleAndContent() {
        for (String blank : new String[] {"", " \t\n", "\u2003"}) {
            assertThrowsWithMessage(
                    IllegalArgumentException.class,
                    "title must not be blank",
                    () -> new NoteView(ID, blank, "Content", CREATED_DATE, Set.of()));
            assertThrowsWithMessage(
                    IllegalArgumentException.class,
                    "content must not be blank",
                    () -> new NoteView(ID, "Title", blank, CREATED_DATE, Set.of()));
        }
    }

    @Test
    void constructorRejectsNullTagElement() {
        assertThrowsWithMessage(
                NullPointerException.class,
                "tag",
                () -> new NoteView(ID, "Title", "Content", CREATED_DATE, orderedTags(Tag.of("work"), null)));
    }

    @Test
    void tagsAreUnmodifiableSnapshot() {
        Set<Tag> tags = orderedTags(Tag.of("work"));

        NoteView view = new NoteView(ID, "Title", "Content", CREATED_DATE, tags);
        tags.add(Tag.of("later"));

        assertEquals(Set.of(Tag.of("work")), view.tags());
        assertThrows(UnsupportedOperationException.class, () -> view.tags().add(Tag.of("blocked")));
    }

    @Test
    void fromCopiesTagsFromEntity() {
        Note note = note("Daily Review", "Summarize progress", orderedTags(Tag.of("work"), Tag.of("journal")));

        NoteView view = NoteView.from(note);
        note.addTag(Tag.of("later"));
        note.removeTag(Tag.of("work"));

        assertIterableEquals(List.of(Tag.of("work"), Tag.of("journal")), view.tags());
    }

    @Test
    void fromRejectsNullNote() {
        assertThrowsWithMessage(NullPointerException.class, "note", () -> NoteView.from(null));
    }

    private static Note note(String title, String content, Set<Tag> tags) {
        return new Note(ID, title, content, CREATED_DATE, tags);
    }

    private static LinkedHashSet<Tag> orderedTags(Tag... tags) {
        LinkedHashSet<Tag> orderedTags = new LinkedHashSet<>();
        for (Tag tag : tags) {
            orderedTags.add(tag);
        }
        return orderedTags;
    }

    private static <T extends Throwable> void assertThrowsWithMessage(
            Class<T> expectedType, String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        T exception = assertThrows(expectedType, executable);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
