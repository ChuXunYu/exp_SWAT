package assistant.testability;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class SystemTimeProvider implements TimeProvider {
    public SystemTimeProvider() {
    }

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
