package assistant.finance;

import assistant.common.MoneyValue;
import java.util.List;
import java.util.Objects;

public final class FinanceStatisticsService {
    public FinanceStatistics calculate(List<TransactionRecord> records) {
        Objects.requireNonNull(records, "records");
        MoneyValue totalIncome = MoneyValue.zero();
        MoneyValue totalExpense = MoneyValue.zero();

        for (TransactionRecord record : records) {
            Objects.requireNonNull(record, "record");
            MoneyValue amount = amountAsMoney(record);
            if (record.getType().isIncome()) {
                totalIncome = totalIncome.add(amount);
            } else {
                totalExpense = totalExpense.add(amount);
            }
        }

        return FinanceStatistics.of(totalIncome, totalExpense);
    }

    private static MoneyValue amountAsMoney(TransactionRecord record) {
        return MoneyValue.from(record.getAmount());
    }
}
