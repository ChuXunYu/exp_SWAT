package assistant.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record MoneyValue(BigDecimal value) {
    public MoneyValue {
        value = Objects.requireNonNull(value, "value");
        try {
            value = value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("value must not require rounding to two decimal places", exception);
        }
    }

    public static MoneyValue zero() {
        return new MoneyValue(BigDecimal.ZERO);
    }

    public static MoneyValue of(String text) {
        Objects.requireNonNull(text, "text");
        return new MoneyValue(new BigDecimal(text.trim()));
    }

    public static MoneyValue from(TransactionAmount amount) {
        Objects.requireNonNull(amount, "amount");
        return new MoneyValue(amount.value());
    }

    public MoneyValue add(MoneyValue other) {
        Objects.requireNonNull(other, "other");
        return new MoneyValue(value.add(other.value()));
    }

    public MoneyValue subtract(MoneyValue other) {
        Objects.requireNonNull(other, "other");
        return new MoneyValue(value.subtract(other.value()));
    }

    public String toPlainString() {
        return value.toPlainString();
    }
}
