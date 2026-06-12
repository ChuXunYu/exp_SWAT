package assistant.note;

import assistant.common.EntityId;
import assistant.common.Tag;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record NoteView(EntityId id, String title, String content, LocalDate createdDate, Set<Tag> tags) {
    public NoteView {
        Objects.requireNonNull(id, "id");
        title = Objects.requireNonNull(title, "title").strip();
        content = Objects.requireNonNull(content, "content").strip();
        Objects.requireNonNull(createdDate, "createdDate");
        Objects.requireNonNull(tags, "tags");
        if (title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }

        LinkedHashSet<Tag> copiedTags = new LinkedHashSet<>();
        for (Tag tag : tags) {
            copiedTags.add(Objects.requireNonNull(tag, "tag"));
        }
        tags = Collections.unmodifiableSet(copiedTags);
    }

    public static NoteView from(Note note) {
        Objects.requireNonNull(note, "note");
        return new NoteView(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedDate(),
                note.getTags());
    }
}
