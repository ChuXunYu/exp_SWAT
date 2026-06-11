package assistant.common;

import java.math.BigDecimal;
import java.util.Objects;

public record TransactionAmount(BigDecimal value) {
    public TransactionAmount {
        value = Objects.requireNonNull(value, "value");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
        if (value.scale() > 2) {
            throw new IllegalArgumentException("value must have at most two decimal places");
        }
        value = value.setScale(2);
    }

    public static TransactionAmount of(String text) {
        Objects.requireNonNull(text, "text");
        return new TransactionAmount(new BigDecimal(text.trim()));
    }
}
