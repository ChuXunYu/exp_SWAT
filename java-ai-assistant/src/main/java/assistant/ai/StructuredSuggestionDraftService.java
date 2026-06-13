package assistant.ai;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.testability.IdGenerator;
import java.util.Objects;

public final class StructuredSuggestionDraftService {
    private final AiAssistantService aiAssistantService;
    private final StructuredSuggestionParser parser;
    private final SuggestionDraftRepository repository;
    private final IdGenerator idGenerator;

    public StructuredSuggestionDraftService(
            AiAssistantService aiAssistantService,
            StructuredSuggestionParser parser,
            SuggestionDraftRepository repository,
            IdGenerator idGenerator) {
        this.aiAssistantService = Objects.requireNonNull(aiAssistantService, "aiAssistantService");
        this.parser = Objects.requireNonNull(parser, "parser");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public OperationResult<SuggestionDraftView> generateTaskDraft(String userGoal) {
        return generate(AiScenario.STRUCTURED_TASK_SUGGESTION, userGoal, SuggestionDraftType.TASK_DRAFT);
    }

    public OperationResult<SuggestionDraftView> generateStudyPlanDraft(String userGoal) {
        return generate(AiScenario.STRUCTURED_STUDY_PLAN_SUGGESTION, userGoal, SuggestionDraftType.STUDY_PLAN_DRAFT);
    }

    private OperationResult<SuggestionDraftView> generate(
            AiScenario scenario, String userGoal, SuggestionDraftType expectedType) {
        if (userGoal == null || userGoal.isBlank()) {
            return failure(ErrorCode.VALIDATION_ERROR, "user goal must not be blank");
        }

        EntityId draftId = idGenerator.nextId();
        OperationResult<String> aiResult = aiAssistantService.ask(scenario, userGoal);
        if (aiResult.isFailure()) {
            return toViewFailure(aiResult);
        }

        OperationResult<SuggestionDraft> parseResult = parser.parse(aiResult.getPayload(), draftId);
        if (parseResult.isFailure()) {
            return toViewFailure(parseResult);
        }

        SuggestionDraft draft = parseResult.getPayload();
        OperationResult<SuggestionDraftView> validationResult = validateGeneratedDraft(draft, expectedType);
        if (validationResult.isFailure()) {
            return validationResult;
        }

        repository.save(draft);
        return OperationResult.success(SuggestionDraftView.from(draft));
    }

    private OperationResult<SuggestionDraftView> validateGeneratedDraft(
            SuggestionDraft draft, SuggestionDraftType expectedType) {
        if (draft.getType() != expectedType) {
            return failure(
                    ErrorCode.VALIDATION_ERROR,
                    "AI structured suggestion type does not match requested draft type");
        }
        if (expectedType.isTaskDraft()) {
            return validateTaskDueDates(draft);
        }
        return OperationResult.success(SuggestionDraftView.from(draft));
    }

    private OperationResult<SuggestionDraftView> validateTaskDueDates(SuggestionDraft draft) {
        boolean missingDueDate = draft.getTasks().stream().anyMatch(task -> !task.hasDueDate());
        if (missingDueDate) {
            return failure(ErrorCode.VALIDATION_ERROR, "task draft dueDate is required before saving");
        }
        return OperationResult.success(SuggestionDraftView.from(draft));
    }

    private static OperationResult<SuggestionDraftView> toViewFailure(OperationResult<?> result) {
        return failure(result.getErrorCode(), result.getMessage());
    }

    private static OperationResult<SuggestionDraftView> failure(ErrorCode errorCode, String message) {
        return OperationResult.failure(errorCode, message);
    }
}
