package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.TransactionAmount;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransactionQueryTest {
    private static final LocalDate JUNE_10 = LocalDate.of(2026, 6, 10);
    private static final LocalDate JUNE_11 = LocalDate.of(2026, 6, 11);
    private static final LocalDate JUNE_12 = LocalDate.of(2026, 6, 12);

    @Test
    void allQueryMatchesEveryTransaction() {
        TransactionQuery query = TransactionQuery.all();

        assertTrue(query.matches(record(1, TransactionType.INCOME, "Salary", JUNE_10)));
        assertTrue(query.matches(record(2, TransactionType.EXPENSE, "Food", JUNE_11)));
    }

    @Test
    void typeQueryMatchesOnlySameType() {
        TransactionQuery income = TransactionQuery.byType(TransactionType.INCOME);
        TransactionQuery expense = TransactionQuery.byType(TransactionType.EXPENSE);

        assertAll(
                () -> assertTrue(income.matches(record(1, TransactionType.INCOME, "Salary", JUNE_10))),
                () -> assertFalse(income.matches(record(2, TransactionType.EXPENSE, "Food", JUNE_10))),
                () -> assertTrue(expense.matches(record(3, TransactionType.EXPENSE, "Food", JUNE_10))),
                () -> assertFalse(expense.matches(record(4, TransactionType.INCOME, "Salary", JUNE_10))));
    }

    @Test
    void categoryQueryNormalizesAndMatchesExactCategory() {
        TransactionQuery query = TransactionQuery.byCategory(" \u2003Food\t");

        assertAll(
                () -> assertTrue(query.matches(record(1, TransactionType.EXPENSE, "Food", JUNE_10))),
                () -> assertFalse(query.matches(record(2, TransactionType.EXPENSE, "food", JUNE_10))),
                () -> assertFalse(query.matches(record(3, TransactionType.EXPENSE, "Dining", JUNE_10))));
    }

    @Test
    void dateRangeQueryUsesInclusiveBounds() {
        TransactionQuery query = TransactionQuery.byDateRange(new DateRange(JUNE_10, JUNE_11));

        assertAll(
                () -> assertTrue(query.matches(record(1, TransactionType.EXPENSE, "Food", JUNE_10))),
                () -> assertTrue(query.matches(record(2, TransactionType.EXPENSE, "Food", JUNE_11))),
                () -> assertFalse(query.matches(record(3, TransactionType.EXPENSE, "Food", JUNE_12))));
    }

    @Test
    void combinedQueryRequiresEveryProvidedFilterToMatch() {
        TransactionQuery query =
                TransactionQuery.of(TransactionType.EXPENSE, "Food", new DateRange(JUNE_10, JUNE_11));

        assertAll(
                () -> assertTrue(query.matches(record(1, TransactionType.EXPENSE, "Food", JUNE_10))),
                () -> assertFalse(query.matches(record(2, TransactionType.INCOME, "Food", JUNE_10))),
                () -> assertFalse(query.matches(record(3, TransactionType.EXPENSE, "Salary", JUNE_10))),
                () -> assertFalse(query.matches(record(4, TransactionType.EXPENSE, "Food", JUNE_12))));
    }

    @Test
    void ofAllowsNullComponentsAsWildcards() {
        TransactionQuery byCategory = TransactionQuery.of(null, "Food", null);
        TransactionQuery byTypeAndDate = TransactionQuery.of(TransactionType.EXPENSE, null, new DateRange(JUNE_10, JUNE_10));

        assertAll(
                () -> assertTrue(byCategory.matches(record(1, TransactionType.INCOME, "Food", JUNE_12))),
                () -> assertFalse(byCategory.matches(record(2, TransactionType.EXPENSE, "Dining", JUNE_12))),
                () -> assertTrue(byTypeAndDate.matches(record(3, TransactionType.EXPENSE, "Travel", JUNE_10))),
                () -> assertFalse(byTypeAndDate.matches(record(4, TransactionType.EXPENSE, "Travel", JUNE_11))));
    }

    @Test
    void exposesFilterPresenceFlags() {
        TransactionQuery all = TransactionQuery.all();
        TransactionQuery query = TransactionQuery.of(TransactionType.INCOME, "Salary", new DateRange(JUNE_10, JUNE_11));

        assertAll(
                () -> assertFalse(all.hasTypeFilter()),
                () -> assertFalse(all.hasCategoryFilter()),
                () -> assertFalse(all.hasDateRangeFilter()),
                () -> assertTrue(query.hasTypeFilter()),
                () -> assertTrue(query.hasCategoryFilter()),
                () -> assertTrue(query.hasDateRangeFilter()));
    }

    @Test
    void singleCriterionFactoriesRejectNullCriterion() {
        assertThrows(NullPointerException.class, () -> TransactionQuery.byType(null));
        assertThrows(NullPointerException.class, () -> TransactionQuery.byCategory(null));
        assertThrows(NullPointerException.class, () -> TransactionQuery.byDateRange(null));
    }

    @Test
    void rejectsBlankCategoryFilter() {
        for (String blankCategory : new String[] {"", " \t\n", "\u2003"}) {
            assertThrows(IllegalArgumentException.class, () -> TransactionQuery.byCategory(blankCategory));
            assertThrows(IllegalArgumentException.class, () -> TransactionQuery.of(null, blankCategory, null));
        }
    }

    @Test
    void matchesRejectsNullRecord() {
        assertThrows(NullPointerException.class, () -> TransactionQuery.all().matches(null));
    }

    private static TransactionRecord record(long id, TransactionType type, String category, LocalDate date) {
        return new TransactionRecord(new EntityId(id), type, TransactionAmount.of("12.30"), category, date, "Note");
    }
}
