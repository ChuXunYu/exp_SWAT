package assistant.ai;

import assistant.common.Progress;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record StudyPlanDraftContent(
        String goalName,
        LocalDate startDate,
        LocalDate endDate,
        int expectedHours,
        Progress initialProgress,
        List<String> breakdown) {
    public StudyPlanDraftContent {
        goalName = Objects.requireNonNull(goalName, "goalName").strip();
        if (goalName.isEmpty()) {
            throw new IllegalArgumentException("goalName must not be blank");
        }
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        if (expectedHours <= 0) {
            throw new IllegalArgumentException("expectedHours must be positive");
        }
        Objects.requireNonNull(initialProgress, "initialProgress");
        Objects.requireNonNull(breakdown, "breakdown");
        breakdown = breakdown.stream()
                .map(item -> Objects.requireNonNull(item, "breakdownItem").strip())
                .filter(item -> !item.isEmpty())
                .toList();
    }

    public boolean hasBreakdown() {
        return !breakdown.isEmpty();
    }
}
