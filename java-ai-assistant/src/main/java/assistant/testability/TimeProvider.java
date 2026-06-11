package assistant.testability;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDate today();

    LocalDateTime now();
}
