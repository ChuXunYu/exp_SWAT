package assistant.ai;

import assistant.common.EntityId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SuggestionDraftView(
        EntityId id,
        SuggestionDraftType type,
        SuggestionDraftStatus status,
        List<TaskDraftItem> tasks,
        Optional<StudyPlanDraftContent> studyPlan) {
    public SuggestionDraftView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(tasks, "tasks");
        tasks = tasks.stream()
                .map(task -> Objects.requireNonNull(task, "task"))
                .toList();
        studyPlan = Objects.requireNonNull(studyPlan, "studyPlan");
    }

    public static SuggestionDraftView from(SuggestionDraft draft) {
        Objects.requireNonNull(draft, "draft");
        return new SuggestionDraftView(
                draft.getId(),
                draft.getType(),
                draft.getStatus(),
                draft.getTasks(),
                draft.getStudyPlan());
    }

    public boolean isConfirmable() {
        return status.isConfirmable();
    }
}
