package assistant.note;

import assistant.common.EntityId;
import java.util.List;
import java.util.Optional;

public interface NoteRepository {
    void save(Note note);

    Optional<Note> findById(EntityId id);

    List<Note> findAll();

    List<Note> findBy(NoteQuery query, NoteSearchPolicy searchPolicy);

    boolean deleteById(EntityId id);
}
