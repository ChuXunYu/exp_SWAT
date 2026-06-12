package assistant.finance;

import assistant.common.EntityId;
import assistant.common.TransactionAmount;
import java.time.LocalDate;
import java.util.Objects;

public record TransactionView(
        EntityId id, TransactionType type, TransactionAmount amount, String category, LocalDate date, String note) {
    public TransactionView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(amount, "amount");
        category = Objects.requireNonNull(category, "category").strip();
        if (category.isEmpty()) {
            throw new IllegalArgumentException("category must not be blank");
        }
        Objects.requireNonNull(date, "date");
        note = Objects.requireNonNull(note, "note").strip();
    }

    public static TransactionView from(TransactionRecord record) {
        Objects.requireNonNull(record, "record");
        return new TransactionView(
                record.getId(),
                record.getType(),
                record.getAmount(),
                record.getCategory(),
                record.getDate(),
                record.getNote());
    }
}
