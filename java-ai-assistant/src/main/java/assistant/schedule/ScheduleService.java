package assistant.schedule;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.testability.IdGenerator;
import assistant.testability.TimeProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class ScheduleService {
    private final ScheduleRepository repository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final ScheduleConflictPolicy conflictPolicy;

    public ScheduleService(
            ScheduleRepository repository,
            IdGenerator idGenerator,
            TimeProvider timeProvider,
            ScheduleConflictPolicy conflictPolicy) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider");
        this.conflictPolicy = Objects.requireNonNull(conflictPolicy, "conflictPolicy");
    }

    public OperationResult<ScheduleView> createSchedule(
            String name, DateTimeRange timeRange, String location, String note) {
        ScheduleItem candidate;
        try {
            candidate = ScheduleItem.create(idGenerator.nextId(), name, timeRange, location, note);
        } catch (NullPointerException | IllegalArgumentException exception) {
            return validationFailure(exception.getMessage());
        }

        return conflictPolicy.findFirstConflict(candidate, repository.findAll())
                .map(this::conflictFailure)
                .orElseGet(() -> {
                    repository.save(candidate);
                    return OperationResult.success(toView(candidate, timeProvider.now()));
                });
    }

    public OperationResult<ScheduleView> getSchedule(EntityId id) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id)
                .map(schedule -> OperationResult.success(toView(schedule, timeProvider.now())))
                .orElseGet(() -> notFound(id));
    }

    public OperationResult<List<ScheduleView>> listSchedules() {
        LocalDateTime currentDateTime = timeProvider.now();
        return OperationResult.success(toUnmodifiableViews(repository.findAll(), currentDateTime));
    }

    public OperationResult<List<ScheduleView>> listSchedules(ScheduleQuery query) {
        if (query == null) {
            return validationFailureList("query must not be null");
        }
        LocalDateTime currentDateTime = timeProvider.now();
        return OperationResult.success(toUnmodifiableViews(repository.findBy(query, currentDateTime), currentDateTime));
    }

    public OperationResult<List<ScheduleView>> listSchedulesByDate(LocalDate date) {
        if (date == null) {
            return validationFailureList("date must not be null");
        }
        return listSchedules(ScheduleQuery.byDate(date));
    }

    public OperationResult<ScheduleView> updateSchedule(
            EntityId id, String name, DateTimeRange timeRange, String location, String note) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id).map(existing -> {
            ScheduleItem candidate;
            try {
                candidate = ScheduleItem.create(id, name, timeRange, location, note);
            } catch (NullPointerException | IllegalArgumentException exception) {
                return validationFailure(exception.getMessage());
            }

            return conflictPolicy.findFirstConflict(candidate, schedulesExcept(id))
                    .map(this::conflictFailure)
                    .orElseGet(() -> {
                        existing.updateDetails(
                                candidate.getName(),
                                candidate.getTimeRange(),
                                candidate.getLocation(),
                                candidate.getNote());
                        repository.save(existing);
                        return OperationResult.success(toView(existing, timeProvider.now()));
                    });
        }).orElseGet(() -> notFound(id));
    }

    public OperationResult<Void> deleteSchedule(EntityId id) {
        if (id == null) {
            return validationFailureVoid("id must not be null");
        }
        if (!repository.deleteById(id)) {
            return notFoundVoid(id);
        }
        return OperationResult.success();
    }

    private OperationResult<ScheduleView> validationFailure(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<List<ScheduleView>> validationFailureList(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<Void> validationFailureVoid(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<ScheduleView> notFound(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "schedule not found: " + id.value());
    }

    private OperationResult<Void> notFoundVoid(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "schedule not found: " + id.value());
    }

    private OperationResult<ScheduleView> conflictFailure(ScheduleItem conflict) {
        return OperationResult.failure(ErrorCode.SCHEDULE_CONFLICT, "schedule conflict: " + conflict.getId().value());
    }

    private ScheduleView toView(ScheduleItem schedule, LocalDateTime currentDateTime) {
        return ScheduleView.from(schedule, currentDateTime);
    }

    private List<ScheduleView> toUnmodifiableViews(List<ScheduleItem> schedules, LocalDateTime currentDateTime) {
        return schedules.stream().map(schedule -> toView(schedule, currentDateTime)).toList();
    }

    private List<ScheduleItem> schedulesExcept(EntityId id) {
        return repository.findAll().stream()
                .filter(schedule -> !schedule.getId().equals(id))
                .toList();
    }
}
