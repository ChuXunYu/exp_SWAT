package assistant.study;

import assistant.common.DateRange;
import java.time.LocalDate;
import java.util.Objects;

public record StudyPlanQuery(StudyPlanStatus status, DateRange period) {
    public static StudyPlanQuery all() {
        return new StudyPlanQuery(null, null);
    }

    public static StudyPlanQuery byStatus(StudyPlanStatus status) {
        return new StudyPlanQuery(Objects.requireNonNull(status, "status"), null);
    }

    public static StudyPlanQuery byPeriod(DateRange period) {
        return new StudyPlanQuery(null, Objects.requireNonNull(period, "period"));
    }

    public static StudyPlanQuery of(StudyPlanStatus status, DateRange period) {
        return new StudyPlanQuery(status, period);
    }

    public boolean hasStatusFilter() {
        return status != null;
    }

    public boolean hasPeriodFilter() {
        return period != null;
    }

    public boolean matches(StudyPlan plan, StudyPlanAnalysisService analysisService, LocalDate currentDate) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(analysisService, "analysisService");
        Objects.requireNonNull(currentDate, "currentDate");
        return (!hasStatusFilter() || analysisService.analyzeStatus(plan, currentDate) == status)
                && (!hasPeriodFilter() || plan.getPeriod().overlaps(period));
    }
}
