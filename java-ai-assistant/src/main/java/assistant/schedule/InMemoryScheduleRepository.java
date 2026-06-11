package assistant.schedule;

import assistant.common.EntityId;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryScheduleRepository implements ScheduleRepository {
    private final Map<EntityId, ScheduleItem> schedules = new LinkedHashMap<>();

    @Override
    public void save(ScheduleItem schedule) {
        Objects.requireNonNull(schedule, "schedule");
        schedules.put(schedule.getId(), schedule);
    }

    @Override
    public Optional<ScheduleItem> findById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(schedules.get(id));
    }

    @Override
    public List<ScheduleItem> findAll() {
        return List.copyOf(schedules.values());
    }

    @Override
    public List<ScheduleItem> findBy(ScheduleQuery query, LocalDateTime currentDateTime) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(currentDateTime, "currentDateTime");
        return schedules.values().stream().filter(schedule -> query.matches(schedule, currentDateTime)).toList();
    }

    @Override
    public boolean deleteById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return schedules.remove(id) != null;
    }
}
