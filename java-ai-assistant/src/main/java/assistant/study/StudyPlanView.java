package assistant.study;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import java.time.LocalDate;
import java.util.Objects;

public record StudyPlanView(
        EntityId id,
        String goalName,
        DateRange period,
        LocalDate startDate,
        LocalDate endDate,
        int expectedHours,
        Progress progress,
        StudyPlanStatus status) {
    public StudyPlanView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(goalName, "goalName");
        Objects.requireNonNull(period, "period");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(status, "status");
        if (goalName.strip().isEmpty()) {
            throw new IllegalArgumentException("goalName must not be blank");
        }
        if (expectedHours <= 0) {
            throw new IllegalArgumentException("expectedHours must be positive");
        }
        if (!startDate.equals(period.startDate())) {
            throw new IllegalArgumentException("startDate must match period");
        }
        if (!endDate.equals(period.endDate())) {
            throw new IllegalArgumentException("endDate must match period");
        }
    }

    public static StudyPlanView from(StudyPlan plan, StudyPlanAnalysisService analysisService, LocalDate currentDate) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(analysisService, "analysisService");
        Objects.requireNonNull(currentDate, "currentDate");
        return new StudyPlanView(
                plan.getId(),
                plan.getGoalName(),
                plan.getPeriod(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getExpectedHours(),
                plan.getProgress(),
                analysisService.analyzeStatus(plan, currentDate));
    }
}
