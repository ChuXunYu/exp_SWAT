package assistant.study;

import java.time.LocalDate;
import java.util.Objects;

public final class StudyPlanAnalysisService {
    public StudyPlanStatus analyzeStatus(StudyPlan plan, LocalDate currentDate) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(currentDate, "currentDate");

        if (plan.getProgress().isComplete()) {
            return StudyPlanStatus.COMPLETED;
        }
        if (currentDate.isAfter(plan.getEndDate())) {
            return StudyPlanStatus.OVERDUE_INCOMPLETE;
        }
        if (currentDate.isBefore(plan.getStartDate())) {
            return StudyPlanStatus.NOT_STARTED;
        }
        return StudyPlanStatus.IN_PROGRESS;
    }

    public boolean isCompleted(StudyPlan plan) {
        Objects.requireNonNull(plan, "plan");
        return plan.getProgress().isComplete();
    }

    public boolean isOverdueIncomplete(StudyPlan plan, LocalDate currentDate) {
        return analyzeStatus(plan, currentDate).isOverdueIncomplete();
    }
}
