package assistant.finance;

import assistant.common.EntityId;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    void save(TransactionRecord record);

    Optional<TransactionRecord> findById(EntityId id);

    List<TransactionRecord> findAll();

    List<TransactionRecord> findBy(TransactionQuery query);

    boolean deleteById(EntityId id);
}
