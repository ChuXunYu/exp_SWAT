package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.EntityId;
import assistant.common.TransactionAmount;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransactionViewTest {
    private static final EntityId ID = new EntityId(1);
    private static final TransactionAmount AMOUNT = TransactionAmount.of("12.30");
    private static final LocalDate DATE = LocalDate.of(2026, 6, 10);

    @Test
    void fromProjectsTransactionRecordFields() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        TransactionView view = TransactionView.from(record);

        assertView(view, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
    }

    @Test
    void fromRejectsNullRecord() {
        assertThrows(NullPointerException.class, () -> TransactionView.from(null));
    }

    @Test
    void constructorRejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () -> new TransactionView(
                null, TransactionType.INCOME, AMOUNT, "Salary", DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionView(
                ID, null, AMOUNT, "Salary", DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionView(
                ID, TransactionType.INCOME, null, "Salary", DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionView(
                ID, TransactionType.INCOME, AMOUNT, null, DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionView(
                ID, TransactionType.INCOME, AMOUNT, "Salary", null, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionView(
                ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, null));
    }

    @Test
    void constructorNormalizesCategoryAndNote() {
        TransactionView view =
                new TransactionView(ID, TransactionType.EXPENSE, AMOUNT, "\u2003Food\t", DATE, " \tLunch\u2003");

        assertEquals("Food", view.category());
        assertEquals("Lunch", view.note());
    }

    @Test
    void constructorRejectsBlankCategory() {
        for (String blankCategory : new String[] {"", " \t\n", "\u2003"}) {
            assertThrows(IllegalArgumentException.class, () -> new TransactionView(
                    ID, TransactionType.INCOME, AMOUNT, blankCategory, DATE, "Note"));
        }
    }

    @Test
    void viewIsDetachedFromLaterRecordMutation() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
        TransactionView view = TransactionView.from(record);

        record.updateDetails(
                TransactionType.EXPENSE,
                TransactionAmount.of("8.75"),
                "Food",
                LocalDate.of(2026, 6, 11),
                "Lunch");

        assertView(view, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
    }

    private static void assertView(
            TransactionView view,
            EntityId id,
            TransactionType type,
            TransactionAmount amount,
            String category,
            LocalDate date,
            String note) {
        assertAll(
                () -> assertEquals(id, view.id()),
                () -> assertEquals(type, view.type()),
                () -> assertEquals(amount, view.amount()),
                () -> assertEquals(category, view.category()),
                () -> assertEquals(date, view.date()),
                () -> assertEquals(note, view.note()));
    }
}
