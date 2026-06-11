package assistant.testability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SystemTimeProviderTest {
    @Test
    void nowReturnsNonNullDateTimeNearInvocationWindow() {
        SystemTimeProvider provider = new SystemTimeProvider();

        LocalDateTime before = LocalDateTime.now();
        LocalDateTime actual = provider.now();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(actual);
        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(after));
    }

    @Test
    void todayReturnsNonNullDateNearInvocationWindow() {
        SystemTimeProvider provider = new SystemTimeProvider();

        LocalDate before = LocalDate.now();
        LocalDate actual = provider.today();
        LocalDate after = LocalDate.now();

        assertNotNull(actual);
        assertTrue(actual.equals(before) || actual.equals(after));
    }

    @Test
    void canBeUsedThroughTimeProviderInterface() {
        TimeProvider provider = new SystemTimeProvider();

        assertNotNull(provider.today());
        assertNotNull(provider.now());
    }
}
