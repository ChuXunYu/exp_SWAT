package assistant.note;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.common.Tag;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NoteTest {
    private static final EntityId ID = new EntityId(1);
    private static final LocalDate CREATED_DATE = LocalDate.of(2026, 6, 12);

    @Test
    void constructorStoresNormalizedFieldsAndTags() {
        Note note = new Note(
                ID,
                "\u2003Daily Review\t",
                " \tSummarize progress\u2003",
                CREATED_DATE,
                orderedTags(Tag.of("work"), Tag.of("journal")));

        assertAll(
                () -> assertEquals(ID, note.getId()),
                () -> assertEquals("Daily Review", note.getTitle()),
                () -> assertEquals("Summarize progress", note.getContent()),
                () -> assertEquals(CREATED_DATE, note.getCreatedDate()),
                () -> assertEquals(orderedTags(Tag.of("work"), Tag.of("journal")), note.getTags()));
    }

    @Test
    void createDelegatesToConstructor() {
        Note note = Note.create(ID, "Plan", "Write implementation", CREATED_DATE, Set.of(Tag.of("work")));

        assertAll(
                () -> assertEquals(ID, note.getId()),
                () -> assertEquals("Plan", note.getTitle()),
                () -> assertEquals("Write implementation", note.getContent()),
                () -> assertEquals(Set.of(Tag.of("work")), note.getTags()));
    }

    @Test
    void constructorRejectsNullRequiredFields() {
        Set<Tag> tags = Set.of(Tag.of("work"));

        assertAll(
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "id",
                        () -> new Note(null, "Title", "Content", CREATED_DATE, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "title",
                        () -> new Note(ID, null, "Content", CREATED_DATE, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "content",
                        () -> new Note(ID, "Title", null, CREATED_DATE, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "createdDate",
                        () -> new Note(ID, "Title", "Content", null, tags)),
                () -> assertThrowsWithMessage(
                        NullPointerException.class,
                        "tags",
                        () -> new Note(ID, "Title", "Content", CREATED_DATE, null)));
    }

    @Test
    void constructorRejectsBlankTitleAndContent() {
        for (String blank : new String[] {"", " \t\n", "\u2003"}) {
            assertThrowsWithMessage(
                    IllegalArgumentException.class,
                    "title must not be blank",
                    () -> new Note(ID, blank, "Content", CREATED_DATE, Set.of()));
            assertThrowsWithMessage(
                    IllegalArgumentException.class,
                    "content must not be blank",
                    () -> new Note(ID, "Title", blank, CREATED_DATE, Set.of()));
        }
    }

    @Test
    void constructorRejectsNullTagElement() {
        Set<Tag> tags = orderedTags(Tag.of("work"), null);

        assertThrowsWithMessage(
                NullPointerException.class,
                "tag",
                () -> new Note(ID, "Title", "Content", CREATED_DATE, tags));
    }

    @Test
    void constructorAllowsEmptyTagSet() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, Set.of());

        assertTrue(note.getTags().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> note.getTags().add(Tag.of("work")));
    }

    @Test
    void constructorCopiesInputTagsAndDeduplicatesByTagSemantics() {
        Set<Tag> inputTags = orderedTags(Tag.of("Work"), Tag.of(" work "), Tag.of("journal"));

        Note note = new Note(ID, "Title", "Content", CREATED_DATE, inputTags);
        inputTags.add(Tag.of("later"));

        assertEquals(orderedTags(Tag.of("work"), Tag.of("journal")), note.getTags());
    }

    @Test
    void getTagsPreservesCurrentTagIterationOrder() {
        Note note = new Note(
                ID,
                "Title",
                "Content",
                CREATED_DATE,
                orderedTags(Tag.of("work"), Tag.of("journal"), Tag.of("ideas")));

        assertIterableEquals(List.of(Tag.of("work"), Tag.of("journal"), Tag.of("ideas")), note.getTags());

        note.removeTag(Tag.of("journal"));
        note.addTag(Tag.of("later"));

        assertIterableEquals(List.of(Tag.of("work"), Tag.of("ideas"), Tag.of("later")), note.getTags());
    }

    @Test
    void getTagsReturnsUnmodifiableSnapshot() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, orderedTags(Tag.of("work")));
        Set<Tag> snapshot = note.getTags();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(Tag.of("blocked")));

        note.addTag(Tag.of("journal"));
        note.removeTag(Tag.of("work"));

        assertEquals(Set.of(Tag.of("work")), snapshot);
        assertEquals(Set.of(Tag.of("journal")), note.getTags());
    }

    @Test
    void updateContentChangesTitleAndContentOnly() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, orderedTags(Tag.of("work")));

        note.updateContent("\u2003New title\u2003", " \tNew content ");

        assertAll(
                () -> assertEquals(ID, note.getId()),
                () -> assertEquals("New title", note.getTitle()),
                () -> assertEquals("New content", note.getContent()),
                () -> assertEquals(CREATED_DATE, note.getCreatedDate()),
                () -> assertEquals(Set.of(Tag.of("work")), note.getTags()));
    }

    @Test
    void updateContentRejectsInvalidInputAndKeepsOldState() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, Set.of(Tag.of("work")));

        assertThrows(NullPointerException.class, () -> note.updateContent(null, "Changed"));
        assertNoteText(note, "Title", "Content");

        assertThrows(IllegalArgumentException.class, () -> note.updateContent("\u2003", "Changed"));
        assertNoteText(note, "Title", "Content");

        assertThrows(NullPointerException.class, () -> note.updateContent("Changed", null));
        assertNoteText(note, "Title", "Content");

        assertThrows(IllegalArgumentException.class, () -> note.updateContent("Changed", "\u2003"));
        assertNoteText(note, "Title", "Content");
    }

    @Test
    void replaceTagsReplacesWithValidatedSnapshot() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, Set.of(Tag.of("old")));
        Set<Tag> newTags = orderedTags(Tag.of("work"), Tag.of("journal"));

        note.replaceTags(newTags);
        newTags.add(Tag.of("later"));

        assertEquals(orderedTags(Tag.of("work"), Tag.of("journal")), note.getTags());
    }

    @Test
    void replaceTagsRejectsInvalidInputAndKeepsOldTags() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, orderedTags(Tag.of("old")));

        assertThrows(NullPointerException.class, () -> note.replaceTags(null));
        assertEquals(Set.of(Tag.of("old")), note.getTags());

        assertThrows(NullPointerException.class, () -> note.replaceTags(orderedTags(Tag.of("work"), null)));
        assertEquals(Set.of(Tag.of("old")), note.getTags());
    }

    @Test
    void addTagAddsOnlyNewTagSemantics() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, orderedTags(Tag.of("work")));

        assertTrue(note.addTag(Tag.of("journal")));
        assertFalse(note.addTag(Tag.of(" WORK ")));
        assertEquals(orderedTags(Tag.of("work"), Tag.of("journal")), note.getTags());
    }

    @Test
    void addTagRejectsNullTag() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, Set.of());

        assertThrowsWithMessage(NullPointerException.class, "tag", () -> note.addTag(null));
    }

    @Test
    void removeTagRemovesExistingTagAndReturnsFalseForMissing() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, orderedTags(Tag.of("work")));

        assertTrue(note.removeTag(Tag.of(" WORK ")));
        assertFalse(note.removeTag(Tag.of("missing")));
        assertTrue(note.getTags().isEmpty());
    }

    @Test
    void removeTagRejectsNullTag() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, Set.of());

        assertThrowsWithMessage(NullPointerException.class, "tag", () -> note.removeTag(null));
    }

    @Test
    void hasTagUsesTagValueSemantics() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, orderedTags(Tag.of("Work")));

        assertTrue(note.hasTag(Tag.of(" work ")));
        assertFalse(note.hasTag(Tag.of("journal")));
    }

    @Test
    void hasTagRejectsNullTag() {
        Note note = new Note(ID, "Title", "Content", CREATED_DATE, Set.of());

        assertThrowsWithMessage(NullPointerException.class, "tag", () -> note.hasTag(null));
    }

    private static void assertNoteText(Note note, String title, String content) {
        assertAll(() -> assertEquals(title, note.getTitle()), () -> assertEquals(content, note.getContent()));
    }

    private static <T extends Throwable> void assertThrowsWithMessage(
            Class<T> expectedType, String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        T exception = assertThrows(expectedType, executable);

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static Set<Tag> orderedTags(Tag... tags) {
        LinkedHashSet<Tag> orderedTags = new LinkedHashSet<>();
        for (Tag tag : tags) {
            orderedTags.add(tag);
        }
        return orderedTags;
    }
}
