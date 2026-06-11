package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyValueTest {
    @Test
    void constructsZeroValueAndFormatsWithTwoDecimals() {
        MoneyValue value = new MoneyValue(BigDecimal.ZERO);

        assertEquals("0.00", value.toPlainString());
    }

    @Test
    void zeroFactoryReturnsZeroWithTwoDecimals() {
        MoneyValue value = MoneyValue.zero();

        assertEquals("0.00", value.toPlainString());
        assertEquals(2, value.value().scale());
    }

    @Test
    void constructsPositiveValueAndFormatsWithTwoDecimals() {
        MoneyValue value = new MoneyValue(new BigDecimal("1.2"));

        assertEquals("1.20", value.toPlainString());
    }

    @Test
    void constructsNegativeValueAndFormatsWithTwoDecimals() {
        MoneyValue value = new MoneyValue(new BigDecimal("-3.4"));

        assertEquals("-3.40", value.toPlainString());
    }

    @Test
    void preservesTwoDecimalInput() {
        MoneyValue value = new MoneyValue(new BigDecimal("5.67"));

        assertEquals(new BigDecimal("5.67"), value.value());
    }

    @Test
    void normalizesTrailingZerosToTwoDecimals() {
        MoneyValue value = new MoneyValue(new BigDecimal("1.2000"));

        assertEquals("1.20", value.toPlainString());
    }

    @Test
    void rejectsNullValue() {
        assertThrows(NullPointerException.class, () -> new MoneyValue(null));
    }

    @Test
    void rejectsValueThatRequiresRounding() {
        assertThrows(IllegalArgumentException.class, () -> new MoneyValue(new BigDecimal("1.234")));
    }

    @Test
    void createsMoneyValueFromStringAndTrimsWhitespace() {
        MoneyValue value = MoneyValue.of(" -3.40 ");

        assertEquals("-3.40", value.toPlainString());
    }

    @Test
    void stringFactoryRejectsNullText() {
        assertThrows(NullPointerException.class, () -> MoneyValue.of(null));
    }

    @Test
    void stringFactoryRejectsBlankText() {
        assertThrows(IllegalArgumentException.class, () -> MoneyValue.of("   "));
    }

    @Test
    void stringFactoryRejectsInvalidNumberText() {
        assertThrows(IllegalArgumentException.class, () -> MoneyValue.of("abc"));
    }

    @Test
    void stringFactoryRejectsValueThatRequiresRounding() {
        assertThrows(IllegalArgumentException.class, () -> MoneyValue.of("1.234"));
    }

    @Test
    void createsMoneyValueFromTransactionAmount() {
        MoneyValue value = MoneyValue.from(new TransactionAmount(new BigDecimal("8.50")));

        assertEquals("8.50", value.toPlainString());
    }

    @Test
    void fromTransactionAmountRejectsNull() {
        assertThrows(NullPointerException.class, () -> MoneyValue.from(null));
    }

    @Test
    void addReturnsNewMoneyValueWithTwoDecimalResult() {
        MoneyValue left = MoneyValue.of("1.20");
        MoneyValue right = MoneyValue.of("2.30");

        MoneyValue result = left.add(right);

        assertEquals("3.50", result.toPlainString());
        assertEquals("1.20", left.toPlainString());
        assertEquals("2.30", right.toPlainString());
    }

    @Test
    void addAllowsNegativeOperand() {
        MoneyValue result = MoneyValue.of("5.00").add(MoneyValue.of("-2.25"));

        assertEquals("2.75", result.toPlainString());
    }

    @Test
    void addRejectsNullOther() {
        assertThrows(NullPointerException.class, () -> MoneyValue.zero().add(null));
    }

    @Test
    void subtractReturnsNewMoneyValueWithTwoDecimalResult() {
        MoneyValue left = MoneyValue.of("5.00");
        MoneyValue right = MoneyValue.of("1.25");

        MoneyValue result = left.subtract(right);

        assertEquals("3.75", result.toPlainString());
        assertEquals("5.00", left.toPlainString());
        assertEquals("1.25", right.toPlainString());
    }

    @Test
    void subtractAllowsNegativeResult() {
        MoneyValue result = MoneyValue.of("1.00").subtract(MoneyValue.of("3.40"));

        assertEquals("-2.40", result.toPlainString());
    }

    @Test
    void subtractRejectsNullOther() {
        assertThrows(NullPointerException.class, () -> MoneyValue.zero().subtract(null));
    }

    @Test
    void equalityAndHashCodeUseNormalizedAmount() {
        MoneyValue value = new MoneyValue(new BigDecimal("1.2"));
        MoneyValue same = new MoneyValue(new BigDecimal("1.20"));
        MoneyValue sameWithTrailingZeros = new MoneyValue(new BigDecimal("1.2000"));

        assertEquals(same, value);
        assertEquals(sameWithTrailingZeros, value);
        assertEquals(same.hashCode(), value.hashCode());
        assertEquals(sameWithTrailingZeros.hashCode(), value.hashCode());
    }

    @Test
    void toStringUsesRecordComponentNameAndNormalizedValue() {
        MoneyValue value = new MoneyValue(new BigDecimal("-3.4"));

        assertEquals("MoneyValue[value=-3.40]", value.toString());
        assertTrue(value.toString().contains("value=-3.40"));
    }
}
