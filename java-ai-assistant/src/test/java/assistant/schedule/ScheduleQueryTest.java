package assistant.schedule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ScheduleQueryTest {
    private static final LocalDate JUNE_10 = LocalDate.of(2026, 6, 10);
    private static final LocalDate JUNE_11 = LocalDate.of(2026, 6, 11);
    private static final LocalDateTime CURRENT = LocalDateTime.of(2026, 6, 11, 9, 30);

    @Test
    void allQueryMatchesEverySchedule() {
        ScheduleQuery query = ScheduleQuery.all();

        assertTrue(query.matches(schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), CURRENT));
        assertTrue(query.matches(schedule(2, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0), CURRENT));
    }

    @Test
    void dateQueryMatchesSchedulesCoveringNaturalDate() {
        ScheduleQuery query = ScheduleQuery.byDate(JUNE_11);

        assertTrue(query.matches(schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), CURRENT));
        assertFalse(query.matches(schedule(2, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0), CURRENT));
    }

    @Test
    void dateQueryMatchesCrossDateScheduleCoveringMiddleDate() {
        ScheduleQuery query = ScheduleQuery.byDate(JUNE_11);
        ScheduleItem crossDate = schedule(1, 2026, 6, 10, 22, 0, 2026, 6, 12, 1, 0);

        assertTrue(query.matches(crossDate, CURRENT));
    }

    @Test
    void dateQueryExcludesExclusiveMidnightEndBoundary() {
        ScheduleQuery query = ScheduleQuery.byDate(JUNE_11);
        ScheduleItem endsAtMidnight = schedule(1, 2026, 6, 10, 22, 0, 2026, 6, 11, 0, 0);

        assertFalse(query.matches(endsAtMidnight, CURRENT));
    }

    @Test
    void statusQueryMatchesStatusAtProvidedCurrentDateTime() {
        ScheduleItem schedule = schedule(1, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        assertTrue(ScheduleQuery.byStatus(ScheduleStatus.UPCOMING)
                .matches(schedule, LocalDateTime.of(2026, 6, 11, 9, 59)));
        assertTrue(ScheduleQuery.byStatus(ScheduleStatus.ONGOING)
                .matches(schedule, LocalDateTime.of(2026, 6, 11, 10, 30)));
        assertTrue(ScheduleQuery.byStatus(ScheduleStatus.EXPIRED)
                .matches(schedule, LocalDateTime.of(2026, 6, 11, 11, 0)));
        assertFalse(ScheduleQuery.byStatus(ScheduleStatus.ONGOING)
                .matches(schedule, LocalDateTime.of(2026, 6, 11, 9, 59)));
    }

    @Test
    void combinedQueryRequiresDateAndStatusToMatch() {
        ScheduleQuery query = ScheduleQuery.of(JUNE_11, ScheduleStatus.ONGOING);

        assertTrue(query.matches(schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), CURRENT));
        assertFalse(query.matches(schedule(2, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0), CURRENT));
        assertFalse(query.matches(schedule(3, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), CURRENT));
    }

    @Test
    void ofAllowsNullComponentsAsWildcards() {
        ScheduleQuery statusOnly = ScheduleQuery.of(null, ScheduleStatus.ONGOING);
        ScheduleQuery dateOnly = ScheduleQuery.of(JUNE_11, null);

        assertTrue(statusOnly.matches(schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), CURRENT));
        assertFalse(statusOnly.matches(schedule(2, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), CURRENT));
        assertTrue(dateOnly.matches(schedule(3, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), CURRENT));
        assertFalse(dateOnly.matches(schedule(4, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0), CURRENT));
    }

    @Test
    void exposesFilterPresenceFlags() {
        ScheduleQuery all = ScheduleQuery.all();
        ScheduleQuery byDate = ScheduleQuery.byDate(JUNE_11);
        ScheduleQuery byStatus = ScheduleQuery.byStatus(ScheduleStatus.UPCOMING);
        ScheduleQuery combined = ScheduleQuery.of(JUNE_10, ScheduleStatus.EXPIRED);

        assertFalse(all.hasDateFilter());
        assertFalse(all.hasStatusFilter());
        assertTrue(byDate.hasDateFilter());
        assertFalse(byDate.hasStatusFilter());
        assertFalse(byStatus.hasDateFilter());
        assertTrue(byStatus.hasStatusFilter());
        assertTrue(combined.hasDateFilter());
        assertTrue(combined.hasStatusFilter());
    }

    @Test
    void singleCriterionFactoriesRejectNullCriterion() {
        assertThrows(NullPointerException.class, () -> ScheduleQuery.byDate(null));
        assertThrows(NullPointerException.class, () -> ScheduleQuery.byStatus(null));
    }

    @Test
    void matchesRejectsNullArguments() {
        ScheduleItem schedule = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);

        assertThrows(NullPointerException.class, () -> ScheduleQuery.all().matches(null, CURRENT));
        assertThrows(NullPointerException.class, () -> ScheduleQuery.all().matches(schedule, null));
    }

    private static ScheduleItem schedule(
            long id,
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
        return new ScheduleItem(
                new EntityId(id),
                "Schedule " + id,
                new DateTimeRange(
                        LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute),
                        LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute)),
                "Room " + id,
                "Note " + id);
    }
}
