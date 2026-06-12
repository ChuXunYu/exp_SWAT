package assistant.finance;

import assistant.common.DateRange;
import java.util.Objects;

public record TransactionQuery(TransactionType type, String category, DateRange dateRange) {
    public TransactionQuery {
        if (category != null) {
            category = category.strip();
            if (category.isEmpty()) {
                throw new IllegalArgumentException("category must not be blank");
            }
        }
    }

    public static TransactionQuery all() {
        return new TransactionQuery(null, null, null);
    }

    public static TransactionQuery byType(TransactionType type) {
        return new TransactionQuery(Objects.requireNonNull(type, "type"), null, null);
    }

    public static TransactionQuery byCategory(String category) {
        return new TransactionQuery(null, Objects.requireNonNull(category, "category"), null);
    }

    public static TransactionQuery byDateRange(DateRange dateRange) {
        return new TransactionQuery(null, null, Objects.requireNonNull(dateRange, "dateRange"));
    }

    public static TransactionQuery of(TransactionType type, String category, DateRange dateRange) {
        return new TransactionQuery(type, category, dateRange);
    }

    public boolean hasTypeFilter() {
        return type != null;
    }

    public boolean hasCategoryFilter() {
        return category != null;
    }

    public boolean hasDateRangeFilter() {
        return dateRange != null;
    }

    public boolean matches(TransactionRecord record) {
        Objects.requireNonNull(record, "record");
        return (!hasTypeFilter() || record.getType() == type)
                && (!hasCategoryFilter() || record.getCategory().equals(category))
                && (!hasDateRangeFilter() || dateRange.contains(record.getDate()));
    }
}
