package assistant.ai;

import assistant.common.BusinessException;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SuggestionDraft {
    private final EntityId id;
    private final SuggestionDraftType type;
    private SuggestionDraftStatus status;
    private final List<TaskDraftItem> tasks;
    private final StudyPlanDraftContent studyPlan;

    private SuggestionDraft(
            EntityId id,
            SuggestionDraftType type,
            List<TaskDraftItem> tasks,
            StudyPlanDraftContent studyPlan) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.status = SuggestionDraftStatus.CONFIRMABLE;
        this.tasks = copyTasks(tasks);
        this.studyPlan = studyPlan;
        validateContentShape();
    }

    public static SuggestionDraft forTasks(EntityId id, List<TaskDraftItem> tasks) {
        return new SuggestionDraft(id, SuggestionDraftType.TASK_DRAFT, tasks, null);
    }

    public static SuggestionDraft forStudyPlan(EntityId id, StudyPlanDraftContent studyPlan) {
        return new SuggestionDraft(
                id,
                SuggestionDraftType.STUDY_PLAN_DRAFT,
                List.of(),
                Objects.requireNonNull(studyPlan, "studyPlan"));
    }

    public EntityId getId() {
        return id;
    }

    public SuggestionDraftType getType() {
        return type;
    }

    public SuggestionDraftStatus getStatus() {
        return status;
    }

    public List<TaskDraftItem> getTasks() {
        return tasks;
    }

    public Optional<StudyPlanDraftContent> getStudyPlan() {
        return Optional.ofNullable(studyPlan);
    }

    public boolean isConfirmable() {
        return status.isConfirmable();
    }

    public void cancel() {
        requireConfirmable();
        status = SuggestionDraftStatus.CANCELLED;
    }

    public void markImported() {
        requireConfirmable();
        status = SuggestionDraftStatus.IMPORTED;
    }

    private static List<TaskDraftItem> copyTasks(List<TaskDraftItem> tasks) {
        Objects.requireNonNull(tasks, "tasks");
        return tasks.stream()
                .map(task -> Objects.requireNonNull(task, "task"))
                .toList();
    }

    private void validateContentShape() {
        if (type.isTaskDraft()) {
            if (tasks.isEmpty()) {
                throw new IllegalArgumentException("tasks must not be empty");
            }
            if (studyPlan != null) {
                throw new IllegalArgumentException("studyPlan must be null for task draft");
            }
            return;
        }
        if (!tasks.isEmpty()) {
            throw new IllegalArgumentException("tasks must be empty for study plan draft");
        }
        if (studyPlan == null) {
            throw new NullPointerException("studyPlan");
        }
    }

    private void requireConfirmable() {
        if (!status.isConfirmable()) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable");
        }
    }
}
