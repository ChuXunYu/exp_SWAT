package assistant.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ScheduleConflictPolicyTest {
    private final ScheduleConflictPolicy policy = new ScheduleConflictPolicy();

    @Test
    void conflictsWhenRangesShareInteriorDateTimes() {
        ScheduleItem left = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem right = schedule(2, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);

        assertTrue(policy.conflicts(left, right));
    }

    @Test
    void conflictsWhenCandidateContainsExistingRange() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 8, 0, 2026, 6, 11, 12, 0);
        ScheduleItem existing = schedule(2, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);

        assertTrue(policy.conflicts(candidate, existing));
    }

    @Test
    void conflictsWhenExistingContainsCandidateRange() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem existing = schedule(2, 2026, 6, 11, 8, 0, 2026, 6, 11, 12, 0);

        assertTrue(policy.conflicts(candidate, existing));
    }

    @Test
    void conflictsWhenCrossDateRangesOverlap() {
        ScheduleItem crossDate = schedule(1, 2026, 6, 10, 23, 0, 2026, 6, 11, 2, 0);
        ScheduleItem singleDate = schedule(2, 2026, 6, 11, 1, 0, 2026, 6, 11, 3, 0);

        assertTrue(policy.conflicts(crossDate, singleDate));
    }

    @Test
    void conflictsWhenSchedulesHaveSameEntityIdAndOverlappingRanges() {
        ScheduleItem left = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem right = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);

        assertTrue(policy.conflicts(left, right));
    }

    @Test
    void conflictsIsSymmetric() {
        ScheduleItem left = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem right = schedule(2, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);

        assertEquals(policy.conflicts(left, right), policy.conflicts(right, left));
    }

    @Test
    void doesNotConflictWhenRangesTouchAtBoundary() {
        ScheduleItem left = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem right = schedule(2, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        assertFalse(policy.conflicts(left, right));
    }

    @Test
    void doesNotConflictWhenRangesAreSeparated() {
        ScheduleItem left = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem right = schedule(2, 2026, 6, 11, 10, 30, 2026, 6, 11, 11, 0);

        assertFalse(policy.conflicts(left, right));
    }

    @Test
    void doesNotConflictWhenSchedulesAreOnDifferentDatesWithoutOverlap() {
        ScheduleItem left = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem right = schedule(2, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0);

        assertFalse(policy.conflicts(left, right));
    }

    @Test
    void conflictsRejectsNullArguments() {
        ScheduleItem schedule = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);

        assertThrows(NullPointerException.class, () -> policy.conflicts(null, schedule));
        assertThrows(NullPointerException.class, () -> policy.conflicts(schedule, null));
    }

    @Test
    void hasConflictReturnsTrueWhenAnyExistingScheduleConflicts() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);
        ScheduleItem separated = schedule(2, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0);
        ScheduleItem conflicting = schedule(3, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        assertTrue(policy.hasConflict(candidate, List.of(separated, conflicting)));
    }

    @Test
    void collectionMethodsTreatSameEntityIdAsConflictWhenRangesOverlap() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);
        ScheduleItem separated = schedule(2, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0);
        ScheduleItem sameIdConflict = schedule(1, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        assertTrue(policy.hasConflict(candidate, List.of(separated, sameIdConflict)));
        assertEquals(
                Optional.of(sameIdConflict),
                policy.findFirstConflict(candidate, List.of(separated, sameIdConflict)));
    }

    @Test
    void hasConflictReturnsFalseForEmptyOrNonConflictingCollection() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);
        ScheduleItem before = schedule(2, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 30);
        ScheduleItem after = schedule(3, 2026, 6, 11, 10, 30, 2026, 6, 11, 11, 0);

        assertFalse(policy.hasConflict(candidate, Collections.emptyList()));
        assertFalse(policy.hasConflict(candidate, List.of(before, after)));
    }

    @Test
    void findFirstConflictReturnsFirstConflictByIterationOrder() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);
        ScheduleItem separated = schedule(2, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0);
        ScheduleItem firstConflict = schedule(3, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem secondConflict = schedule(4, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        Optional<ScheduleItem> result =
                policy.findFirstConflict(candidate, List.of(separated, firstConflict, secondConflict));

        assertEquals(Optional.of(firstConflict), result);
    }

    @Test
    void findFirstConflictReturnsEmptyWhenNoScheduleConflicts() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);
        ScheduleItem before = schedule(2, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 30);
        ScheduleItem after = schedule(3, 2026, 6, 11, 10, 30, 2026, 6, 11, 11, 0);

        assertEquals(Optional.empty(), policy.findFirstConflict(candidate, List.of(before, after)));
    }

    @Test
    void collectionMethodsRejectNullCandidateOrCollection() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);
        List<ScheduleItem> existingSchedules = List.of(schedule(2, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0));

        assertThrows(NullPointerException.class, () -> policy.hasConflict(null, existingSchedules));
        assertThrows(NullPointerException.class, () -> policy.hasConflict(candidate, null));
        assertThrows(NullPointerException.class, () -> policy.findFirstConflict(null, existingSchedules));
        assertThrows(NullPointerException.class, () -> policy.findFirstConflict(candidate, null));
    }

    @Test
    void collectionMethodsRejectNullElements() {
        ScheduleItem candidate = schedule(1, 2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30);
        List<ScheduleItem> existingSchedules =
                java.util.Arrays.asList(schedule(2, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0), null);
        List<ScheduleItem> existingSchedulesWithEarlierConflict =
                java.util.Arrays.asList(schedule(3, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), null);

        assertThrows(NullPointerException.class, () -> policy.hasConflict(candidate, existingSchedules));
        assertThrows(NullPointerException.class, () -> policy.findFirstConflict(candidate, existingSchedules));
        assertThrows(NullPointerException.class, () -> policy.hasConflict(candidate, existingSchedulesWithEarlierConflict));
        assertThrows(
                NullPointerException.class,
                () -> policy.findFirstConflict(candidate, existingSchedulesWithEarlierConflict));
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
        DateTimeRange timeRange = new DateTimeRange(
                LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute),
                LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute));
        return new ScheduleItem(new EntityId(id), "Schedule " + id, timeRange, "Room " + id, "Note " + id);
    }
}
