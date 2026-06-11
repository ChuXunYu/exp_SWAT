package assistant.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record DateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    public DateTimeRange {
        Objects.requireNonNull(startDateTime, "startDateTime");
        Objects.requireNonNull(endDateTime, "endDateTime");
        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException("endDateTime must be after startDateTime");
        }
    }

    public boolean contains(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime");
        return !dateTime.isBefore(startDateTime) && dateTime.isBefore(endDateTime);
    }

    public boolean overlaps(DateTimeRange other) {
        Objects.requireNonNull(other, "other");
        return startDateTime.isBefore(other.endDateTime()) && other.startDateTime().isBefore(endDateTime);
    }

    public boolean coversDate(LocalDate date) {
        Objects.requireNonNull(date, "date");
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime nextDateStart = date.plusDays(1).atStartOfDay();
        return startDateTime.isBefore(nextDateStart) && dateStart.isBefore(endDateTime);
    }
}
