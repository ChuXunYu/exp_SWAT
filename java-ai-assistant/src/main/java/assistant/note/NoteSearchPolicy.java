package assistant.note;

import assistant.common.Tag;
import java.util.Locale;
import java.util.Objects;

public final class NoteSearchPolicy {
    public NoteSearchPolicy() {}

    public boolean matchesKeyword(Note note, String keyword) {
        Objects.requireNonNull(note, "note");
        String normalizedKeyword = normalizeKeyword(keyword);

        return containsIgnoreCase(note.getTitle(), normalizedKeyword)
                || containsIgnoreCase(note.getContent(), normalizedKeyword)
                || matchesTag(note, normalizedKeyword);
    }

    private static String normalizeKeyword(String keyword) {
        String normalizedKeyword = Objects.requireNonNull(keyword, "keyword").strip();
        if (normalizedKeyword.isEmpty()) {
            throw new IllegalArgumentException("keyword must not be blank");
        }
        return normalizedKeyword;
    }

    private static boolean containsIgnoreCase(String text, String keyword) {
        return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private static boolean matchesTag(Note note, String keyword) {
        try {
            return note.getTags().contains(Tag.of(keyword));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
