package assistant.common;

import java.time.LocalDate;
import java.util.Objects;

public record DateRange(LocalDate startDate, LocalDate endDate) {
    public DateRange {
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }

    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "other");
        return !startDate.isAfter(other.endDate()) && !endDate.isBefore(other.startDate());
    }
}
