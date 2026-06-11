package assistant.testability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FixedTimeProviderTest {
    @Test
    void nowReturnsFixedDateTime() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2026, 6, 11, 9, 30);
        FixedTimeProvider provider = new FixedTimeProvider(fixedDateTime);

        assertEquals(fixedDateTime, provider.now());
    }

    @Test
    void todayReturnsDatePartOfFixedDateTime() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2026, 6, 11, 9, 30);
        FixedTimeProvider provider = new FixedTimeProvider(fixedDateTime);

        assertEquals(LocalDate.of(2026, 6, 11), provider.today());
    }

    @Test
    void todayAndNowStayConsistentAcrossCalls() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2026, 6, 11, 9, 30);
        FixedTimeProvider provider = new FixedTimeProvider(fixedDateTime);

        assertEquals(fixedDateTime, provider.now());
        assertEquals(fixedDateTime, provider.now());
        assertEquals(provider.now().toLocalDate(), provider.today());
        assertEquals(provider.now().toLocalDate(), provider.today());
    }

    @Test
    void rejectsNullFixedDateTime() {
        assertThrows(NullPointerException.class, () -> new FixedTimeProvider(null));
    }

    @Test
    void independentInstancesKeepIndependentFixedTimes() {
        LocalDateTime firstDateTime = LocalDateTime.of(2026, 6, 11, 9, 30);
        LocalDateTime secondDateTime = LocalDateTime.of(2027, 1, 2, 18, 45);
        FixedTimeProvider first = new FixedTimeProvider(firstDateTime);
        FixedTimeProvider second = new FixedTimeProvider(secondDateTime);

        assertEquals(firstDateTime, first.now());
        assertEquals(secondDateTime, second.now());
        assertEquals(firstDateTime.toLocalDate(), first.today());
        assertEquals(secondDateTime.toLocalDate(), second.today());
    }

    @Test
    void canBeUsedThroughTimeProviderInterface() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2026, 6, 11, 9, 30);
        TimeProvider provider = new FixedTimeProvider(fixedDateTime);

        assertEquals(fixedDateTime, provider.now());
        assertEquals(fixedDateTime.toLocalDate(), provider.today());
    }
}
