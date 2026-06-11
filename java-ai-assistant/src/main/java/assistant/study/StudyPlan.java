package assistant.study;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import java.time.LocalDate;
import java.util.Objects;

public class StudyPlan {
    private final EntityId id;
    private String goalName;
    private DateRange period;
    private int expectedHours;
    private Progress progress;

    public StudyPlan(EntityId id, String goalName, DateRange period, int expectedHours, Progress progress) {
        this.id = Objects.requireNonNull(id, "id");
        this.goalName = normalizeGoalName(goalName);
        this.period = Objects.requireNonNull(period, "period");
        this.expectedHours = requirePositiveExpectedHours(expectedHours);
        this.progress = Objects.requireNonNull(progress, "progress");
    }

    public static StudyPlan create(EntityId id, String goalName, DateRange period, int expectedHours) {
        return new StudyPlan(id, goalName, period, expectedHours, Progress.zero());
    }

    public static StudyPlan create(
            EntityId id, String goalName, DateRange period, int expectedHours, Progress progress) {
        return new StudyPlan(id, goalName, period, expectedHours, progress);
    }

    public EntityId getId() {
        return id;
    }

    public String getGoalName() {
        return goalName;
    }

    public DateRange getPeriod() {
        return period;
    }

    public LocalDate getStartDate() {
        return period.startDate();
    }

    public LocalDate getEndDate() {
        return period.endDate();
    }

    public int getExpectedHours() {
        return expectedHours;
    }

    public Progress getProgress() {
        return progress;
    }

    public boolean isCompleted() {
        return progress.isComplete();
    }

    public void updateDetails(String goalName, DateRange period, int expectedHours) {
        String normalizedGoalName = normalizeGoalName(goalName);
        DateRange requiredPeriod = Objects.requireNonNull(period, "period");
        int requiredExpectedHours = requirePositiveExpectedHours(expectedHours);

        this.goalName = normalizedGoalName;
        this.period = requiredPeriod;
        this.expectedHours = requiredExpectedHours;
    }

    public void updateProgress(Progress progress) {
        this.progress = Objects.requireNonNull(progress, "progress");
    }

    private static String normalizeGoalName(String goalName) {
        String normalizedGoalName = Objects.requireNonNull(goalName, "goalName").strip();
        if (normalizedGoalName.isEmpty()) {
            throw new IllegalArgumentException("goalName must not be blank");
        }
        return normalizedGoalName;
    }

    private static int requirePositiveExpectedHours(int expectedHours) {
        if (expectedHours <= 0) {
            throw new IllegalArgumentException("expectedHours must be positive");
        }
        return expectedHours;
    }
}
