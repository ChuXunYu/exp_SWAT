package assistant.schedule;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScheduleStatusTest {
    @Test
    void exposesFixedStatusValuesInDeclaredOrder() {
        assertArrayEquals(
                new ScheduleStatus[] {ScheduleStatus.UPCOMING, ScheduleStatus.ONGOING, ScheduleStatus.EXPIRED},
                ScheduleStatus.values());
    }

    @Test
    void displayNameReturnsStableChineseText() {
        assertEquals("即将开始", ScheduleStatus.UPCOMING.displayName());
        assertEquals("进行中", ScheduleStatus.ONGOING.displayName());
        assertEquals("已过期", ScheduleStatus.EXPIRED.displayName());
    }

    @Test
    void upcomingSemanticFlagsMatchOnlyUpcoming() {
        assertTrue(ScheduleStatus.UPCOMING.isUpcoming());
        assertFalse(ScheduleStatus.ONGOING.isUpcoming());
        assertFalse(ScheduleStatus.EXPIRED.isUpcoming());
    }

    @Test
    void ongoingSemanticFlagsMatchOnlyOngoing() {
        assertFalse(ScheduleStatus.UPCOMING.isOngoing());
        assertTrue(ScheduleStatus.ONGOING.isOngoing());
        assertFalse(ScheduleStatus.EXPIRED.isOngoing());
    }

    @Test
    void expiredSemanticFlagsMatchOnlyExpired() {
        assertFalse(ScheduleStatus.UPCOMING.isExpired());
        assertFalse(ScheduleStatus.ONGOING.isExpired());
        assertTrue(ScheduleStatus.EXPIRED.isExpired());
    }

    @Test
    void valueOfParsesDeclaredStatusName() {
        assertEquals(ScheduleStatus.ONGOING, ScheduleStatus.valueOf("ONGOING"));
    }

    @Test
    void valueOfRejectsUnknownStatusName() {
        assertThrows(IllegalArgumentException.class, () -> ScheduleStatus.valueOf("ACTIVE"));
    }

    @Test
    void nameUsesStableEnumConstantName() {
        assertEquals("UPCOMING", ScheduleStatus.UPCOMING.name());
    }
}
