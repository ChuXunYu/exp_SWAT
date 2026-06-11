package assistant.schedule;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ScheduleViewTest {
    private static final EntityId ID = new EntityId(1);
    private static final LocalDateTime START = LocalDateTime.of(2026, 6, 11, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 6, 11, 10, 0);
    private static final DateTimeRange TIME_RANGE = new DateTimeRange(START, END);

    @Test
    void constructorStoresProvidedSnapshotFields() {
        ScheduleView view = new ScheduleView(
                ID, "Team meeting", TIME_RANGE, START, END, "Room 301", "Discuss plan", ScheduleStatus.ONGOING);

        assertAll(
                () -> assertEquals(ID, view.id()),
                () -> assertEquals("Team meeting", view.name()),
                () -> assertEquals(TIME_RANGE, view.timeRange()),
                () -> assertEquals(START, view.startDateTime()),
                () -> assertEquals(END, view.endDateTime()),
                () -> assertEquals("Room 301", view.location()),
                () -> assertEquals("Discuss plan", view.note()),
                () -> assertEquals(ScheduleStatus.ONGOING, view.status()));
    }

    @Test
    void constructorPreservesProvidedText() {
        ScheduleView view = new ScheduleView(
                ID,
                "  Team meeting  ",
                TIME_RANGE,
                START,
                END,
                "\tRoom 301\n",
                "  Discuss plan\t",
                ScheduleStatus.UPCOMING);

        assertAll(
                () -> assertEquals("  Team meeting  ", view.name()),
                () -> assertEquals("\tRoom 301\n", view.location()),
                () -> assertEquals("  Discuss plan\t", view.note()));
    }

    @Test
    void constructorRejectsNullRequiredFields() {
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(null, "Name", TIME_RANGE, START, END, "Location", "Note", ScheduleStatus.UPCOMING));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(ID, null, TIME_RANGE, START, END, "Location", "Note", ScheduleStatus.UPCOMING));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(ID, "Name", null, START, END, "Location", "Note", ScheduleStatus.UPCOMING));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(ID, "Name", TIME_RANGE, null, END, "Location", "Note", ScheduleStatus.UPCOMING));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(ID, "Name", TIME_RANGE, START, null, "Location", "Note", ScheduleStatus.UPCOMING));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(ID, "Name", TIME_RANGE, START, END, null, "Note", ScheduleStatus.UPCOMING));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(ID, "Name", TIME_RANGE, START, END, "Location", null, ScheduleStatus.UPCOMING));
        assertThrows(
                NullPointerException.class,
                () -> new ScheduleView(ID, "Name", TIME_RANGE, START, END, "Location", "Note", null));
    }

    @Test
    void constructorRejectsBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ScheduleView(
                        ID, " \t\n", TIME_RANGE, START, END, "Location", "Note", ScheduleStatus.UPCOMING));
    }

    @Test
    void constructorRejectsInconsistentTimeEndpoints() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ScheduleView(
                        ID,
                        "Name",
                        TIME_RANGE,
                        START.minusMinutes(1),
                        END,
                        "Location",
                        "Note",
                        ScheduleStatus.UPCOMING));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ScheduleView(
                        ID,
                        "Name",
                        TIME_RANGE,
                        START,
                        END.plusMinutes(1),
                        "Location",
                        "Note",
                        ScheduleStatus.UPCOMING));
    }

    @Test
    void fromCopiesAllScheduleFieldsAndComputesStatus() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        ScheduleView view = ScheduleView.from(schedule, LocalDateTime.of(2026, 6, 11, 9, 30));

        assertAll(
                () -> assertEquals(schedule.getId(), view.id()),
                () -> assertEquals(schedule.getName(), view.name()),
                () -> assertEquals(schedule.getTimeRange(), view.timeRange()),
                () -> assertEquals(schedule.getStartDateTime(), view.startDateTime()),
                () -> assertEquals(schedule.getEndDateTime(), view.endDateTime()),
                () -> assertEquals(schedule.getLocation(), view.location()),
                () -> assertEquals(schedule.getNote(), view.note()),
                () -> assertEquals(ScheduleStatus.ONGOING, view.status()));
    }

    @Test
    void fromComputesUpcomingOngoingAndExpiredSnapshots() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertEquals(ScheduleStatus.UPCOMING, ScheduleView.from(schedule, START.minusMinutes(1)).status());
        assertEquals(ScheduleStatus.ONGOING, ScheduleView.from(schedule, START).status());
        assertEquals(ScheduleStatus.EXPIRED, ScheduleView.from(schedule, END).status());
    }

    @Test
    void semanticFlagsReflectSnapshotStatus() {
        ScheduleView upcoming = viewWithStatus(ScheduleStatus.UPCOMING);
        ScheduleView ongoing = viewWithStatus(ScheduleStatus.ONGOING);
        ScheduleView expired = viewWithStatus(ScheduleStatus.EXPIRED);

        assertTrue(upcoming.isUpcoming());
        assertFalse(upcoming.isOngoing());
        assertFalse(upcoming.isExpired());
        assertTrue(ongoing.isOngoing());
        assertFalse(ongoing.isUpcoming());
        assertFalse(ongoing.isExpired());
        assertTrue(expired.isExpired());
        assertFalse(expired.isUpcoming());
        assertFalse(expired.isOngoing());
    }

    @Test
    void fromRejectsNullArguments() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");

        assertThrows(NullPointerException.class, () -> ScheduleView.from(null, START));
        assertThrows(NullPointerException.class, () -> ScheduleView.from(schedule, null));
    }

    @Test
    void fromCreatesSnapshotIndependentFromLaterScheduleMutation() {
        ScheduleItem schedule = new ScheduleItem(ID, "Team meeting", TIME_RANGE, "Room 301", "Discuss plan");
        ScheduleView view = ScheduleView.from(schedule, LocalDateTime.of(2026, 6, 11, 9, 30));
        DateTimeRange newRange = new DateTimeRange(
                LocalDateTime.of(2026, 6, 12, 14, 0),
                LocalDateTime.of(2026, 6, 12, 15, 0));

        schedule.updateDetails("Code review", newRange, "Room 302", "Review changes");

        assertAll(
                () -> assertEquals("Team meeting", view.name()),
                () -> assertEquals(TIME_RANGE, view.timeRange()),
                () -> assertEquals(START, view.startDateTime()),
                () -> assertEquals(END, view.endDateTime()),
                () -> assertEquals("Room 301", view.location()),
                () -> assertEquals("Discuss plan", view.note()),
                () -> assertEquals(ScheduleStatus.ONGOING, view.status()));
    }

    private static ScheduleView viewWithStatus(ScheduleStatus status) {
        return new ScheduleView(ID, "Team meeting", TIME_RANGE, START, END, "Room 301", "Discuss plan", status);
    }
}
