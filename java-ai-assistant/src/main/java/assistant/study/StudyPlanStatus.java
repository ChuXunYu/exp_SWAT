package assistant.study;

public enum StudyPlanStatus {
    NOT_STARTED("未开始"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    OVERDUE_INCOMPLETE("逾期未完成");

    private final String displayName;

    StudyPlanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isNotStarted() {
        return this == NOT_STARTED;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isOverdueIncomplete() {
        return this == OVERDUE_INCOMPLETE;
    }
}
