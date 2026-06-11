package assistant.schedule;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.testability.FixedTimeProvider;
import assistant.testability.IdGenerator;
import assistant.testability.IncrementalIdGenerator;
import assistant.testability.TimeProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {
    private static final LocalDate JUNE_11 = LocalDate.of(2026, 6, 11);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 11, 9, 30);

    @Test
    void constructorRejectsNullDependencies() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        IdGenerator idGenerator = new IncrementalIdGenerator(100);
        TimeProvider timeProvider = new FixedTimeProvider(NOW);
        ScheduleConflictPolicy conflictPolicy = new ScheduleConflictPolicy();

        assertThrows(NullPointerException.class, () -> new ScheduleService(null, idGenerator, timeProvider, conflictPolicy));
        assertThrows(NullPointerException.class, () -> new ScheduleService(repository, null, timeProvider, conflictPolicy));
        assertThrows(NullPointerException.class, () -> new ScheduleService(repository, idGenerator, null, conflictPolicy));
        assertThrows(NullPointerException.class, () -> new ScheduleService(repository, idGenerator, timeProvider, null));
    }

    @Test
    void createScheduleStoresScheduleAndReturnsScheduleView() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleService service = newService(repository, 100, NOW);
        DateTimeRange timeRange = range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        OperationResult<ScheduleView> result =
                service.createSchedule("Team meeting", timeRange, "Room 301", "Discuss plan");

        assertSuccess(result);
        ScheduleView view = result.getPayload();
        assertAll(
                () -> assertInstanceOf(ScheduleView.class, view),
                () -> assertEquals(new EntityId(100), view.id()),
                () -> assertEquals("Team meeting", view.name()),
                () -> assertEquals(timeRange, view.timeRange()),
                () -> assertEquals(timeRange.startDateTime(), view.startDateTime()),
                () -> assertEquals(timeRange.endDateTime(), view.endDateTime()),
                () -> assertEquals("Room 301", view.location()),
                () -> assertEquals("Discuss plan", view.note()),
                () -> assertEquals(ScheduleStatus.UPCOMING, view.status()),
                () -> assertEquals(1, repository.findAll().size()));
    }

    @Test
    void createScheduleNormalizesEntityTextInReturnedView() {
        ScheduleService service = newService(100);

        OperationResult<ScheduleView> result = service.createSchedule(
                "  Team meeting\t",
                range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0),
                "\tRoom 301 ",
                " Discuss plan\n");

        assertSuccess(result);
        assertAll(
                () -> assertEquals("Team meeting", result.getPayload().name()),
                () -> assertEquals("Room 301", result.getPayload().location()),
                () -> assertEquals("Discuss plan", result.getPayload().note()));
    }

    @Test
    void createScheduleAllowsNullLocationAndNoteAsEmptyStrings() {
        ScheduleService service = newService(100);

        OperationResult<ScheduleView> result =
                service.createSchedule("Team meeting", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), null, null);

        assertSuccess(result);
        assertEquals("", result.getPayload().location());
        assertEquals("", result.getPayload().note());
        assertEquals("", service.getSchedule(new EntityId(100)).getPayload().location());
        assertEquals("", service.getSchedule(new EntityId(100)).getPayload().note());
    }

    @Test
    void createScheduleReturnsValidationErrorForInvalidFieldsAndDoesNotStore() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleService service = newService(repository, 100, NOW);

        assertFailure(
                service.createSchedule(" \t\n", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room", "Note"),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(service.createSchedule("Team meeting", null, "Room", "Note"), ErrorCode.VALIDATION_ERROR);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createScheduleRejectsOverlappingScheduleAndKeepsRepositoryUnchanged() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleService service = newService(repository, 100, NOW);
        service.createSchedule("First", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 1", "Note 1");

        OperationResult<ScheduleView> result =
                service.createSchedule("Second", range(2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30), "Room 2", "Note 2");

        assertFailure(result, ErrorCode.SCHEDULE_CONFLICT);
        assertEquals(List.of(new EntityId(100)), idsOfItems(repository.findAll()));
    }

    @Test
    void createScheduleAllowsTouchingTimeRanges() {
        ScheduleService service = newService(100);
        assertSuccess(service.createSchedule("First", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 1", "Note 1"));

        OperationResult<ScheduleView> result =
                service.createSchedule("Second", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room 2", "Note 2");

        assertSuccess(result);
        assertEquals(List.of(new EntityId(100), new EntityId(101)), idsOf(service.listSchedules().getPayload()));
    }

    @Test
    void getScheduleReturnsViewForExistingSchedule() {
        ScheduleService service = newService(100);
        service.createSchedule("Team meeting", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room 301", "Note");

        OperationResult<ScheduleView> result = service.getSchedule(new EntityId(100));

        assertSuccess(result);
        assertInstanceOf(ScheduleView.class, result.getPayload());
        assertEquals("Team meeting", result.getPayload().name());
    }

    @Test
    void getScheduleReturnsNotFoundForMissingSchedule() {
        ScheduleService service = newService(100);

        OperationResult<ScheduleView> result = service.getSchedule(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void getScheduleRejectsNullId() {
        ScheduleService service = newService(100);

        OperationResult<ScheduleView> result = service.getSchedule(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void listSchedulesReturnsUnmodifiableViewsInInsertionOrder() {
        ScheduleService service = serviceWithMixedSchedules();

        OperationResult<List<ScheduleView>> result = service.listSchedules();

        assertSuccess(result);
        List<ScheduleView> views = result.getPayload();
        assertAll(
                () -> assertEquals(List.of(new EntityId(100), new EntityId(101), new EntityId(102)), idsOf(views)),
                () -> assertInstanceOf(ScheduleView.class, views.get(0)),
                () -> assertThrows(UnsupportedOperationException.class, () -> views.clear()));
    }

    @Test
    void listSchedulesComputesStatusesWithInjectedTimeProvider() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        CountingTimeProvider timeProvider = new CountingTimeProvider(NOW);
        ScheduleService service = new ScheduleService(
                repository, new IncrementalIdGenerator(100), timeProvider, new ScheduleConflictPolicy());
        repository.save(schedule(100, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0));
        repository.save(schedule(101, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0));
        repository.save(schedule(102, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0));

        OperationResult<List<ScheduleView>> result = service.listSchedules();

        assertSuccess(result);
        assertEquals(List.of(ScheduleStatus.EXPIRED, ScheduleStatus.ONGOING, ScheduleStatus.UPCOMING),
                statusesOf(result.getPayload()));
        assertEquals(1, timeProvider.nowCalls());
    }

    @Test
    void listSchedulesWithQueryFiltersByDateStatusAndCombination() {
        ScheduleService service = serviceWithMixedSchedules();

        OperationResult<List<ScheduleView>> byDate = service.listSchedules(ScheduleQuery.byDate(JUNE_11));
        OperationResult<List<ScheduleView>> byStatus = service.listSchedules(ScheduleQuery.byStatus(ScheduleStatus.ONGOING));
        OperationResult<List<ScheduleView>> combined =
                service.listSchedules(ScheduleQuery.of(JUNE_11, ScheduleStatus.UPCOMING));

        assertSuccess(byDate);
        assertSuccess(byStatus);
        assertSuccess(combined);
        assertEquals(List.of(new EntityId(100), new EntityId(101), new EntityId(102)), idsOf(byDate.getPayload()));
        assertEquals(List.of(new EntityId(101)), idsOf(byStatus.getPayload()));
        assertEquals(List.of(new EntityId(102)), idsOf(combined.getPayload()));
    }

    @Test
    void listSchedulesWithQueryUsesOneCurrentTimeForFilteringAndProjection() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        CountingTimeProvider timeProvider = new CountingTimeProvider(NOW);
        ScheduleService service = new ScheduleService(
                repository, new IncrementalIdGenerator(100), timeProvider, new ScheduleConflictPolicy());
        repository.save(schedule(100, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0));
        repository.save(schedule(101, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0));

        OperationResult<List<ScheduleView>> result = service.listSchedules(ScheduleQuery.byStatus(ScheduleStatus.ONGOING));

        assertSuccess(result);
        assertEquals(List.of(new EntityId(100)), idsOf(result.getPayload()));
        assertEquals(List.of(ScheduleStatus.ONGOING), statusesOf(result.getPayload()));
        assertEquals(1, timeProvider.nowCalls());
    }

    @Test
    void listSchedulesRejectsNullQuery() {
        ScheduleService service = newService(100);

        OperationResult<List<ScheduleView>> result = service.listSchedules(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void listSchedulesByDateReturnsSchedulesCoveringDate() {
        ScheduleService service = newService(100);
        service.createSchedule("Morning", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 1", "Note 1");
        service.createSchedule("Tomorrow", range(2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0), "Room 2", "Note 2");

        OperationResult<List<ScheduleView>> result = service.listSchedulesByDate(JUNE_11);

        assertSuccess(result);
        assertEquals(List.of(new EntityId(100)), idsOf(result.getPayload()));
    }

    @Test
    void listSchedulesByDateIncludesCrossDateSchedule() {
        ScheduleService service = newService(100);
        service.createSchedule("Night work", range(2026, 6, 10, 22, 0, 2026, 6, 12, 1, 0), "Lab", "Build");

        OperationResult<List<ScheduleView>> result = service.listSchedulesByDate(JUNE_11);

        assertSuccess(result);
        assertEquals(List.of(new EntityId(100)), idsOf(result.getPayload()));
    }

    @Test
    void listSchedulesByDateExcludesExclusiveMidnightEndBoundary() {
        ScheduleService service = newService(100);
        service.createSchedule("Night work", range(2026, 6, 10, 22, 0, 2026, 6, 11, 0, 0), "Lab", "Build");

        OperationResult<List<ScheduleView>> result = service.listSchedulesByDate(JUNE_11);

        assertSuccess(result);
        assertTrue(result.getPayload().isEmpty());
    }

    @Test
    void listSchedulesByDateRejectsNullDate() {
        ScheduleService service = newService(100);

        OperationResult<List<ScheduleView>> result = service.listSchedulesByDate(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void updateScheduleChangesEditableFieldsAndPersists() {
        ScheduleService service = newService(100);
        service.createSchedule("Team meeting", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 301", "Note");
        DateTimeRange newRange = range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        OperationResult<ScheduleView> result =
                service.updateSchedule(new EntityId(100), "Code review", newRange, "Room 302", "Review changes");

        assertSuccess(result);
        assertAll(
                () -> assertEquals("Code review", result.getPayload().name()),
                () -> assertEquals(newRange, result.getPayload().timeRange()),
                () -> assertEquals("Room 302", result.getPayload().location()),
                () -> assertEquals("Review changes", result.getPayload().note()));
        assertSameScheduleView(result.getPayload(), service.getSchedule(new EntityId(100)).getPayload());
    }

    @Test
    void updateScheduleAllowsNullLocationAndNoteAsEmptyStrings() {
        ScheduleService service = newService(100);
        service.createSchedule("Team meeting", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 301", "Note");

        OperationResult<ScheduleView> result = service.updateSchedule(
                new EntityId(100), "Code review", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), null, null);

        assertSuccess(result);
        assertEquals("", result.getPayload().location());
        assertEquals("", result.getPayload().note());
        assertEquals("", service.getSchedule(new EntityId(100)).getPayload().location());
        assertEquals("", service.getSchedule(new EntityId(100)).getPayload().note());
    }

    @Test
    void updateSchedulePersistsChangesWhenRepositoryReturnsDetachedCopies() {
        CopyingScheduleRepository repository = new CopyingScheduleRepository();
        EntityId id = new EntityId(100);
        repository.save(schedule(100, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0));
        ScheduleService service = new ScheduleService(
                repository, new IncrementalIdGenerator(200), new FixedTimeProvider(NOW), new ScheduleConflictPolicy());

        OperationResult<ScheduleView> result = service.updateSchedule(
                id,
                "Code review",
                range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0),
                "Room 302",
                "Review changes");

        assertSuccess(result);
        ScheduleView stored = service.getSchedule(id).getPayload();
        assertAll(
                () -> assertEquals("Code review", stored.name()),
                () -> assertEquals(range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), stored.timeRange()),
                () -> assertEquals("Room 302", stored.location()),
                () -> assertEquals("Review changes", stored.note()));
    }

    @Test
    void updateScheduleReturnsNotFoundForMissingSchedule() {
        ScheduleService service = newService(100);

        OperationResult<ScheduleView> result = service.updateSchedule(
                new EntityId(999), "Code review", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room", "Note");

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void updateScheduleRejectsNullId() {
        ScheduleService service = newService(100);

        OperationResult<ScheduleView> result =
                service.updateSchedule(null, "Code review", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room", "Note");

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void updateScheduleRejectsInvalidFieldsAndKeepsStoredScheduleUnchanged() {
        ScheduleService service = newService(100);
        service.createSchedule("Team meeting", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 301", "Note");
        ScheduleView before = service.getSchedule(new EntityId(100)).getPayload();

        assertFailure(
                service.updateSchedule(
                        new EntityId(100), " \t\n", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room", "Note"),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(service.updateSchedule(new EntityId(100), "Code review", null, "Room", "Note"),
                ErrorCode.VALIDATION_ERROR);

        assertSameScheduleView(before, service.getSchedule(new EntityId(100)).getPayload());
    }

    @Test
    void updateScheduleRejectsOverlappingOtherScheduleAndKeepsStoredScheduleUnchanged() {
        ScheduleService service = newService(100);
        service.createSchedule("First", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 1", "Note 1");
        service.createSchedule("Second", range(2026, 6, 11, 11, 0, 2026, 6, 11, 12, 0), "Room 2", "Note 2");
        ScheduleView before = service.getSchedule(new EntityId(101)).getPayload();

        OperationResult<ScheduleView> result = service.updateSchedule(
                new EntityId(101), "Second changed", range(2026, 6, 11, 9, 30, 2026, 6, 11, 10, 30), "Room 3", "Note 3");

        assertFailure(result, ErrorCode.SCHEDULE_CONFLICT);
        assertSameScheduleView(before, service.getSchedule(new EntityId(101)).getPayload());
    }

    @Test
    void updateScheduleExcludesCurrentScheduleWhenCheckingConflict() {
        ScheduleService service = newService(100);
        DateTimeRange timeRange = range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        service.createSchedule("Team meeting", timeRange, "Room 301", "Note");

        OperationResult<ScheduleView> result =
                service.updateSchedule(new EntityId(100), "Team meeting", timeRange, "Room 301", "Changed note");

        assertSuccess(result);
        assertEquals("Changed note", result.getPayload().note());
    }

    @Test
    void updateScheduleAllowsTouchingOtherScheduleTimeRange() {
        ScheduleService service = newService(100);
        service.createSchedule("First", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 1", "Note 1");
        service.createSchedule("Second", range(2026, 6, 11, 11, 0, 2026, 6, 11, 12, 0), "Room 2", "Note 2");

        OperationResult<ScheduleView> result = service.updateSchedule(
                new EntityId(101), "Second", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room 2", "Note 2");

        assertSuccess(result);
        assertEquals(range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), result.getPayload().timeRange());
    }

    @Test
    void deleteScheduleRemovesExistingSchedule() {
        ScheduleService service = newService(100);
        service.createSchedule("Team meeting", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room", "Note");

        OperationResult<Void> result = service.deleteSchedule(new EntityId(100));

        assertSuccess(result);
        assertFailure(service.getSchedule(new EntityId(100)), ErrorCode.NOT_FOUND);
    }

    @Test
    void deleteScheduleReturnsNotFoundForMissingSchedule() {
        ScheduleService service = newService(100);

        OperationResult<Void> result = service.deleteSchedule(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void deleteScheduleRejectsNullId() {
        ScheduleService service = newService(100);

        OperationResult<Void> result = service.deleteSchedule(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void deleteScheduleRemovesScheduleFromLaterQueries() {
        ScheduleService service = newService(100);
        service.createSchedule("Ongoing", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 1", "Note 1");
        service.createSchedule("Upcoming", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room 2", "Note 2");

        assertSuccess(service.deleteSchedule(new EntityId(100)));

        assertEquals(List.of(new EntityId(101)), idsOf(service.listSchedules().getPayload()));
        assertEquals(List.of(new EntityId(101)), idsOf(service.listSchedulesByDate(JUNE_11).getPayload()));
        assertTrue(service.listSchedules(ScheduleQuery.byStatus(ScheduleStatus.ONGOING)).getPayload().isEmpty());
    }

    @Test
    void returnedScheduleViewDoesNotChangeWhenStoredScheduleIsUpdatedLater() {
        ScheduleService service = newService(100);
        ScheduleView original = service.createSchedule(
                "Team meeting", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 301", "Note").getPayload();

        service.updateSchedule(
                new EntityId(100), "Code review", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room 302", "New note");

        assertAll(
                () -> assertEquals("Team meeting", original.name()),
                () -> assertEquals(range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), original.timeRange()),
                () -> assertEquals("Room 301", original.location()),
                () -> assertEquals("Note", original.note()),
                () -> assertEquals(ScheduleStatus.ONGOING, original.status()));
    }

    @Test
    void returnedListSnapshotDoesNotChangeWhenStoredSchedulesChangeLater() {
        ScheduleService service = newService(100);
        service.createSchedule("Team meeting", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 301", "Note");

        List<ScheduleView> originalList = service.listSchedules().getPayload();
        ScheduleView originalView = originalList.get(0);

        service.updateSchedule(
                new EntityId(100), "Code review", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room 302", "New note");
        service.createSchedule("Planning", range(2026, 6, 11, 11, 0, 2026, 6, 11, 12, 0), "Room 303", "Plan");

        assertAll(
                () -> assertEquals(1, originalList.size()),
                () -> assertEquals("Team meeting", originalView.name()),
                () -> assertEquals(range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), originalView.timeRange()),
                () -> assertEquals("Room 301", originalView.location()),
                () -> assertEquals("Note", originalView.note()),
                () -> assertEquals(ScheduleStatus.ONGOING, originalView.status()));
    }

    @Test
    void returnedListCannotModifyServiceStorage() {
        ScheduleService service = newService(100);
        service.createSchedule("Team meeting", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 301", "Note");

        List<ScheduleView> views = service.listSchedules().getPayload();

        assertThrows(UnsupportedOperationException.class, () -> views.add(new ScheduleView(
                new EntityId(999),
                "External",
                range(2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0),
                LocalDateTime.of(2026, 6, 12, 9, 0),
                LocalDateTime.of(2026, 6, 12, 10, 0),
                "Outside",
                "Mutation",
                ScheduleStatus.UPCOMING)));
        assertEquals(1, service.listSchedules().getPayload().size());
    }

    @Test
    void filteredResultsAreScheduleViewsAndDoNotExposeScheduleItems() {
        ScheduleService service = serviceWithMixedSchedules();

        OperationResult<List<ScheduleView>> result =
                service.listSchedules(ScheduleQuery.of(JUNE_11, ScheduleStatus.ONGOING));

        assertSuccess(result);
        assertThrows(UnsupportedOperationException.class, () -> result.getPayload().clear());
        assertInstanceOf(ScheduleView.class, result.getPayload().get(0));
    }

    private static ScheduleService newService(long startInclusive) {
        return newService(new InMemoryScheduleRepository(), startInclusive, NOW);
    }

    private static ScheduleService newService(
            InMemoryScheduleRepository repository, long startInclusive, LocalDateTime currentDateTime) {
        return new ScheduleService(
                repository,
                new IncrementalIdGenerator(startInclusive),
                new FixedTimeProvider(currentDateTime),
                new ScheduleConflictPolicy());
    }

    private static ScheduleService serviceWithMixedSchedules() {
        ScheduleService service = newService(100);
        service.createSchedule("Expired", range(2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0), "Room 1", "Note 1");
        service.createSchedule("Ongoing", range(2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0), "Room 2", "Note 2");
        service.createSchedule("Upcoming", range(2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0), "Room 3", "Note 3");
        return service;
    }

    private static ScheduleItem schedule(
            long id,
            int startYear,
            int startMonth,
            int startDay,
            int startHour,
            int startMinute,
            int endYear,
            int endMonth,
            int endDay,
            int endHour,
            int endMinute) {
        return new ScheduleItem(
                new EntityId(id),
                "Schedule " + id,
                range(startYear, startMonth, startDay, startHour, startMinute, endYear, endMonth, endDay, endHour, endMinute),
                "Room " + id,
                "Note " + id);
    }

    private static DateTimeRange range(
            int startYear,
            int startMonth,
            int startDay,
            int startHour,
            int startMinute,
            int endYear,
            int endMonth,
            int endDay,
            int endHour,
            int endMinute) {
        return new DateTimeRange(
                LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute),
                LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute));
    }

    private static List<EntityId> idsOf(List<ScheduleView> views) {
        return views.stream().map(ScheduleView::id).toList();
    }

    private static List<EntityId> idsOfItems(List<ScheduleItem> schedules) {
        return schedules.stream().map(ScheduleItem::getId).toList();
    }

    private static List<ScheduleStatus> statusesOf(List<ScheduleView> views) {
        return views.stream().map(ScheduleView::status).toList();
    }

    private static void assertSameScheduleView(ScheduleView expected, ScheduleView actual) {
        assertAll(
                () -> assertEquals(expected.id(), actual.id()),
                () -> assertEquals(expected.name(), actual.name()),
                () -> assertEquals(expected.timeRange(), actual.timeRange()),
                () -> assertEquals(expected.startDateTime(), actual.startDateTime()),
                () -> assertEquals(expected.endDateTime(), actual.endDateTime()),
                () -> assertEquals(expected.location(), actual.location()),
                () -> assertEquals(expected.note(), actual.note()),
                () -> assertEquals(expected.status(), actual.status()));
    }

    private static <T> void assertSuccess(OperationResult<T> result) {
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
    }

    private static <T> void assertFailure(OperationResult<T> result, ErrorCode errorCode) {
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals(errorCode, result.getErrorCode());
        assertFalse(result.getMessage().isBlank());
    }

    private static final class CountingTimeProvider implements TimeProvider {
        private final LocalDateTime fixedDateTime;
        private int nowCalls;

        private CountingTimeProvider(LocalDateTime fixedDateTime) {
            this.fixedDateTime = Objects.requireNonNull(fixedDateTime, "fixedDateTime");
        }

        @Override
        public LocalDate today() {
            return fixedDateTime.toLocalDate();
        }

        @Override
        public LocalDateTime now() {
            nowCalls++;
            return fixedDateTime;
        }

        private int nowCalls() {
            return nowCalls;
        }
    }

    private static final class CopyingScheduleRepository implements ScheduleRepository {
        private final Map<EntityId, ScheduleItem> schedules = new LinkedHashMap<>();

        @Override
        public void save(ScheduleItem schedule) {
            Objects.requireNonNull(schedule, "schedule");
            schedules.put(schedule.getId(), copyOf(schedule));
        }

        @Override
        public Optional<ScheduleItem> findById(EntityId id) {
            Objects.requireNonNull(id, "id");
            ScheduleItem schedule = schedules.get(id);
            return schedule == null ? Optional.empty() : Optional.of(copyOf(schedule));
        }

        @Override
        public List<ScheduleItem> findAll() {
            return schedules.values().stream()
                    .map(CopyingScheduleRepository::copyOf)
                    .toList();
        }

        @Override
        public List<ScheduleItem> findBy(ScheduleQuery query, LocalDateTime currentDateTime) {
            Objects.requireNonNull(query, "query");
            Objects.requireNonNull(currentDateTime, "currentDateTime");
            return schedules.values().stream()
                    .filter(schedule -> query.matches(schedule, currentDateTime))
                    .map(CopyingScheduleRepository::copyOf)
                    .toList();
        }

        @Override
        public boolean deleteById(EntityId id) {
            Objects.requireNonNull(id, "id");
            return schedules.remove(id) != null;
        }

        private static ScheduleItem copyOf(ScheduleItem schedule) {
            return new ScheduleItem(
                    schedule.getId(),
                    schedule.getName(),
                    schedule.getTimeRange(),
                    schedule.getLocation(),
                    schedule.getNote());
        }
    }
}
