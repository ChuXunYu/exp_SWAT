package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.MoneyValue;
import org.junit.jupiter.api.Test;

class FinanceStatisticsTest {
    @Test
    void zeroReturnsAllZeroValues() {
        FinanceStatistics statistics = FinanceStatistics.zero();

        assertStatistics(statistics, "0.00", "0.00", "0.00");
    }

    @Test
    void ofCalculatesPositiveBalance() {
        FinanceStatistics statistics = FinanceStatistics.of(MoneyValue.of("10.00"), MoneyValue.of("3.25"));

        assertStatistics(statistics, "10.00", "3.25", "6.75");
    }

    @Test
    void ofCalculatesZeroBalance() {
        FinanceStatistics statistics = FinanceStatistics.of(MoneyValue.of("10.00"), MoneyValue.of("10.00"));

        assertStatistics(statistics, "10.00", "10.00", "0.00");
    }

    @Test
    void ofAllowsNegativeBalance() {
        FinanceStatistics statistics = FinanceStatistics.of(MoneyValue.of("3.25"), MoneyValue.of("10.00"));

        assertStatistics(statistics, "3.25", "10.00", "-6.75");
    }

    @Test
    void ofRejectsNullTotals() {
        assertThrows(NullPointerException.class, () -> FinanceStatistics.of(null, MoneyValue.zero()));
        assertThrows(NullPointerException.class, () -> FinanceStatistics.of(MoneyValue.zero(), null));
    }

    @Test
    void ofRejectsNegativeIncomeTotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FinanceStatistics.of(MoneyValue.of("-1.00"), MoneyValue.zero()));
    }

    @Test
    void ofRejectsNegativeExpenseTotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FinanceStatistics.of(MoneyValue.zero(), MoneyValue.of("-1.00")));
    }

    @Test
    void canonicalConstructorRejectsNullFields() {
        assertThrows(
                NullPointerException.class,
                () -> new FinanceStatistics(null, MoneyValue.zero(), MoneyValue.zero()));
        assertThrows(
                NullPointerException.class,
                () -> new FinanceStatistics(MoneyValue.zero(), null, MoneyValue.zero()));
        assertThrows(
                NullPointerException.class,
                () -> new FinanceStatistics(MoneyValue.zero(), MoneyValue.zero(), null));
    }

    @Test
    void canonicalConstructorRejectsNegativeTotals() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FinanceStatistics(MoneyValue.of("-1.00"), MoneyValue.zero(), MoneyValue.of("-1.00")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new FinanceStatistics(MoneyValue.zero(), MoneyValue.of("-1.00"), MoneyValue.of("1.00")));
    }

    @Test
    void canonicalConstructorRejectsInconsistentBalance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FinanceStatistics(MoneyValue.of("10.00"), MoneyValue.of("3.00"), MoneyValue.of("8.00")));
    }

    @Test
    void canonicalConstructorAcceptsConsistentNegativeBalance() {
        FinanceStatistics statistics =
                new FinanceStatistics(MoneyValue.of("3.25"), MoneyValue.of("10.00"), MoneyValue.of("-6.75"));

        assertStatistics(statistics, "3.25", "10.00", "-6.75");
    }

    private static void assertStatistics(
            FinanceStatistics statistics, String totalIncome, String totalExpense, String balance) {
        assertAll(
                () -> assertEquals(totalIncome, statistics.totalIncome().toPlainString()),
                () -> assertEquals(totalExpense, statistics.totalExpense().toPlainString()),
                () -> assertEquals(balance, statistics.balance().toPlainString()));
    }
}
