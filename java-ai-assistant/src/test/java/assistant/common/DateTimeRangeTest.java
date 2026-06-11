package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DateTimeRangeTest {
    @Test
    void constructsDateTimeRangeAndExposesBounds() {
        LocalDateTime startDateTime = LocalDateTime.of(2026, 6, 11, 9, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2026, 6, 11, 10, 0);

        DateTimeRange range = new DateTimeRange(startDateTime, endDateTime);

        assertEquals(startDateTime, range.startDateTime());
        assertEquals(endDateTime, range.endDateTime());
    }

    @Test
    void rejectsNullStartDateTime() {
        assertThrows(
                NullPointerException.class,
                () -> new DateTimeRange(null, LocalDateTime.of(2026, 6, 11, 10, 0)));
    }

    @Test
    void rejectsNullEndDateTime() {
        assertThrows(
                NullPointerException.class,
                () -> new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), null));
    }

    @Test
    void rejectsEndDateTimeEqualToStartDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 11, 9, 0);

        assertThrows(IllegalArgumentException.class, () -> new DateTimeRange(dateTime, dateTime));
    }

    @Test
    void rejectsEndDateTimeBeforeStartDateTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DateTimeRange(
                        LocalDateTime.of(2026, 6, 11, 10, 0), LocalDateTime.of(2026, 6, 11, 9, 0)));
    }

    @Test
    void containsIncludesStartDateTime() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertTrue(range.contains(LocalDateTime.of(2026, 6, 11, 9, 0)));
    }

    @Test
    void containsIncludesInteriorDateTime() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertTrue(range.contains(LocalDateTime.of(2026, 6, 11, 9, 30)));
    }

    @Test
    void containsExcludesEndDateTime() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertFalse(range.contains(LocalDateTime.of(2026, 6, 11, 10, 0)));
    }

    @Test
    void containsExcludesDateTimesOutsideRange() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertFalse(range.contains(LocalDateTime.of(2026, 6, 11, 8, 59)));
        assertFalse(range.contains(LocalDateTime.of(2026, 6, 11, 10, 0)));
        assertFalse(range.contains(LocalDateTime.of(2026, 6, 11, 10, 1)));
    }

    @Test
    void containsRejectsNullDateTime() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertThrows(NullPointerException.class, () -> range.contains(null));
    }

    @Test
    void overlapsWhenRangesShareInteriorDateTimes() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 30), LocalDateTime.of(2026, 6, 11, 10, 30));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenOtherStartsBeforeAndEndsInsideRange() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 8, 30), LocalDateTime.of(2026, 6, 11, 9, 30));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenOtherContainsRange() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 8, 30), LocalDateTime.of(2026, 6, 11, 10, 30));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenRangeContainsOther() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 11, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 30), LocalDateTime.of(2026, 6, 11, 10, 30));

        assertTrue(range.overlaps(other));
    }

    @Test
    void doesNotOverlapWhenRangesTouchAtBoundary() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 10, 0), LocalDateTime.of(2026, 6, 11, 11, 0));

        assertFalse(range.overlaps(other));
    }

    @Test
    void doesNotOverlapWhenOtherRangeTouchesStartBoundary() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 8, 0), LocalDateTime.of(2026, 6, 11, 9, 0));

        assertFalse(range.overlaps(other));
    }

    @Test
    void doesNotOverlapWhenRangesAreSeparated() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 10, 30), LocalDateTime.of(2026, 6, 11, 11, 0));

        assertFalse(range.overlaps(other));
    }

    @Test
    void doesNotOverlapWhenOtherRangeIsBeforeRange() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange other =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 8, 0), LocalDateTime.of(2026, 6, 11, 8, 30));

        assertFalse(range.overlaps(other));
    }

    @Test
    void overlapsRejectsNullOtherRange() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertThrows(NullPointerException.class, () -> range.overlaps(null));
    }

    @Test
    void coversDateWhenRangeStartsOnDate() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertTrue(range.coversDate(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void coversDateWhenRangeSpansAcrossDate() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 10, 22, 0), LocalDateTime.of(2026, 6, 12, 1, 0));

        assertTrue(range.coversDate(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void coversDateExcludesDateAtExclusiveEndBoundary() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 10, 22, 0), LocalDateTime.of(2026, 6, 11, 0, 0));

        assertFalse(range.coversDate(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void coversDateExcludesDateWhenRangeStartsAtNextDateStart() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 12, 0, 0), LocalDateTime.of(2026, 6, 12, 1, 0));

        assertFalse(range.coversDate(LocalDate.of(2026, 6, 11)));
    }

    @Test
    void coversDateExcludesDatesOutsideRange() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertFalse(range.coversDate(LocalDate.of(2026, 6, 10)));
        assertFalse(range.coversDate(LocalDate.of(2026, 6, 12)));
    }

    @Test
    void coversDateRejectsNullDate() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertThrows(NullPointerException.class, () -> range.coversDate(null));
    }

    @Test
    void equalityAndHashCodeUseBothBounds() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange same =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange differentStart =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 1), LocalDateTime.of(2026, 6, 11, 10, 0));
        DateTimeRange differentEnd =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 1));

        assertEquals(same, range);
        assertEquals(same.hashCode(), range.hashCode());
        assertNotEquals(differentStart, range);
        assertNotEquals(differentEnd, range);
    }

    @Test
    void toStringUsesRecordComponentNamesAndValues() {
        DateTimeRange range =
                new DateTimeRange(LocalDateTime.of(2026, 6, 11, 9, 0), LocalDateTime.of(2026, 6, 11, 10, 0));

        assertEquals(
                "DateTimeRange[startDateTime=2026-06-11T09:00, endDateTime=2026-06-11T10:00]",
                range.toString());
    }
}
