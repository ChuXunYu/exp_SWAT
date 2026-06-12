package assistant.ai;

import assistant.common.EntityId;
import java.util.List;
import java.util.Optional;

public interface SuggestionDraftRepository {
    void save(SuggestionDraft draft);

    Optional<SuggestionDraft> findById(EntityId id);

    List<SuggestionDraft> findAll();

    boolean deleteById(EntityId id);
}
