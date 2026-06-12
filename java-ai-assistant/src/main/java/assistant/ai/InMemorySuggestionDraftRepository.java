package assistant.ai;

import assistant.common.EntityId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemorySuggestionDraftRepository implements SuggestionDraftRepository {
    private final Map<EntityId, SuggestionDraft> drafts = new LinkedHashMap<>();

    @Override
    public void save(SuggestionDraft draft) {
        Objects.requireNonNull(draft, "draft");
        drafts.put(draft.getId(), draft);
    }

    @Override
    public Optional<SuggestionDraft> findById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(drafts.get(id));
    }

    @Override
    public List<SuggestionDraft> findAll() {
        return List.copyOf(drafts.values());
    }

    @Override
    public boolean deleteById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return drafts.remove(id) != null;
    }
}
