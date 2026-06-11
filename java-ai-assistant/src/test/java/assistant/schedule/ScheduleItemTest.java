package assistant.schedule;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ScheduleItemTest {
    private static final EntityId ID = new EntityId(1);
    private static final LocalDateTime START = LocalDateTime.of(2026, 6, 11, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 6, 11, 10, 0);
    private static final DateTimeRange TIME_RANGE = new DateTimeRange(START, END);

    @Test
    void constructorStoresProvidedFields() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertScheduleState(schedule, ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");
    }

    @Test
    void createFactoryCreatesEquivalentScheduleItem() {
        ScheduleItem schedule = ScheduleItem.create(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertScheduleState(schedule, ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");
    }

    @Test
    void constructorNormalizesNameLocationAndNote() {
        ScheduleItem schedule = new ScheduleItem(
                ID, "\u2003Team meeting\t", TIME_RANGE, " \tRoom 301\u2003", "\u2003Discuss plan\t");

        assertAll(
                () -> assertEquals("Team meeting", schedule.getName()),
                () -> assertEquals("Room 301", schedule.getLocation()),
                () -> assertEquals("Discuss plan", schedule.getNote()));
    }

    @Test
    void constructorConvertsNullLocationAndNoteToEmptyString() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, null, null);

        assertEquals("", schedule.getLocation());
        assertEquals("", schedule.getNote());
    }

    @Test
    void constructorAllowsBlankLocationAndNoteAsEmptyString() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, " \t\u2003\n", "\u2003 \t\n");

        assertEquals("", schedule.getLocation());
        assertEquals("", schedule.getNote());
    }

    @Test
    void keepsInternalWhitespaceInTextFields() {
        ScheduleItem schedule = new ScheduleItem(
                ID, "Team  planning\tmeeting", TIME_RANGE, "Room  301", "Keep  inner\nspacing");

        assertEquals("Team  planning\tmeeting", schedule.getName());
        assertEquals("Room  301", schedule.getLocation());
        assertEquals("Keep  inner\nspacing", schedule.getNote());
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleItem(null, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan"));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleItem(ID, null, TIME_RANGE, "Room 301", "Discuss plan"));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleItem(ID, "Team meeting", null, "Room 301", "Discuss plan"));
    }

    @Test
    void rejectsBlankName() {
        for (String blankName : new String[] {"", " \t\n", "\u2003"}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ScheduleItem(ID, blankName, TIME_RANGE, "Room 301", "Discuss plan"));
        }
    }

    @Test
    void exposesStartAndEndDateTimesFromRange() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(START, schedule.getStartDateTime());
        assertEquals(END, schedule.getEndDateTime());
    }

    @Test
    void updateDetailsChangesEditableFieldsOnly() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");
        DateTimeRange newRange = range(2026, 6, 11, 14, 0, 2026, 6, 11, 15, 0);

        schedule.updateDetails("Code review", newRange, "Room 302", "Review changes");

        assertScheduleState(schedule, ID, "Code review", newRange, "Room 302", "Review changes");
    }

    @Test
    void updateDetailsMakesStatusAndDateCoverageUseNewTimeRange() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");
        DateTimeRange newRange = range(2026, 6, 12, 14, 0, 2026, 6, 12, 15, 30);

        schedule.updateDetails("Code review", newRange, "Room 302", "Review changes");

        assertAll(
                () -> assertEquals(
                        ScheduleStatus.UPCOMING, schedule.statusAt(LocalDateTime.of(2026, 6, 12, 13, 59))),
                () -> assertEquals(ScheduleStatus.ONGOING, schedule.statusAt(newRange.startDateTime())),
                () -> assertEquals(ScheduleStatus.EXPIRED, schedule.statusAt(newRange.endDateTime())),
                () -> assertEquals(true, schedule.coversDate(LocalDate.of(2026, 6, 12))),
                () -> assertEquals(false, schedule.coversDate(LocalDate.of(2026, 6, 11))));
    }

    @Test
    void updateDetailsNormalizesNewTextFields() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        schedule.updateDetails("\u2003Code review\t", TIME_RANGE, "\tRoom 302\u2003", "\u2003Review changes ");

        assertAll(
                () -> assertEquals("Code review", schedule.getName()),
                () -> assertEquals("Room 302", schedule.getLocation()),
                () -> assertEquals("Review changes", schedule.getNote()));
    }

    @Test
    void updateDetailsConvertsNullOptionalFieldsToEmptyString() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        schedule.updateDetails("Code review", TIME_RANGE, null, null);

        assertEquals("", schedule.getLocation());
        assertEquals("", schedule.getNote());
    }

    @Test
    void updateDetailsRejectsInvalidNameAndKeepsFieldsUnchanged() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertThrows(
                NullPointerException.class,
                () -> schedule.updateDetails(null, range(2026, 6, 11, 14, 0, 2026, 6, 11, 15, 0), "Room 302", "New"));
        assertScheduleState(schedule, ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertThrows(
                IllegalArgumentException.class,
                () -> schedule.updateDetails(" \t\n", range(2026, 6, 11, 14, 0, 2026, 6, 11, 15, 0), "Room 302", "New"));
        assertScheduleState(schedule, ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");
    }

    @Test
    void updateDetailsRejectsNullTimeRangeAndKeepsFieldsUnchanged() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertThrows(NullPointerException.class, () -> schedule.updateDetails("Code review", null, "Room 302", "New"));

        assertScheduleState(schedule, ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");
    }

    @Test
    void statusAtReturnsUpcomingBeforeStart() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(ScheduleStatus.UPCOMING, schedule.statusAt(LocalDateTime.of(2026, 6, 11, 8, 59)));
    }

    @Test
    void statusAtReturnsOngoingAtStartBoundary() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(ScheduleStatus.ONGOING, schedule.statusAt(START));
    }

    @Test
    void statusAtReturnsOngoingInsideRange() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(ScheduleStatus.ONGOING, schedule.statusAt(LocalDateTime.of(2026, 6, 11, 9, 30)));
    }

    @Test
    void statusAtReturnsExpiredAtEndBoundary() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(ScheduleStatus.EXPIRED, schedule.statusAt(END));
    }

    @Test
    void statusAtReturnsExpiredAfterEnd() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(ScheduleStatus.EXPIRED, schedule.statusAt(LocalDateTime.of(2026, 6, 11, 10, 1)));
    }

    @Test
    void statusAtRejectsNullCurrentDateTime() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertThrows(NullPointerException.class, () -> schedule.statusAt(null));
    }

    @Test
    void coversDateWhenScheduleStartsOnDate() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(true, schedule.coversDate(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void coversDateWhenScheduleSpansAcrossDate() {
        ScheduleItem schedule =
                new ScheduleItem(ID, "Hackathon", range(2026, 6, 10, 22, 0, 2026, 6, 12, 1, 0), "Lab", "Build");

        assertEquals(true, schedule.coversDate(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void coversDateWhenCrossDateScheduleStartsOnPreviousDate() {
        ScheduleItem schedule =
                new ScheduleItem(ID, "Hackathon", range(2026, 6, 10, 22, 0, 2026, 6, 12, 1, 0), "Lab", "Build");

        assertEquals(true, schedule.coversDate(LocalDate.of(2026, 6, 10)));
    }

    @Test
    void coversDateWhenCrossDateScheduleEndsAfterStartOfDate() {
        ScheduleItem schedule =
                new ScheduleItem(ID, "Hackathon", range(2026, 6, 10, 22, 0, 2026, 6, 12, 1, 0), "Lab", "Build");

        assertEquals(true, schedule.coversDate(LocalDate.of(2026, 6, 12)));
    }

    @Test
    void coversDateExcludesExclusiveEndDateBoundary() {
        ScheduleItem schedule =
                new ScheduleItem(ID, "Night work", range(2026, 6, 10, 22, 0, 2026, 6, 11, 0, 0), "Lab", "Build");

        assertEquals(false, schedule.coversDate(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void coversDateRejectsNullDate() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertThrows(NullPointerException.class, () -> schedule.coversDate(null));
    }

    private static DateTimeRange range(
            int startYear,
            int startMonth,
            int startDay,
            int startHour,
            int startMinute,
            int endYear,
            int endMonth,
            int endDay,
            int endHour,
            int endMinute) {
        return new DateTimeRange(
                LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute),
                LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute));
    }

    private static void assertScheduleState(
            ScheduleItem schedule,
            EntityId id,
            String name,
            DateTimeRange timeRange,
            String location,
            String note) {
        assertAll(
                () -> assertEquals(id, schedule.getId()),
                () -> assertEquals(name, schedule.getName()),
                () -> assertEquals(timeRange, schedule.getTimeRange()),
                () -> assertEquals(timeRange.startDateTime(), schedule.getStartDateTime()),
                () -> assertEquals(timeRange.endDateTime(), schedule.getEndDateTime()),
                () -> assertEquals(location, schedule.getLocation()),
                () -> assertEquals(note, schedule.getNote()));
    }
}
