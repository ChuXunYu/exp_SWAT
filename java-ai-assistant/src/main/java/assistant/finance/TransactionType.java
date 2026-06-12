package assistant.finance;

public enum TransactionType {
    INCOME,
    EXPENSE;

    public boolean isIncome() {
        return this == INCOME;
    }

    public boolean isExpense() {
        return this == EXPENSE;
    }
}
