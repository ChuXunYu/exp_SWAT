package assistant.note;

import assistant.common.EntityId;
import assistant.common.Tag;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class Note {
    private final EntityId id;
    private String title;
    private String content;
    private final LocalDate createdDate;
    private Set<Tag> tags;

    public Note(EntityId id, String title, String content, LocalDate createdDate, Set<Tag> tags) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = normalizeTitle(title);
        this.content = normalizeContent(content);
        this.createdDate = Objects.requireNonNull(createdDate, "createdDate");
        this.tags = copyTags(tags);
    }

    public static Note create(EntityId id, String title, String content, LocalDate createdDate, Set<Tag> tags) {
        return new Note(id, title, content, createdDate, tags);
    }

    public EntityId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(tags));
    }

    public void updateContent(String title, String content) {
        String normalizedTitle = normalizeTitle(title);
        String normalizedContent = normalizeContent(content);

        this.title = normalizedTitle;
        this.content = normalizedContent;
    }

    public void replaceTags(Set<Tag> tags) {
        this.tags = copyTags(tags);
    }

    public boolean addTag(Tag tag) {
        return tags.add(Objects.requireNonNull(tag, "tag"));
    }

    public boolean removeTag(Tag tag) {
        return tags.remove(Objects.requireNonNull(tag, "tag"));
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(Objects.requireNonNull(tag, "tag"));
    }

    private static String normalizeTitle(String title) {
        String normalizedTitle = Objects.requireNonNull(title, "title").strip();
        if (normalizedTitle.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return normalizedTitle;
    }

    private static String normalizeContent(String content) {
        String normalizedContent = Objects.requireNonNull(content, "content").strip();
        if (normalizedContent.isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        return normalizedContent;
    }

    private static LinkedHashSet<Tag> copyTags(Set<Tag> tags) {
        Objects.requireNonNull(tags, "tags");
        LinkedHashSet<Tag> copy = new LinkedHashSet<>();
        for (Tag tag : tags) {
            copy.add(Objects.requireNonNull(tag, "tag"));
        }
        return copy;
    }
}
