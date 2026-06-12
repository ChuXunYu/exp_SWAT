package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.EntityId;
import assistant.common.TransactionAmount;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransactionRecordTest {
    private static final EntityId ID = new EntityId(1);
    private static final TransactionAmount AMOUNT = TransactionAmount.of("12.30");
    private static final LocalDate DATE = LocalDate.of(2026, 6, 10);

    @Test
    void constructorStoresProvidedFields() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        assertRecordState(record, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
    }

    @Test
    void createFactoryCreatesEquivalentRecord() {
        TransactionRecord record =
                TransactionRecord.create(ID, TransactionType.EXPENSE, AMOUNT, "Food", DATE, "Lunch");

        assertRecordState(record, ID, TransactionType.EXPENSE, AMOUNT, "Food", DATE, "Lunch");
    }

    @Test
    void constructorNormalizesCategoryAndNote() {
        TransactionRecord record = new TransactionRecord(
                ID, TransactionType.EXPENSE, AMOUNT, "\u2003Food\t", DATE, " \tLunch\u2003");

        assertEquals("Food", record.getCategory());
        assertEquals("Lunch", record.getNote());
    }

    @Test
    void constructorConvertsNullNoteToEmptyString() {
        TransactionRecord record = new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, null);

        assertEquals("", record.getNote());
    }

    @Test
    void constructorAllowsBlankNoteAsEmptyString() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, " \t\u2003\n");

        assertEquals("", record.getNote());
    }

    @Test
    void keepsInternalWhitespaceInTextFields() {
        TransactionRecord record = new TransactionRecord(
                ID, TransactionType.EXPENSE, AMOUNT, "Daily  food\tcost", DATE, "Keep  inner\nspacing");

        assertEquals("Daily  food\tcost", record.getCategory());
        assertEquals("Keep  inner\nspacing", record.getNote());
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () -> new TransactionRecord(
                null, TransactionType.INCOME, AMOUNT, "Salary", DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionRecord(
                ID, null, AMOUNT, "Salary", DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionRecord(
                ID, TransactionType.INCOME, null, "Salary", DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionRecord(
                ID, TransactionType.INCOME, AMOUNT, null, DATE, "Note"));
        assertThrows(NullPointerException.class, () -> new TransactionRecord(
                ID, TransactionType.INCOME, AMOUNT, "Salary", null, "Note"));
    }

    @Test
    void rejectsBlankCategory() {
        for (String blankCategory : new String[] {"", " \t\n", "\u2003"}) {
            assertThrows(IllegalArgumentException.class, () -> new TransactionRecord(
                    ID, TransactionType.INCOME, AMOUNT, blankCategory, DATE, "Note"));
        }
    }

    @Test
    void updateDetailsChangesEditableFieldsOnly() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
        TransactionAmount newAmount = TransactionAmount.of("8.75");
        LocalDate newDate = LocalDate.of(2026, 6, 11);

        record.updateDetails(TransactionType.EXPENSE, newAmount, "Food", newDate, "Lunch");

        assertRecordState(record, ID, TransactionType.EXPENSE, newAmount, "Food", newDate, "Lunch");
    }

    @Test
    void updateDetailsNormalizesCategoryAndNote() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        record.updateDetails(TransactionType.EXPENSE, TransactionAmount.of("8.75"), "\u2003Food\t", DATE, " Lunch ");

        assertEquals("Food", record.getCategory());
        assertEquals("Lunch", record.getNote());
    }

    @Test
    void updateDetailsConvertsNullNoteToEmptyString() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        record.updateDetails(TransactionType.EXPENSE, TransactionAmount.of("8.75"), "Food", DATE, null);

        assertEquals("", record.getNote());
    }

    @Test
    void updateDetailsRejectsInvalidRequiredInputAndKeepsFieldsUnchanged() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
        TransactionAmount newAmount = TransactionAmount.of("8.75");
        LocalDate newDate = LocalDate.of(2026, 6, 11);

        assertThrows(NullPointerException.class, () -> record.updateDetails(null, newAmount, "Food", newDate, "Lunch"));
        assertRecordState(record, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        assertThrows(
                NullPointerException.class,
                () -> record.updateDetails(TransactionType.EXPENSE, null, "Food", newDate, "Lunch"));
        assertRecordState(record, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        assertThrows(
                NullPointerException.class,
                () -> record.updateDetails(TransactionType.EXPENSE, newAmount, null, newDate, "Lunch"));
        assertRecordState(record, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        assertThrows(
                NullPointerException.class,
                () -> record.updateDetails(TransactionType.EXPENSE, newAmount, "Food", null, "Lunch"));
        assertRecordState(record, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
    }

    @Test
    void updateDetailsRejectsBlankCategoryAndKeepsFieldsUnchanged() {
        TransactionRecord record =
                new TransactionRecord(ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");

        for (String blankCategory : new String[] {"", " \t\n", "\u2003"}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> record.updateDetails(
                            TransactionType.EXPENSE,
                            TransactionAmount.of("8.75"),
                            blankCategory,
                            LocalDate.of(2026, 6, 11),
                            "Lunch"));
            assertRecordState(record, ID, TransactionType.INCOME, AMOUNT, "Salary", DATE, "June payroll");
        }
    }

    private static void assertRecordState(
            TransactionRecord record,
            EntityId id,
            TransactionType type,
            TransactionAmount amount,
            String category,
            LocalDate date,
            String note) {
        assertAll(
                () -> assertEquals(id, record.getId()),
                () -> assertEquals(type, record.getType()),
                () -> assertEquals(amount, record.getAmount()),
                () -> assertEquals(category, record.getCategory()),
                () -> assertEquals(date, record.getDate()),
                () -> assertEquals(note, record.getNote()));
    }
}
