package assistant.ai;

import assistant.common.BusinessException;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.study.StudyPlanService;
import assistant.study.StudyPlanView;
import assistant.task.TaskService;
import assistant.task.TaskView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DraftImportService {
    private final TaskService taskService;
    private final StudyPlanService studyPlanService;

    public DraftImportService(TaskService taskService, StudyPlanService studyPlanService) {
        this.taskService = Objects.requireNonNull(taskService, "taskService");
        this.studyPlanService = Objects.requireNonNull(studyPlanService, "studyPlanService");
    }

    public OperationResult<Void> importDraft(SuggestionDraft draft) {
        if (draft == null) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "draft must not be null");
        }
        try {
            if (draft.getType().isTaskDraft()) {
                return importTasks(draft);
            }
            return importStudyPlan(draft);
        } catch (BusinessException exception) {
            return businessFailure(exception);
        } catch (RuntimeException exception) {
            return systemFailure(exception);
        }
    }

    private OperationResult<Void> importTasks(SuggestionDraft draft) {
        List<TaskDraftItem> tasks = draft.getTasks();
        OperationResult<Void> dueDateValidation = validateTaskDueDates(tasks);
        if (dueDateValidation.isFailure()) {
            return dueDateValidation;
        }
        return createTasks(tasks);
    }

    private OperationResult<Void> validateTaskDueDates(List<TaskDraftItem> tasks) {
        for (TaskDraftItem task : tasks) {
            if (!task.hasDueDate()) {
                return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "task draft dueDate must not be null");
            }
        }
        return OperationResult.success();
    }

    private OperationResult<Void> createTasks(List<TaskDraftItem> tasks) {
        List<EntityId> createdIds = new ArrayList<>();
        try {
            for (TaskDraftItem task : tasks) {
                OperationResult<TaskView> result =
                        taskService.createTask(task.title(), task.description(), task.priority(), task.dueDate());
                if (result.isFailure()) {
                    rollbackCreatedTasks(createdIds);
                    return toFailure(result);
                }
                createdIds.add(result.getPayload().id());
            }
            return OperationResult.success();
        } catch (RuntimeException exception) {
            rollbackCreatedTasks(createdIds);
            throw exception;
        }
    }

    private OperationResult<Void> importStudyPlan(SuggestionDraft draft) {
        StudyPlanDraftContent content = draft.getStudyPlan().orElseThrow(() ->
                new IllegalStateException("studyPlan must be present for study plan draft"));
        OperationResult<StudyPlanView> result = studyPlanService.createStudyPlan(
                content.goalName(),
                content.startDate(),
                content.endDate(),
                content.expectedHours(),
                content.initialProgress().value());
        if (result.isFailure()) {
            return toFailure(result);
        }
        return OperationResult.success();
    }

    private void rollbackCreatedTasks(List<EntityId> createdIds) {
        for (EntityId id : createdIds) {
            try {
                taskService.deleteTask(id);
            } catch (RuntimeException ignored) {
                // Best-effort rollback: preserve the original import failure.
            }
        }
    }

    private OperationResult<Void> toFailure(OperationResult<?> result) {
        return OperationResult.failure(result.getErrorCode(), result.getMessage());
    }

    private OperationResult<Void> businessFailure(BusinessException exception) {
        return OperationResult.failure(exception.getErrorCode(), exception.getMessage());
    }

    private OperationResult<Void> systemFailure(RuntimeException exception) {
        return OperationResult.failure(ErrorCode.SYSTEM_ERROR, "failed to import suggestion draft");
    }
}
