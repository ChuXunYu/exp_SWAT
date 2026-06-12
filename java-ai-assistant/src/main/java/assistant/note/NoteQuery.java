package assistant.note;

import assistant.common.Tag;
import java.util.Objects;

public record NoteQuery(String keyword, Tag tag) {
    public NoteQuery {
        if (keyword != null) {
            keyword = keyword.strip();
            if (keyword.isEmpty()) {
                throw new IllegalArgumentException("keyword must not be blank");
            }
        }
    }

    public static NoteQuery all() {
        return new NoteQuery(null, null);
    }

    public static NoteQuery byKeyword(String keyword) {
        Objects.requireNonNull(keyword, "keyword");
        return new NoteQuery(keyword, null);
    }

    public static NoteQuery byTag(Tag tag) {
        Objects.requireNonNull(tag, "tag");
        return new NoteQuery(null, tag);
    }

    public static NoteQuery of(String keyword, Tag tag) {
        Objects.requireNonNull(keyword, "keyword");
        Objects.requireNonNull(tag, "tag");
        return new NoteQuery(keyword, tag);
    }

    public boolean hasKeywordFilter() {
        return keyword != null;
    }

    public boolean hasTagFilter() {
        return tag != null;
    }

    public boolean matches(Note note, NoteSearchPolicy searchPolicy) {
        Objects.requireNonNull(note, "note");
        Objects.requireNonNull(searchPolicy, "searchPolicy");
        return (!hasKeywordFilter() || searchPolicy.matchesKeyword(note, keyword))
                && (!hasTagFilter() || note.hasTag(tag));
    }
}
