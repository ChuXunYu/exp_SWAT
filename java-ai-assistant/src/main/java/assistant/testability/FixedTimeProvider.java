package assistant.testability;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public final class FixedTimeProvider implements TimeProvider {
    private final LocalDateTime fixedDateTime;

    public FixedTimeProvider(LocalDateTime fixedDateTime) {
        this.fixedDateTime = Objects.requireNonNull(fixedDateTime, "fixedDateTime");
    }

    @Override
    public LocalDate today() {
        return fixedDateTime.toLocalDate();
    }

    @Override
    public LocalDateTime now() {
        return fixedDateTime;
    }
}
