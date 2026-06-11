package assistant.schedule;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class ScheduleConflictPolicy {
    public ScheduleConflictPolicy() {}

    public boolean conflicts(ScheduleItem left, ScheduleItem right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        return left.getTimeRange().overlaps(right.getTimeRange());
    }

    public boolean hasConflict(ScheduleItem candidate, Collection<ScheduleItem> existingSchedules) {
        return findFirstConflict(candidate, existingSchedules).isPresent();
    }

    public Optional<ScheduleItem> findFirstConflict(
            ScheduleItem candidate, Collection<ScheduleItem> existingSchedules) {
        Objects.requireNonNull(candidate, "candidate");
        Objects.requireNonNull(existingSchedules, "existingSchedules");
        for (ScheduleItem existingSchedule : existingSchedules) {
            Objects.requireNonNull(existingSchedule, "existingSchedule");
        }
        for (ScheduleItem existingSchedule : existingSchedules) {
            if (conflicts(candidate, existingSchedule)) {
                return Optional.of(existingSchedule);
            }
        }
        return Optional.empty();
    }
}
