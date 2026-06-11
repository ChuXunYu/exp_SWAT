package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangeTest {
    @Test
    void constructsClosedDateRangeAndExposesBounds() {
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        DateRange range = new DateRange(startDate, endDate);

        assertEquals(startDate, range.startDate());
        assertEquals(endDate, range.endDate());
    }

    @Test
    void allowsSingleDayRange() {
        LocalDate date = LocalDate.of(2026, 6, 11);

        DateRange range = new DateRange(date, date);

        assertTrue(range.contains(date));
    }

    @Test
    void rejectsNullStartDate() {
        assertThrows(NullPointerException.class, () -> new DateRange(null, LocalDate.of(2026, 6, 30)));
    }

    @Test
    void rejectsNullEndDate() {
        assertThrows(NullPointerException.class, () -> new DateRange(LocalDate.of(2026, 6, 1), null));
    }

    @Test
    void rejectsStartDateAfterEndDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 6, 30)));
    }

    @Test
    void containsIncludesStartAndEndDates() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertTrue(range.contains(LocalDate.of(2026, 6, 1)));
        assertTrue(range.contains(LocalDate.of(2026, 6, 30)));
    }

    @Test
    void containsIncludesInteriorDate() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertTrue(range.contains(LocalDate.of(2026, 6, 15)));
    }

    @Test
    void containsExcludesDatesOutsideRange() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertFalse(range.contains(LocalDate.of(2026, 5, 31)));
        assertFalse(range.contains(LocalDate.of(2026, 7, 1)));
    }

    @Test
    void containsRejectsNullDate() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThrows(NullPointerException.class, () -> range.contains(null));
    }

    @Test
    void overlapsWhenRangesShareInteriorDates() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        DateRange other = new DateRange(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 15));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenRangesTouchAtBoundaryDate() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        DateRange other = new DateRange(LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 15));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenOtherRangeTouchesStartBoundaryDate() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 30));
        DateRange other = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenOtherStartsBeforeAndEndsInsideRange() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 30));
        DateRange other = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 15));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenOtherContainsRange() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 20));
        DateRange other = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertTrue(range.overlaps(other));
    }

    @Test
    void overlapsWhenRangeContainsOther() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        DateRange other = new DateRange(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 20));

        assertTrue(range.overlaps(other));
    }

    @Test
    void doesNotOverlapWhenRangesAreSeparated() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        DateRange other = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15));

        assertFalse(range.overlaps(other));
    }

    @Test
    void doesNotOverlapWhenOtherRangeIsBeforeRange() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        DateRange other = new DateRange(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        assertFalse(range.overlaps(other));
    }

    @Test
    void overlapsRejectsNullOtherRange() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThrows(NullPointerException.class, () -> range.overlaps(null));
    }

    @Test
    void equalityAndHashCodeUseBothBounds() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        DateRange same = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        DateRange differentStart = new DateRange(LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 30));
        DateRange differentEnd = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 29));

        assertEquals(same, range);
        assertEquals(same.hashCode(), range.hashCode());
        assertNotEquals(differentStart, range);
        assertNotEquals(differentEnd, range);
    }

    @Test
    void toStringUsesRecordComponentNamesAndValues() {
        DateRange range = new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertEquals("DateRange[startDate=2026-06-01, endDate=2026-06-30]", range.toString());
    }
}
