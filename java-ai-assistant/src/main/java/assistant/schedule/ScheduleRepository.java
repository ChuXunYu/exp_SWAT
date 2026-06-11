package assistant.schedule;

import assistant.common.EntityId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    void save(ScheduleItem schedule);

    Optional<ScheduleItem> findById(EntityId id);

    List<ScheduleItem> findAll();

    List<ScheduleItem> findBy(ScheduleQuery query, LocalDateTime currentDateTime);

    boolean deleteById(EntityId id);
}
