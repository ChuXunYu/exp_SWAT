package assistant.finance;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.TransactionAmount;
import assistant.testability.IdGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class FinanceService {
    private final TransactionRepository repository;
    private final IdGenerator idGenerator;
    private final FinanceStatisticsService statisticsService;

    public FinanceService(
            TransactionRepository repository, IdGenerator idGenerator, FinanceStatisticsService statisticsService) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService");
    }

    public OperationResult<TransactionView> recordIncome(
            String amountText, String category, LocalDate date, String note) {
        return recordTransaction(TransactionType.INCOME, amountText, category, date, note);
    }

    public OperationResult<TransactionView> recordExpense(
            String amountText, String category, LocalDate date, String note) {
        return recordTransaction(TransactionType.EXPENSE, amountText, category, date, note);
    }

    public OperationResult<TransactionView> getTransaction(EntityId id) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id)
                .map(record -> OperationResult.success(toView(record)))
                .orElseGet(() -> notFound(id));
    }

    public OperationResult<List<TransactionView>> listTransactions() {
        return OperationResult.success(toUnmodifiableViews(repository.findAll()));
    }

    public OperationResult<List<TransactionView>> listTransactions(TransactionQuery query) {
        if (query == null) {
            return validationFailureList("query must not be null");
        }
        return OperationResult.success(toUnmodifiableViews(repository.findBy(query)));
    }

    public OperationResult<TransactionView> updateTransaction(
            EntityId id, TransactionType type, String amountText, String category, LocalDate date, String note) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        if (type == null) {
            return validationFailure("type must not be null");
        }
        return repository.findById(id).map(record -> {
            try {
                record.updateDetails(type, toAmount(amountText), category, date, note);
                repository.save(record);
                return OperationResult.success(toView(record));
            } catch (NullPointerException | IllegalArgumentException exception) {
                return validationFailure(exception.getMessage());
            }
        }).orElseGet(() -> notFound(id));
    }

    public OperationResult<Void> deleteTransaction(EntityId id) {
        if (id == null) {
            return validationFailureVoid("id must not be null");
        }
        if (!repository.deleteById(id)) {
            return notFoundVoid(id);
        }
        return OperationResult.success();
    }

    public OperationResult<FinanceStatistics> calculateStatistics() {
        return OperationResult.success(statisticsService.calculate(repository.findAll()));
    }

    public OperationResult<FinanceStatistics> calculateStatistics(TransactionQuery query) {
        if (query == null) {
            return validationFailureStatistics("query must not be null");
        }
        return OperationResult.success(statisticsService.calculate(repository.findBy(query)));
    }

    private OperationResult<TransactionView> recordTransaction(
            TransactionType type, String amountText, String category, LocalDate date, String note) {
        try {
            TransactionRecord record =
                    TransactionRecord.create(idGenerator.nextId(), type, toAmount(amountText), category, date, note);
            repository.save(record);
            return OperationResult.success(toView(record));
        } catch (NullPointerException | IllegalArgumentException exception) {
            return validationFailure(exception.getMessage());
        }
    }

    private TransactionAmount toAmount(String amountText) {
        return TransactionAmount.of(amountText);
    }

    private static TransactionView toView(TransactionRecord record) {
        return TransactionView.from(record);
    }

    private static List<TransactionView> toUnmodifiableViews(List<TransactionRecord> records) {
        return records.stream().map(FinanceService::toView).toList();
    }

    private OperationResult<TransactionView> validationFailure(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message));
    }

    private OperationResult<List<TransactionView>> validationFailureList(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message));
    }

    private OperationResult<FinanceStatistics> validationFailureStatistics(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message));
    }

    private OperationResult<Void> validationFailureVoid(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message));
    }

    private OperationResult<TransactionView> notFound(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "transaction not found: " + id.value());
    }

    private OperationResult<Void> notFoundVoid(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "transaction not found: " + id.value());
    }

    private static String stableMessage(String message) {
        if (message == null || message.isBlank()) {
            return "invalid transaction input";
        }
        return message;
    }
}
