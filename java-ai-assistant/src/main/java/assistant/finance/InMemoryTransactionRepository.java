package assistant.finance;

import assistant.common.EntityId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<EntityId, TransactionRecord> records = new LinkedHashMap<>();

    @Override
    public void save(TransactionRecord record) {
        Objects.requireNonNull(record, "record");
        records.put(record.getId(), copyOf(record));
    }

    @Override
    public Optional<TransactionRecord> findById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(records.get(id)).map(InMemoryTransactionRepository::copyOf);
    }

    @Override
    public List<TransactionRecord> findAll() {
        return records.values().stream().map(InMemoryTransactionRepository::copyOf).toList();
    }

    @Override
    public List<TransactionRecord> findBy(TransactionQuery query) {
        Objects.requireNonNull(query, "query");
        return records.values().stream()
                .filter(query::matches)
                .map(InMemoryTransactionRepository::copyOf)
                .toList();
    }

    @Override
    public boolean deleteById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return records.remove(id) != null;
    }

    private static TransactionRecord copyOf(TransactionRecord source) {
        Objects.requireNonNull(source, "source");
        return new TransactionRecord(
                source.getId(),
                source.getType(),
                source.getAmount(),
                source.getCategory(),
                source.getDate(),
                source.getNote());
    }
}
