package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.TransactionAmount;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryTransactionRepositoryTest {
    private static final LocalDate JUNE_10 = LocalDate.of(2026, 6, 10);
    private static final LocalDate JUNE_11 = LocalDate.of(2026, 6, 11);
    private static final LocalDate JUNE_12 = LocalDate.of(2026, 6, 12);

    @Test
    void saveAndFindByIdReturnsDetachedSnapshot() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        TransactionRecord record = record(1, TransactionType.INCOME, "10.00", "Salary", JUNE_10);

        repository.save(record);
        TransactionRecord found = repository.findById(new EntityId(1)).orElseThrow();

        assertAll(
                () -> assertNotSame(record, found),
                () -> assertRecordState(found, new EntityId(1), TransactionType.INCOME, "10.00", "Salary", JUNE_10, "Note"));
    }

    @Test
    void saveCopiesInputRecordSoLaterCallerMutationsDoNotAffectRepository() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        TransactionRecord record = record(1, TransactionType.INCOME, "10.00", "Salary", JUNE_10);

        repository.save(record);
        record.updateDetails(TransactionType.EXPENSE, TransactionAmount.of("8.75"), "Food", JUNE_11, "Lunch");

        assertRecordState(
                repository.findById(new EntityId(1)).orElseThrow(),
                new EntityId(1),
                TransactionType.INCOME,
                "10.00",
                "Salary",
                JUNE_10,
                "Note");
    }

    @Test
    void findByIdReturnsEmptyWhenRecordDoesNotExist() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();

        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void saveReplacesRecordWithSameIdAndKeepsInsertionOrder() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        repository.save(record(1, TransactionType.INCOME, "10.00", "Salary", JUNE_10));
        repository.save(record(2, TransactionType.EXPENSE, "5.00", "Food", JUNE_11));
        repository.save(record(1, TransactionType.EXPENSE, "8.75", "Dining", JUNE_12));

        List<TransactionRecord> records = repository.findAll();

        assertEquals(List.of(new EntityId(1), new EntityId(2)), idsOf(records));
        assertRecordState(records.get(0), new EntityId(1), TransactionType.EXPENSE, "8.75", "Dining", JUNE_12, "Note");
    }

    @Test
    void findAllReturnsRecordsInInsertionOrder() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        repository.save(record(1, TransactionType.INCOME, "10.00", "Salary", JUNE_10));
        repository.save(record(2, TransactionType.EXPENSE, "5.00", "Food", JUNE_11));

        assertEquals(List.of(new EntityId(1), new EntityId(2)), idsOf(repository.findAll()));
    }

    @Test
    void findAllReturnsUnmodifiableDetachedSnapshotList() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        repository.save(record(1, TransactionType.INCOME, "10.00", "Salary", JUNE_10));

        List<TransactionRecord> snapshot = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(record(2, TransactionType.EXPENSE, "5.00", "Food", JUNE_11)));
        snapshot.get(0).updateDetails(TransactionType.EXPENSE, TransactionAmount.of("8.75"), "Dining", JUNE_12, "Dinner");
        assertRecordState(
                repository.findById(new EntityId(1)).orElseThrow(),
                new EntityId(1),
                TransactionType.INCOME,
                "10.00",
                "Salary",
                JUNE_10,
                "Note");
    }

    @Test
    void findByFiltersByTypeCategoryAndDateRange() {
        InMemoryTransactionRepository repository = repositoryWithMixedRecords();

        assertEquals(List.of(new EntityId(1)), idsOf(repository.findBy(TransactionQuery.byType(TransactionType.INCOME))));
        assertEquals(List.of(new EntityId(2), new EntityId(3)), idsOf(repository.findBy(TransactionQuery.byCategory("Food"))));
        assertEquals(
                List.of(new EntityId(1), new EntityId(2)),
                idsOf(repository.findBy(TransactionQuery.byDateRange(new DateRange(JUNE_10, JUNE_11)))));
    }

    @Test
    void findByAppliesCombinedQueryInInsertionOrder() {
        InMemoryTransactionRepository repository = repositoryWithMixedRecords();

        List<TransactionRecord> result = repository.findBy(
                TransactionQuery.of(TransactionType.EXPENSE, "Food", new DateRange(JUNE_10, JUNE_12)));

        assertEquals(List.of(new EntityId(2), new EntityId(3)), idsOf(result));
    }

    @Test
    void findByReturnsUnmodifiableDetachedSnapshotList() {
        InMemoryTransactionRepository repository = repositoryWithMixedRecords();

        List<TransactionRecord> snapshot = repository.findBy(TransactionQuery.byCategory("Food"));

        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
        snapshot.get(0).updateDetails(TransactionType.INCOME, TransactionAmount.of("99.00"), "Salary", JUNE_12, "Changed");
        assertRecordState(
                repository.findById(new EntityId(2)).orElseThrow(),
                new EntityId(2),
                TransactionType.EXPENSE,
                "5.00",
                "Food",
                JUNE_11,
                "Note");
    }

    @Test
    void mutatingRecordReturnedFromFindByIdDoesNotAffectStoredState() {
        InMemoryTransactionRepository repository = repositoryWithMixedRecords();
        TransactionRecord found = repository.findById(new EntityId(1)).orElseThrow();

        found.updateDetails(TransactionType.EXPENSE, TransactionAmount.of("8.75"), "Dining", JUNE_12, "Changed");

        assertRecordState(
                repository.findById(new EntityId(1)).orElseThrow(),
                new EntityId(1),
                TransactionType.INCOME,
                "10.00",
                "Salary",
                JUNE_10,
                "Note");
    }

    @Test
    void mutatingRecordReturnedFromFindAllDoesNotAffectStoredState() {
        InMemoryTransactionRepository repository = repositoryWithMixedRecords();
        TransactionRecord found = repository.findAll().get(0);

        found.updateDetails(TransactionType.EXPENSE, TransactionAmount.of("8.75"), "Dining", JUNE_12, "Changed");

        assertRecordState(
                repository.findById(new EntityId(1)).orElseThrow(),
                new EntityId(1),
                TransactionType.INCOME,
                "10.00",
                "Salary",
                JUNE_10,
                "Note");
    }

    @Test
    void mutatingRecordReturnedFromFindByDoesNotAffectStoredState() {
        InMemoryTransactionRepository repository = repositoryWithMixedRecords();
        TransactionRecord found = repository.findBy(TransactionQuery.byType(TransactionType.INCOME)).get(0);

        found.updateDetails(TransactionType.EXPENSE, TransactionAmount.of("8.75"), "Dining", JUNE_12, "Changed");

        assertRecordState(
                repository.findById(new EntityId(1)).orElseThrow(),
                new EntityId(1),
                TransactionType.INCOME,
                "10.00",
                "Salary",
                JUNE_10,
                "Note");
    }

    @Test
    void deleteByIdRemovesExistingRecord() {
        InMemoryTransactionRepository repository = repositoryWithMixedRecords();

        assertTrue(repository.deleteById(new EntityId(1)));
        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void deleteByIdReturnsFalseWhenRecordDoesNotExist() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();

        assertFalse(repository.deleteById(new EntityId(1)));
    }

    @Test
    void methodsRejectNullArguments() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();

        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.findBy(null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
    }

    private static InMemoryTransactionRepository repositoryWithMixedRecords() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        repository.save(record(1, TransactionType.INCOME, "10.00", "Salary", JUNE_10));
        repository.save(record(2, TransactionType.EXPENSE, "5.00", "Food", JUNE_11));
        repository.save(record(3, TransactionType.EXPENSE, "8.75", "Food", JUNE_12));
        return repository;
    }

    private static TransactionRecord record(
            long id, TransactionType type, String amount, String category, LocalDate date) {
        return new TransactionRecord(new EntityId(id), type, TransactionAmount.of(amount), category, date, "Note");
    }

    private static List<EntityId> idsOf(List<TransactionRecord> records) {
        return records.stream().map(TransactionRecord::getId).toList();
    }

    private static void assertRecordState(
            TransactionRecord record,
            EntityId id,
            TransactionType type,
            String amount,
            String category,
            LocalDate date,
            String note) {
        assertAll(
                () -> assertEquals(id, record.getId()),
                () -> assertEquals(type, record.getType()),
                () -> assertEquals(TransactionAmount.of(amount), record.getAmount()),
                () -> assertEquals(category, record.getCategory()),
                () -> assertEquals(date, record.getDate()),
                () -> assertEquals(note, record.getNote()));
    }
}
