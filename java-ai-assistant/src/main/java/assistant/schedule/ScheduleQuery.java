package assistant.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record ScheduleQuery(LocalDate date, ScheduleStatus status) {
    public static ScheduleQuery all() {
        return new ScheduleQuery(null, null);
    }

    public static ScheduleQuery byDate(LocalDate date) {
        return new ScheduleQuery(Objects.requireNonNull(date, "date"), null);
    }

    public static ScheduleQuery byStatus(ScheduleStatus status) {
        return new ScheduleQuery(null, Objects.requireNonNull(status, "status"));
    }

    public static ScheduleQuery of(LocalDate date, ScheduleStatus status) {
        return new ScheduleQuery(date, status);
    }

    public boolean hasDateFilter() {
        return date != null;
    }

    public boolean hasStatusFilter() {
        return status != null;
    }

    public boolean matches(ScheduleItem item, LocalDateTime currentDateTime) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(currentDateTime, "currentDateTime");
        return (!hasDateFilter() || item.coversDate(date))
                && (!hasStatusFilter() || item.statusAt(currentDateTime) == status);
    }
}
