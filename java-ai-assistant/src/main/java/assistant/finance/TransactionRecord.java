package assistant.finance;

import assistant.common.EntityId;
import assistant.common.TransactionAmount;
import java.time.LocalDate;
import java.util.Objects;

public class TransactionRecord {
    private final EntityId id;
    private TransactionType type;
    private TransactionAmount amount;
    private String category;
    private LocalDate date;
    private String note;

    public TransactionRecord(
            EntityId id, TransactionType type, TransactionAmount amount, String category, LocalDate date, String note) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.category = normalizeCategory(category);
        this.date = Objects.requireNonNull(date, "date");
        this.note = normalizeNote(note);
    }

    public static TransactionRecord create(
            EntityId id, TransactionType type, TransactionAmount amount, String category, LocalDate date, String note) {
        return new TransactionRecord(id, type, amount, category, date, note);
    }

    public EntityId getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionAmount getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public void updateDetails(
            TransactionType type, TransactionAmount amount, String category, LocalDate date, String note) {
        TransactionType requiredType = Objects.requireNonNull(type, "type");
        TransactionAmount requiredAmount = Objects.requireNonNull(amount, "amount");
        String normalizedCategory = normalizeCategory(category);
        LocalDate requiredDate = Objects.requireNonNull(date, "date");
        String normalizedNote = normalizeNote(note);

        this.type = requiredType;
        this.amount = requiredAmount;
        this.category = normalizedCategory;
        this.date = requiredDate;
        this.note = normalizedNote;
    }

    private static String normalizeCategory(String category) {
        String normalizedCategory = Objects.requireNonNull(category, "category").strip();
        if (normalizedCategory.isEmpty()) {
            throw new IllegalArgumentException("category must not be blank");
        }
        return normalizedCategory;
    }

    private static String normalizeNote(String note) {
        if (note == null) {
            return "";
        }
        return note.strip();
    }
}
