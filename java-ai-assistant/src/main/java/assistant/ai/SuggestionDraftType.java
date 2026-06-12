package assistant.ai;

public enum SuggestionDraftType {
    TASK_DRAFT,
    STUDY_PLAN_DRAFT;

    public boolean isTaskDraft() {
        return this == TASK_DRAFT;
    }

    public boolean isStudyPlanDraft() {
        return this == STUDY_PLAN_DRAFT;
    }
}
