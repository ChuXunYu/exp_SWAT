package assistant.schedule;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import java.time.LocalDateTime;
import java.util.Objects;

public record ScheduleView(
        EntityId id,
        String name,
        DateTimeRange timeRange,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String location,
        String note,
        ScheduleStatus status) {
    public ScheduleView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(timeRange, "timeRange");
        Objects.requireNonNull(startDateTime, "startDateTime");
        Objects.requireNonNull(endDateTime, "endDateTime");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(note, "note");
        Objects.requireNonNull(status, "status");
        if (name.strip().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (!startDateTime.equals(timeRange.startDateTime())) {
            throw new IllegalArgumentException("startDateTime must match timeRange");
        }
        if (!endDateTime.equals(timeRange.endDateTime())) {
            throw new IllegalArgumentException("endDateTime must match timeRange");
        }
    }

    public static ScheduleView from(ScheduleItem item, LocalDateTime currentDateTime) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(currentDateTime, "currentDateTime");
        return new ScheduleView(
                item.getId(),
                item.getName(),
                item.getTimeRange(),
                item.getStartDateTime(),
                item.getEndDateTime(),
                item.getLocation(),
                item.getNote(),
                item.statusAt(currentDateTime));
    }

    public boolean isUpcoming() {
        return status.isUpcoming();
    }

    public boolean isOngoing() {
        return status.isOngoing();
    }

    public boolean isExpired() {
        return status.isExpired();
    }
}
