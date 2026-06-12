package assistant.finance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.TransactionAmount;
import assistant.testability.IncrementalIdGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FinanceServiceTest {
    private static final LocalDate JUNE_10 = LocalDate.of(2026, 6, 10);
    private static final LocalDate JUNE_11 = LocalDate.of(2026, 6, 11);
    private static final LocalDate JUNE_12 = LocalDate.of(2026, 6, 12);

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> new FinanceService(
                null, new IncrementalIdGenerator(100), new FinanceStatisticsService()));
        assertThrows(NullPointerException.class, () -> new FinanceService(
                new InMemoryTransactionRepository(), null, new FinanceStatisticsService()));
        assertThrows(NullPointerException.class, () -> new FinanceService(
                new InMemoryTransactionRepository(), new IncrementalIdGenerator(100), null));
    }

    @Test
    void recordIncomeCreatesRecordAndReturnsView() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        FinanceService service = new FinanceService(repository, new IncrementalIdGenerator(100), new FinanceStatisticsService());

        OperationResult<TransactionView> result = service.recordIncome("10.00", "Salary", JUNE_10, "June payroll");

        assertSuccess(result);
        assertView(result.getPayload(), new EntityId(100), TransactionType.INCOME, "10.00", "Salary", JUNE_10, "June payroll");
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void recordExpenseCreatesRecordAndReturnsView() {
        FinanceService service = newService(100);

        OperationResult<TransactionView> result = service.recordExpense("8.75", "Food", JUNE_10, "Lunch");

        assertSuccess(result);
        assertView(result.getPayload(), new EntityId(100), TransactionType.EXPENSE, "8.75", "Food", JUNE_10, "Lunch");
    }

    @Test
    void recordTransactionNormalizesCategoryAndNote() {
        FinanceService service = newService(100);

        OperationResult<TransactionView> result = service.recordExpense("8.75", " \u2003Food\t", JUNE_10, " Lunch ");

        assertSuccess(result);
        assertEquals("Food", result.getPayload().category());
        assertEquals("Lunch", result.getPayload().note());
    }

    @Test
    void recordTransactionRejectsInvalidAmountCategoryAndDateAndKeepsRepositoryUnchanged() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        FinanceService service = new FinanceService(repository, new IncrementalIdGenerator(100), new FinanceStatisticsService());

        assertFailure(service.recordIncome("bad", "Salary", JUNE_10, "Note"), ErrorCode.VALIDATION_ERROR);
        assertFailure(service.recordExpense("8.75", " \t\n", JUNE_10, "Note"), ErrorCode.VALIDATION_ERROR);
        assertFailure(service.recordExpense("8.75", "Food", null, "Note"), ErrorCode.VALIDATION_ERROR);
        assertFailure(service.recordExpense(null, "Food", JUNE_10, "Note"), ErrorCode.VALIDATION_ERROR);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void getTransactionReturnsViewForExistingRecord() {
        FinanceService service = newService(100);
        service.recordIncome("10.00", "Salary", JUNE_10, "Note");

        OperationResult<TransactionView> result = service.getTransaction(new EntityId(100));

        assertSuccess(result);
        assertInstanceOf(TransactionView.class, result.getPayload());
        assertEquals("Salary", result.getPayload().category());
    }

    @Test
    void getTransactionReturnsNotFoundForMissingRecord() {
        OperationResult<TransactionView> result = newService(100).getTransaction(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void getTransactionRejectsNullId() {
        OperationResult<TransactionView> result = newService(100).getTransaction(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void listTransactionsReturnsUnmodifiableViewsInInsertionOrder() {
        FinanceService service = newService(100);
        service.recordIncome("10.00", "Salary", JUNE_10, "Note");
        service.recordExpense("8.75", "Food", JUNE_11, "Lunch");

        OperationResult<List<TransactionView>> result = service.listTransactions();

        assertSuccess(result);
        List<TransactionView> views = result.getPayload();
        assertAll(
                () -> assertEquals(List.of(new EntityId(100), new EntityId(101)), idsOf(views)),
                () -> assertInstanceOf(TransactionView.class, views.get(0)),
                () -> assertThrows(UnsupportedOperationException.class, () -> views.clear()));
    }

    @Test
    void listTransactionsWithQueryFiltersByTypeCategoryDateRangeAndCombination() {
        FinanceService service = serviceWithMixedTransactions();

        assertEquals(
                List.of(new EntityId(100)),
                idsOf(service.listTransactions(TransactionQuery.byType(TransactionType.INCOME)).getPayload()));
        assertEquals(
                List.of(new EntityId(101), new EntityId(102)),
                idsOf(service.listTransactions(TransactionQuery.byCategory("Food")).getPayload()));
        assertEquals(
                List.of(new EntityId(100), new EntityId(101)),
                idsOf(service.listTransactions(TransactionQuery.byDateRange(new DateRange(JUNE_10, JUNE_11))).getPayload()));
        assertEquals(
                List.of(new EntityId(101), new EntityId(102)),
                idsOf(service.listTransactions(TransactionQuery.of(TransactionType.EXPENSE, "Food", new DateRange(JUNE_10, JUNE_12))).getPayload()));
    }

    @Test
    void listTransactionsRejectsNullQuery() {
        OperationResult<List<TransactionView>> result = newService(100).listTransactions(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void updateTransactionChangesEditableFieldsAndReturnsView() {
        FinanceService service = newService(100);
        service.recordIncome("10.00", "Salary", JUNE_10, "Note");

        OperationResult<TransactionView> result =
                service.updateTransaction(new EntityId(100), TransactionType.EXPENSE, "8.75", "Food", JUNE_11, "Lunch");

        assertSuccess(result);
        assertView(result.getPayload(), new EntityId(100), TransactionType.EXPENSE, "8.75", "Food", JUNE_11, "Lunch");
        assertView(
                service.getTransaction(new EntityId(100)).getPayload(),
                new EntityId(100),
                TransactionType.EXPENSE,
                "8.75",
                "Food",
                JUNE_11,
                "Lunch");
    }

    @Test
    void updateTransactionRejectsNullId() {
        OperationResult<TransactionView> result =
                newService(100).updateTransaction(null, TransactionType.EXPENSE, "8.75", "Food", JUNE_11, "Lunch");

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void updateTransactionRejectsMissingId() {
        OperationResult<TransactionView> result =
                newService(100).updateTransaction(new EntityId(999), TransactionType.EXPENSE, "8.75", "Food", JUNE_11, "Lunch");

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void updateTransactionRejectsNullTypeAndKeepsRecordUnchanged() {
        FinanceService service = newService(100);
        service.recordIncome("10.00", "Salary", JUNE_10, "Note");
        TransactionView before = service.getTransaction(new EntityId(100)).getPayload();

        OperationResult<TransactionView> result =
                service.updateTransaction(new EntityId(100), null, "8.75", "Food", JUNE_11, "Lunch");

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
        assertSameView(before, service.getTransaction(new EntityId(100)).getPayload());
    }

    @Test
    void updateTransactionRejectsInvalidInputAndKeepsRecordUnchanged() {
        FinanceService service = newService(100);
        service.recordIncome("10.00", "Salary", JUNE_10, "Note");
        TransactionView before = service.getTransaction(new EntityId(100)).getPayload();

        assertFailure(
                service.updateTransaction(new EntityId(100), TransactionType.EXPENSE, "bad", "Food", JUNE_11, "Lunch"),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateTransaction(new EntityId(100), TransactionType.EXPENSE, "8.75", " \t", JUNE_11, "Lunch"),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateTransaction(new EntityId(100), TransactionType.EXPENSE, "8.75", "Food", null, "Lunch"),
                ErrorCode.VALIDATION_ERROR);

        assertSameView(before, service.getTransaction(new EntityId(100)).getPayload());
    }

    @Test
    void deleteTransactionRemovesExistingRecord() {
        FinanceService service = newService(100);
        service.recordIncome("10.00", "Salary", JUNE_10, "Note");

        OperationResult<Void> result = service.deleteTransaction(new EntityId(100));

        assertSuccess(result);
        assertFailure(service.getTransaction(new EntityId(100)), ErrorCode.NOT_FOUND);
    }

    @Test
    void deleteTransactionRejectsNullId() {
        OperationResult<Void> result = newService(100).deleteTransaction(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void deleteTransactionReturnsNotFoundForMissingRecord() {
        OperationResult<Void> result = newService(100).deleteTransaction(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void calculateStatisticsReturnsTotalsForAllCurrentRecords() {
        FinanceService service = serviceWithMixedTransactions();

        OperationResult<FinanceStatistics> result = service.calculateStatistics();

        assertSuccess(result);
        assertStatistics(result.getPayload(), "10.00", "13.75", "-3.75");
    }

    @Test
    void calculateStatisticsWithQueryReturnsTotalsForMatchingRecords() {
        FinanceService service = serviceWithMixedTransactions();

        OperationResult<FinanceStatistics> result =
                service.calculateStatistics(TransactionQuery.byCategory("Food"));

        assertSuccess(result);
        assertStatistics(result.getPayload(), "0.00", "13.75", "-13.75");
    }

    @Test
    void calculateStatisticsRejectsNullQuery() {
        CountingTransactionRepository repository = new CountingTransactionRepository();
        FinanceService service = new FinanceService(repository, new IncrementalIdGenerator(100), new FinanceStatisticsService());

        OperationResult<FinanceStatistics> result = service.calculateStatistics(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
        assertEquals(0, repository.findByCalls);
    }

    @Test
    void statisticsReflectCreateUpdateAndDeleteChanges() {
        FinanceService service = newService(100);
        service.recordIncome("20.00", "Salary", JUNE_10, "Note");
        service.recordExpense("8.75", "Food", JUNE_11, "Lunch");
        assertStatistics(service.calculateStatistics().getPayload(), "20.00", "8.75", "11.25");

        service.updateTransaction(new EntityId(101), TransactionType.INCOME, "5.00", "Refund", JUNE_12, "Return");
        assertStatistics(service.calculateStatistics().getPayload(), "25.00", "0.00", "25.00");

        service.deleteTransaction(new EntityId(100));
        assertStatistics(service.calculateStatistics().getPayload(), "5.00", "0.00", "5.00");
    }

    @Test
    void serviceNeverReturnsMutableTransactionRecordReferences() {
        FinanceService service = newService(100);
        TransactionView original = service.recordIncome("10.00", "Salary", JUNE_10, "Note").getPayload();
        List<TransactionView> originalList = service.listTransactions().getPayload();

        service.updateTransaction(new EntityId(100), TransactionType.EXPENSE, "8.75", "Food", JUNE_11, "Lunch");
        service.recordExpense("2.00", "Transport", JUNE_12, "Bus");

        assertAll(
                () -> assertSameView(
                        new TransactionView(new EntityId(100), TransactionType.INCOME, TransactionAmount.of("10.00"), "Salary", JUNE_10, "Note"),
                        original),
                () -> assertEquals(1, originalList.size()),
                () -> assertEquals("Salary", originalList.get(0).category()),
                () -> assertThrows(UnsupportedOperationException.class, () -> originalList.clear()));
    }

    private static FinanceService newService(long startInclusive) {
        return new FinanceService(
                new InMemoryTransactionRepository(),
                new IncrementalIdGenerator(startInclusive),
                new FinanceStatisticsService());
    }

    private static FinanceService serviceWithMixedTransactions() {
        FinanceService service = newService(100);
        service.recordIncome("10.00", "Salary", JUNE_10, "Note");
        service.recordExpense("5.00", "Food", JUNE_11, "Lunch");
        service.recordExpense("8.75", "Food", JUNE_12, "Dinner");
        return service;
    }

    private static List<EntityId> idsOf(List<TransactionView> views) {
        return views.stream().map(TransactionView::id).toList();
    }

    private static void assertView(
            TransactionView view,
            EntityId id,
            TransactionType type,
            String amount,
            String category,
            LocalDate date,
            String note) {
        assertAll(
                () -> assertEquals(id, view.id()),
                () -> assertEquals(type, view.type()),
                () -> assertEquals(TransactionAmount.of(amount), view.amount()),
                () -> assertEquals(category, view.category()),
                () -> assertEquals(date, view.date()),
                () -> assertEquals(note, view.note()));
    }

    private static void assertSameView(TransactionView expected, TransactionView actual) {
        assertAll(
                () -> assertEquals(expected.id(), actual.id()),
                () -> assertEquals(expected.type(), actual.type()),
                () -> assertEquals(expected.amount(), actual.amount()),
                () -> assertEquals(expected.category(), actual.category()),
                () -> assertEquals(expected.date(), actual.date()),
                () -> assertEquals(expected.note(), actual.note()));
    }

    private static void assertStatistics(
            FinanceStatistics statistics, String totalIncome, String totalExpense, String balance) {
        assertAll(
                () -> assertEquals(totalIncome, statistics.totalIncome().toPlainString()),
                () -> assertEquals(totalExpense, statistics.totalExpense().toPlainString()),
                () -> assertEquals(balance, statistics.balance().toPlainString()));
    }

    private static <T> void assertSuccess(OperationResult<T> result) {
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
    }

    private static <T> void assertFailure(OperationResult<T> result, ErrorCode errorCode) {
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals(errorCode, result.getErrorCode());
    }

    private static final class CountingTransactionRepository implements TransactionRepository {
        private int findByCalls;

        @Override
        public void save(TransactionRecord record) {
            Objects.requireNonNull(record, "record");
        }

        @Override
        public Optional<TransactionRecord> findById(EntityId id) {
            Objects.requireNonNull(id, "id");
            return Optional.empty();
        }

        @Override
        public List<TransactionRecord> findAll() {
            return List.of();
        }

        @Override
        public List<TransactionRecord> findBy(TransactionQuery query) {
            Objects.requireNonNull(query, "query");
            findByCalls++;
            return List.of();
        }

        @Override
        public boolean deleteById(EntityId id) {
            Objects.requireNonNull(id, "id");
            return false;
        }
    }
}
