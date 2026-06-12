package assistant.study;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.testability.IdGenerator;
import assistant.testability.TimeProvider;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class StudyPlanService {
    private final StudyPlanRepository repository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final StudyPlanAnalysisService analysisService;

    public StudyPlanService(
            StudyPlanRepository repository,
            IdGenerator idGenerator,
            TimeProvider timeProvider,
            StudyPlanAnalysisService analysisService) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider");
        this.analysisService = Objects.requireNonNull(analysisService, "analysisService");
    }

    public OperationResult<StudyPlanView> createStudyPlan(
            String goalName, LocalDate startDate, LocalDate endDate, int expectedHours) {
        return createStudyPlan(goalName, startDate, endDate, expectedHours, 0);
    }

    public OperationResult<StudyPlanView> createStudyPlan(
            String goalName, LocalDate startDate, LocalDate endDate, int expectedHours, int initialProgress) {
        StudyPlan plan;
        try {
            plan = StudyPlan.create(
                    idGenerator.nextId(),
                    goalName,
                    toDateRange(startDate, endDate),
                    expectedHours,
                    toProgress(initialProgress));
        } catch (NullPointerException | IllegalArgumentException exception) {
            return validationFailure(exception.getMessage());
        }

        repository.save(plan);
        LocalDate currentDate = timeProvider.today();
        return OperationResult.success(toView(plan, currentDate));
    }

    public OperationResult<StudyPlanView> getStudyPlan(EntityId id) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        LocalDate currentDate = timeProvider.today();
        return repository.findById(id)
                .map(plan -> OperationResult.success(toView(plan, currentDate)))
                .orElseGet(() -> notFound(id));
    }

    public OperationResult<List<StudyPlanView>> listStudyPlans() {
        LocalDate currentDate = timeProvider.today();
        return OperationResult.success(toUnmodifiableViews(repository.findAll(), currentDate));
    }

    public OperationResult<List<StudyPlanView>> listStudyPlans(StudyPlanQuery query) {
        if (query == null) {
            return validationFailureList("query must not be null");
        }
        LocalDate currentDate = timeProvider.today();
        return OperationResult.success(toUnmodifiableViews(
                repository.findBy(query, analysisService, currentDate), currentDate));
    }

    public OperationResult<StudyPlanView> updateStudyPlanDetails(
            EntityId id, String goalName, LocalDate startDate, LocalDate endDate, int expectedHours) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id).map(plan -> {
            try {
                plan.updateDetails(goalName, toDateRange(startDate, endDate), expectedHours);
            } catch (NullPointerException | IllegalArgumentException exception) {
                return validationFailure(exception.getMessage());
            }
            repository.save(plan);
            LocalDate currentDate = timeProvider.today();
            return OperationResult.success(toView(plan, currentDate));
        }).orElseGet(() -> notFound(id));
    }

    public OperationResult<StudyPlanView> updateStudyPlanProgress(EntityId id, int progressValue) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id).map(plan -> {
            try {
                plan.updateProgress(toProgress(progressValue));
            } catch (NullPointerException | IllegalArgumentException exception) {
                return validationFailure(exception.getMessage());
            }
            repository.save(plan);
            LocalDate currentDate = timeProvider.today();
            return OperationResult.success(toView(plan, currentDate));
        }).orElseGet(() -> notFound(id));
    }

    public OperationResult<Void> deleteStudyPlan(EntityId id) {
        if (id == null) {
            return validationFailureVoid("id must not be null");
        }
        if (!repository.deleteById(id)) {
            return notFoundVoid(id);
        }
        return OperationResult.success();
    }

    public OperationResult<Integer> countCompletedPlans() {
        LocalDate currentDate = timeProvider.today();
        int count = (int) repository.findAll().stream()
                .filter(plan -> analysisService.analyzeStatus(plan, currentDate) == StudyPlanStatus.COMPLETED)
                .count();
        return OperationResult.success(count);
    }

    public OperationResult<Integer> countIncompletePlans() {
        LocalDate currentDate = timeProvider.today();
        int count = (int) repository.findAll().stream()
                .filter(plan -> analysisService.analyzeStatus(plan, currentDate) != StudyPlanStatus.COMPLETED)
                .count();
        return OperationResult.success(count);
    }

    private OperationResult<StudyPlanView> validationFailure(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<List<StudyPlanView>> validationFailureList(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<Void> validationFailureVoid(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<Integer> validationFailureCount(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<StudyPlanView> notFound(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "study plan not found: " + id.value());
    }

    private OperationResult<Void> notFoundVoid(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "study plan not found: " + id.value());
    }

    private StudyPlanView toView(StudyPlan plan, LocalDate currentDate) {
        return StudyPlanView.from(plan, analysisService, currentDate);
    }

    private List<StudyPlanView> toUnmodifiableViews(List<StudyPlan> plans, LocalDate currentDate) {
        return plans.stream().map(plan -> toView(plan, currentDate)).toList();
    }

    private DateRange toDateRange(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    private Progress toProgress(int progressValue) {
        if (progressValue == 0) {
            return Progress.zero();
        }
        if (progressValue == 100) {
            return Progress.complete();
        }
        return Progress.of(progressValue);
    }
}
