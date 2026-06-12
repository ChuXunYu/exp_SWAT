package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.EntityId;
import assistant.common.TransactionAmount;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class FinanceStatisticsServiceTest {
    private static final LocalDate DATE = LocalDate.of(2026, 6, 10);

    @Test
    void calculateReturnsZeroForEmptyRecords() {
        FinanceStatistics statistics = new FinanceStatisticsService().calculate(List.of());

        assertStatistics(statistics, "0.00", "0.00", "0.00");
    }

    @Test
    void calculateAccumulatesIncomeAndExpenseSeparately() {
        FinanceStatistics statistics = new FinanceStatisticsService().calculate(List.of(
                record(1, TransactionType.INCOME, "10.00"),
                record(2, TransactionType.INCOME, "2.50"),
                record(3, TransactionType.EXPENSE, "3.25")));

        assertStatistics(statistics, "12.50", "3.25", "9.25");
    }

    @Test
    void calculateAllowsNegativeBalanceWhenExpenseExceedsIncome() {
        FinanceStatistics statistics = new FinanceStatisticsService().calculate(List.of(
                record(1, TransactionType.INCOME, "3.25"),
                record(2, TransactionType.EXPENSE, "10.00")));

        assertStatistics(statistics, "3.25", "10.00", "-6.75");
    }

    @Test
    void calculateKeepsTwoDecimalMoneyPrecision() {
        FinanceStatistics statistics = new FinanceStatisticsService().calculate(List.of(
                record(1, TransactionType.INCOME, "0.10"),
                record(2, TransactionType.INCOME, "0.20"),
                record(3, TransactionType.EXPENSE, "0.03")));

        assertStatistics(statistics, "0.30", "0.03", "0.27");
    }

    @Test
    void calculateHandlesOnlyIncomeRecords() {
        FinanceStatistics statistics = new FinanceStatisticsService().calculate(List.of(
                record(1, TransactionType.INCOME, "10.00"),
                record(2, TransactionType.INCOME, "2.50")));

        assertStatistics(statistics, "12.50", "0.00", "12.50");
    }

    @Test
    void calculateHandlesOnlyExpenseRecords() {
        FinanceStatistics statistics = new FinanceStatisticsService().calculate(List.of(
                record(1, TransactionType.EXPENSE, "10.00"),
                record(2, TransactionType.EXPENSE, "2.50")));

        assertStatistics(statistics, "0.00", "12.50", "-12.50");
    }

    @Test
    void calculateRejectsNullRecordList() {
        assertThrows(NullPointerException.class, () -> new FinanceStatisticsService().calculate(null));
    }

    @Test
    void calculateRejectsNullRecordElement() {
        assertThrows(NullPointerException.class, () -> new FinanceStatisticsService().calculate(Arrays.asList(
                record(1, TransactionType.INCOME, "10.00"),
                null)));
    }

    private static TransactionRecord record(long id, TransactionType type, String amount) {
        return new TransactionRecord(new EntityId(id), type, TransactionAmount.of(amount), "Category", DATE, "Note");
    }

    private static void assertStatistics(
            FinanceStatistics statistics, String totalIncome, String totalExpense, String balance) {
        assertAll(
                () -> assertEquals(totalIncome, statistics.totalIncome().toPlainString()),
                () -> assertEquals(totalExpense, statistics.totalExpense().toPlainString()),
                () -> assertEquals(balance, statistics.balance().toPlainString()));
    }
}
