package assistant.schedule;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class ScheduleItem {
    private final EntityId id;
    private String name;
    private DateTimeRange timeRange;
    private String location;
    private String note;

    public ScheduleItem(EntityId id, String name, DateTimeRange timeRange, String location, String note) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = normalizeName(name);
        this.timeRange = Objects.requireNonNull(timeRange, "timeRange");
        this.location = normalizeOptionalText(location);
        this.note = normalizeOptionalText(note);
    }

    public static ScheduleItem create(EntityId id, String name, DateTimeRange timeRange, String location, String note) {
        return new ScheduleItem(id, name, timeRange, location, note);
    }

    public EntityId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DateTimeRange getTimeRange() {
        return timeRange;
    }

    public LocalDateTime getStartDateTime() {
        return timeRange.startDateTime();
    }

    public LocalDateTime getEndDateTime() {
        return timeRange.endDateTime();
    }

    public String getLocation() {
        return location;
    }

    public String getNote() {
        return note;
    }

    public void updateDetails(String name, DateTimeRange timeRange, String location, String note) {
        String normalizedName = normalizeName(name);
        DateTimeRange requiredTimeRange = Objects.requireNonNull(timeRange, "timeRange");
        String normalizedLocation = normalizeOptionalText(location);
        String normalizedNote = normalizeOptionalText(note);

        this.name = normalizedName;
        this.timeRange = requiredTimeRange;
        this.location = normalizedLocation;
        this.note = normalizedNote;
    }

    public ScheduleStatus statusAt(LocalDateTime currentDateTime) {
        Objects.requireNonNull(currentDateTime, "currentDateTime");
        if (currentDateTime.isBefore(timeRange.startDateTime())) {
            return ScheduleStatus.UPCOMING;
        }
        if (currentDateTime.isBefore(timeRange.endDateTime())) {
            return ScheduleStatus.ONGOING;
        }
        return ScheduleStatus.EXPIRED;
    }

    public boolean coversDate(LocalDate date) {
        return timeRange.coversDate(date);
    }

    private static String normalizeName(String name) {
        String normalizedName = Objects.requireNonNull(name, "name").strip();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return normalizedName;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return "";
        }
        return value.strip();
    }
}
