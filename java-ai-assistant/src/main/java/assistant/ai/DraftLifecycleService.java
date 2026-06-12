package assistant.ai;

import assistant.common.BusinessException;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import java.util.List;
import java.util.Objects;

public final class DraftLifecycleService {
    private final SuggestionDraftRepository repository;
    private final DraftImportService importService;

    public DraftLifecycleService(SuggestionDraftRepository repository, DraftImportService importService) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.importService = Objects.requireNonNull(importService, "importService");
    }

    public OperationResult<SuggestionDraftView> getDraft(EntityId id) {
        OperationResult<SuggestionDraft> draftResult = findDraft(id);
        if (draftResult.isFailure()) {
            return toViewFailure(draftResult);
        }
        return OperationResult.success(toView(draftResult.getPayload()));
    }

    public OperationResult<List<SuggestionDraftView>> listDrafts() {
        return OperationResult.success(repository.findAll().stream()
                .map(this::toView)
                .toList());
    }

    public OperationResult<SuggestionDraftView> cancelDraft(EntityId id) {
        OperationResult<SuggestionDraft> draftResult = findDraft(id);
        if (draftResult.isFailure()) {
            return toViewFailure(draftResult);
        }
        SuggestionDraft draft = draftResult.getPayload();
        OperationResult<SuggestionDraftView> confirmableResult = ensureConfirmable(draft);
        if (confirmableResult.isFailure()) {
            return confirmableResult;
        }
        try {
            draft.cancel();
            repository.save(draft);
            return OperationResult.success(toView(draft));
        } catch (BusinessException exception) {
            return businessFailure(exception);
        }
    }

    public OperationResult<SuggestionDraftView> confirmDraft(EntityId id) {
        OperationResult<SuggestionDraft> draftResult = findDraft(id);
        if (draftResult.isFailure()) {
            return toViewFailure(draftResult);
        }
        SuggestionDraft draft = draftResult.getPayload();
        OperationResult<SuggestionDraftView> confirmableResult = ensureConfirmable(draft);
        if (confirmableResult.isFailure()) {
            return confirmableResult;
        }
        OperationResult<Void> importResult = importService.importDraft(draft);
        if (importResult.isFailure()) {
            return toViewFailure(importResult);
        }
        try {
            draft.markImported();
            repository.save(draft);
            return OperationResult.success(toView(draft));
        } catch (BusinessException exception) {
            return businessFailure(exception);
        }
    }

    private OperationResult<SuggestionDraft> findDraft(EntityId id) {
        if (id == null) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "id must not be null");
        }
        return repository.findById(id)
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure(
                        ErrorCode.NOT_FOUND, "suggestion draft not found: " + id.value()));
    }

    private OperationResult<SuggestionDraftView> ensureConfirmable(SuggestionDraft draft) {
        if (!draft.isConfirmable()) {
            return OperationResult.failure(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable");
        }
        return OperationResult.success(toView(draft));
    }

    private SuggestionDraftView toView(SuggestionDraft draft) {
        return SuggestionDraftView.from(draft);
    }

    private OperationResult<SuggestionDraftView> toViewFailure(OperationResult<?> result) {
        return OperationResult.failure(result.getErrorCode(), result.getMessage());
    }

    private OperationResult<SuggestionDraftView> businessFailure(BusinessException exception) {
        return OperationResult.failure(exception.getErrorCode(), exception.getMessage());
    }
}
