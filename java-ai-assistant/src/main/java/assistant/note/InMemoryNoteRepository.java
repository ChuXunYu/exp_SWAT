package assistant.note;

import assistant.common.EntityId;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryNoteRepository implements NoteRepository {
    private final Map<EntityId, Note> notes = new LinkedHashMap<>();

    @Override
    public void save(Note note) {
        Objects.requireNonNull(note, "note");
        notes.put(note.getId(), copyOf(note));
    }

    @Override
    public Optional<Note> findById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(notes.get(id)).map(InMemoryNoteRepository::copyOf);
    }

    @Override
    public List<Note> findAll() {
        return copyList(notes.values());
    }

    @Override
    public List<Note> findBy(NoteQuery query, NoteSearchPolicy searchPolicy) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(searchPolicy, "searchPolicy");
        return notes.values().stream()
                .filter(note -> query.matches(note, searchPolicy))
                .map(InMemoryNoteRepository::copyOf)
                .toList();
    }

    @Override
    public boolean deleteById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return notes.remove(id) != null;
    }

    private static Note copyOf(Note source) {
        Objects.requireNonNull(source, "source");
        return new Note(
                source.getId(),
                source.getTitle(),
                source.getContent(),
                source.getCreatedDate(),
                source.getTags());
    }

    private static List<Note> copyList(Collection<Note> sources) {
        return sources.stream().map(InMemoryNoteRepository::copyOf).toList();
    }
}
