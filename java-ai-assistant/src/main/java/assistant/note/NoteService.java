package assistant.note;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Tag;
import assistant.testability.IdGenerator;
import assistant.testability.TimeProvider;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class NoteService {
    private final NoteRepository repository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final NoteSearchPolicy searchPolicy;

    public NoteService(
            NoteRepository repository,
            IdGenerator idGenerator,
            TimeProvider timeProvider,
            NoteSearchPolicy searchPolicy) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider");
        this.searchPolicy = Objects.requireNonNull(searchPolicy, "searchPolicy");
    }

    public OperationResult<NoteView> createNote(String title, String content, Set<String> tagTexts) {
        try {
            Set<Tag> tags = toTags(tagTexts);
            Note note = Note.create(idGenerator.nextId(), title, content, timeProvider.today(), tags);
            repository.save(note);
            return OperationResult.success(toView(note));
        } catch (NullPointerException | IllegalArgumentException exception) {
            return validationFailure(exception.getMessage());
        }
    }

    public OperationResult<NoteView> updateNote(EntityId id, String title, String content, Set<String> tagTexts) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id).map(note -> {
            try {
                Set<Tag> tags = toTags(tagTexts);
                note.updateContent(title, content);
                note.replaceTags(tags);
                repository.save(note);
                return OperationResult.success(toView(note));
            } catch (NullPointerException | IllegalArgumentException exception) {
                return validationFailure(exception.getMessage());
            }
        }).orElseGet(() -> notFound(id));
    }

    public OperationResult<NoteView> getNote(EntityId id) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id)
                .map(note -> OperationResult.success(toView(note)))
                .orElseGet(() -> notFound(id));
    }

    public OperationResult<List<NoteView>> listNotes() {
        return OperationResult.success(toUnmodifiableViews(repository.findAll()));
    }

    public OperationResult<List<NoteView>> listNotes(NoteQuery query) {
        if (query == null) {
            return validationFailureList("query must not be null");
        }
        return OperationResult.success(toUnmodifiableViews(repository.findBy(query, searchPolicy)));
    }

    public OperationResult<List<NoteView>> searchByKeyword(String keyword) {
        try {
            return listNotes(NoteQuery.byKeyword(keyword));
        } catch (NullPointerException | IllegalArgumentException exception) {
            return validationFailureList(exception.getMessage());
        }
    }

    public OperationResult<List<NoteView>> searchByTag(String tagText) {
        try {
            return listNotes(NoteQuery.byTag(Tag.of(tagText)));
        } catch (NullPointerException | IllegalArgumentException exception) {
            return validationFailureList(exception.getMessage());
        }
    }

    public OperationResult<Void> deleteNote(EntityId id) {
        if (id == null) {
            return validationFailureVoid("id must not be null");
        }
        if (!repository.deleteById(id)) {
            return notFoundVoid(id);
        }
        return OperationResult.success();
    }

    private Set<Tag> toTags(Set<String> tagTexts) {
        Objects.requireNonNull(tagTexts, "tagTexts");
        LinkedHashSet<Tag> tags = new LinkedHashSet<>();
        for (String tagText : tagTexts) {
            if (tagText == null) {
                throw new NullPointerException("tagText");
            }
            tags.add(Tag.of(tagText));
        }
        return tags;
    }

    private static NoteView toView(Note note) {
        return NoteView.from(note);
    }

    private static List<NoteView> toUnmodifiableViews(List<Note> notes) {
        return notes.stream().map(NoteService::toView).toList();
    }

    private OperationResult<NoteView> validationFailure(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message));
    }

    private OperationResult<List<NoteView>> validationFailureList(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message));
    }

    private OperationResult<Void> validationFailureVoid(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message));
    }

    private OperationResult<NoteView> notFound(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "note not found: " + id.value());
    }

    private OperationResult<Void> notFoundVoid(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "note not found: " + id.value());
    }

    private static String stableMessage(String message) {
        if (message == null || message.isBlank()) {
            return "invalid note input";
        }
        return message;
    }
}
