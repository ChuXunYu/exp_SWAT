package assistant.finance;

import assistant.common.MoneyValue;
import java.math.BigDecimal;
import java.util.Objects;

public record FinanceStatistics(MoneyValue totalIncome, MoneyValue totalExpense, MoneyValue balance) {
    public FinanceStatistics {
        totalIncome = requireNonNegative(totalIncome, "totalIncome");
        totalExpense = requireNonNegative(totalExpense, "totalExpense");
        balance = Objects.requireNonNull(balance, "balance");
        if (!balance.equals(totalIncome.subtract(totalExpense))) {
            throw new IllegalArgumentException("balance must equal totalIncome minus totalExpense");
        }
    }

    public static FinanceStatistics zero() {
        return new FinanceStatistics(MoneyValue.zero(), MoneyValue.zero(), MoneyValue.zero());
    }

    public static FinanceStatistics of(MoneyValue totalIncome, MoneyValue totalExpense) {
        MoneyValue requiredTotalIncome = requireNonNegative(totalIncome, "totalIncome");
        MoneyValue requiredTotalExpense = requireNonNegative(totalExpense, "totalExpense");
        return new FinanceStatistics(
                requiredTotalIncome, requiredTotalExpense, requiredTotalIncome.subtract(requiredTotalExpense));
    }

    private static MoneyValue requireNonNegative(MoneyValue value, String name) {
        MoneyValue requiredValue = Objects.requireNonNull(value, name);
        if (requiredValue.value().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return requiredValue;
    }
}
