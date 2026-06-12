package assistant.study;

import assistant.common.EntityId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository {
    void save(StudyPlan plan);

    Optional<StudyPlan> findById(EntityId id);

    List<StudyPlan> findAll();

    List<StudyPlan> findBy(StudyPlanQuery query, StudyPlanAnalysisService analysisService, LocalDate currentDate);

    boolean deleteById(EntityId id);
}
