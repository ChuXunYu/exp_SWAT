package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionAmountTest {
    @Test
    void constructsPositiveIntegerAmountAndNormalizesScale() {
        TransactionAmount amount = new TransactionAmount(new BigDecimal("12"));

        assertEquals(new BigDecimal("12.00"), amount.value());
        assertEquals(2, amount.value().scale());
    }

    @Test
    void constructsPositiveOneDecimalAmountAndNormalizesScale() {
        TransactionAmount amount = new TransactionAmount(new BigDecimal("12.3"));

        assertEquals(new BigDecimal("12.30"), amount.value());
    }

    @Test
    void constructsPositiveTwoDecimalAmount() {
        TransactionAmount amount = new TransactionAmount(new BigDecimal("12.34"));

        assertEquals(new BigDecimal("12.34"), amount.value());
    }

    @Test
    void rejectsNullValue() {
        assertThrows(NullPointerException.class, () -> new TransactionAmount(null));
    }

    @Test
    void rejectsZeroValue() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionAmount(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new TransactionAmount(new BigDecimal("0.00")));
    }

    @Test
    void rejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionAmount(new BigDecimal("-1.00")));
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionAmount(new BigDecimal("1.234")));
    }

    @Test
    void rejectsMoreThanTwoDecimalPlacesEvenWhenTrailingZeros() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionAmount(new BigDecimal("1.230")));
    }

    @Test
    void createsAmountFromStringAndTrimsWhitespace() {
        TransactionAmount amount = TransactionAmount.of(" 12.30 ");

        assertEquals(new BigDecimal("12.30"), amount.value());
    }

    @Test
    void stringFactoryRejectsNullText() {
        assertThrows(NullPointerException.class, () -> TransactionAmount.of(null));
    }

    @Test
    void stringFactoryRejectsBlankText() {
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of("   "));
    }

    @Test
    void stringFactoryRejectsInvalidNumberText() {
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of("abc"));
    }

    @Test
    void stringFactoryRejectsAmountsThatViolateTransactionConstraints() {
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of("0"));
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of("-1.00"));
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of("1.234"));
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of("1.230"));
    }

    @Test
    void equalityAndHashCodeUseNormalizedAmount() {
        TransactionAmount amount = new TransactionAmount(new BigDecimal("1.2"));
        TransactionAmount same = new TransactionAmount(new BigDecimal("1.20"));

        assertEquals(same, amount);
        assertEquals(same.hashCode(), amount.hashCode());
    }

    @Test
    void toStringUsesRecordComponentNameAndNormalizedValue() {
        TransactionAmount amount = new TransactionAmount(new BigDecimal("12.3"));

        assertEquals("TransactionAmount[value=12.30]", amount.toString());
        assertTrue(amount.toString().contains("value=12.30"));
    }
}
