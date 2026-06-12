package assistant.study;

import assistant.common.EntityId;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryStudyPlanRepository implements StudyPlanRepository {
    private final Map<EntityId, StudyPlan> plans = new LinkedHashMap<>();

    @Override
    public void save(StudyPlan plan) {
        Objects.requireNonNull(plan, "plan");
        plans.put(plan.getId(), copyOf(plan));
    }

    @Override
    public Optional<StudyPlan> findById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(plans.get(id)).map(InMemoryStudyPlanRepository::copyOf);
    }

    @Override
    public List<StudyPlan> findAll() {
        return plans.values().stream().map(InMemoryStudyPlanRepository::copyOf).toList();
    }

    @Override
    public List<StudyPlan> findBy(StudyPlanQuery query, StudyPlanAnalysisService analysisService, LocalDate currentDate) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(analysisService, "analysisService");
        Objects.requireNonNull(currentDate, "currentDate");
        return plans.values().stream()
                .filter(plan -> query.matches(plan, analysisService, currentDate))
                .map(InMemoryStudyPlanRepository::copyOf)
                .toList();
    }

    @Override
    public boolean deleteById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return plans.remove(id) != null;
    }

    private static StudyPlan copyOf(StudyPlan source) {
        Objects.requireNonNull(source, "source");
        return new StudyPlan(
                source.getId(),
                source.getGoalName(),
                source.getPeriod(),
                source.getExpectedHours(),
                source.getProgress());
    }
}
