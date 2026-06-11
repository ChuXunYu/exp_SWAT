package assistant.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryScheduleRepositoryTest {
    private static final LocalDate JUNE_11 = LocalDate.of(2026, 6, 11);
    private static final LocalDateTime CURRENT = LocalDateTime.of(2026, 6, 11, 9, 30);

    @Test
    void saveAndFindByIdReturnsStoredSchedule() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleItem schedule = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);

        repository.save(schedule);

        assertSame(schedule, repository.findById(new EntityId(1)).orElseThrow());
    }

    @Test
    void findByIdReturnsEmptyWhenScheduleDoesNotExist() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();

        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void saveReplacesScheduleWithSameId() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleItem original = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem replacement = schedule(1, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0);

        repository.save(original);
        repository.save(replacement);

        assertEquals(List.of(replacement), repository.findAll());
        assertSame(replacement, repository.findById(new EntityId(1)).orElseThrow());
    }

    @Test
    void replacingExistingScheduleKeepsOriginalInsertionPosition() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleItem original = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem second = schedule(2, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);
        ScheduleItem replacement = schedule(1, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0);

        repository.save(original);
        repository.save(second);
        repository.save(replacement);

        assertEquals(List.of(replacement, second), repository.findAll());
    }

    @Test
    void findAllReturnsSchedulesInInsertionOrder() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleItem first = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem second = schedule(2, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);

        repository.save(first);
        repository.save(second);

        assertEquals(List.of(first, second), repository.findAll());
    }

    @Test
    void findAllReturnsUnmodifiableSnapshotList() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleItem first = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem second = schedule(2, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0);
        repository.save(first);

        List<ScheduleItem> snapshot = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
        repository.save(second);
        assertEquals(1, snapshot.size());
    }

    @Test
    void findByFiltersByDate() {
        InMemoryScheduleRepository repository = repositoryWithMixedSchedules();

        List<ScheduleItem> result = repository.findBy(ScheduleQuery.byDate(JUNE_11), CURRENT);

        assertEquals(List.of(new EntityId(1), new EntityId(2), new EntityId(3)), idsOf(result));
    }

    @Test
    void findByFiltersByStatusAtProvidedCurrentTime() {
        InMemoryScheduleRepository repository = repositoryWithMixedSchedules();

        List<ScheduleItem> result = repository.findBy(ScheduleQuery.byStatus(ScheduleStatus.ONGOING), CURRENT);

        assertEquals(List.of(new EntityId(2)), idsOf(result));
    }

    @Test
    void findByAppliesCombinedQueryInInsertionOrder() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleItem first = schedule(1, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0);
        ScheduleItem second = schedule(2, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem third = schedule(3, 2026, 6, 11, 9, 15, 2026, 6, 11, 9, 45);
        ScheduleItem fourth = schedule(4, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0);
        repository.save(first);
        repository.save(second);
        repository.save(third);
        repository.save(fourth);

        List<ScheduleItem> result =
                repository.findBy(ScheduleQuery.of(JUNE_11, ScheduleStatus.ONGOING), CURRENT);

        assertEquals(List.of(second, third), result);
    }

    @Test
    void findByReturnsUnmodifiableSnapshotList() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        ScheduleItem first = schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0);
        ScheduleItem second = schedule(2, 2026, 6, 11, 9, 15, 2026, 6, 11, 9, 45);
        repository.save(first);

        List<ScheduleItem> snapshot = repository.findBy(ScheduleQuery.byStatus(ScheduleStatus.ONGOING), CURRENT);

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
        repository.save(second);
        assertEquals(1, snapshot.size());
    }

    @Test
    void deleteByIdRemovesExistingSchedule() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        repository.save(schedule(1, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0));

        assertTrue(repository.deleteById(new EntityId(1)));
        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void deleteByIdReturnsFalseWhenScheduleDoesNotExist() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();

        assertFalse(repository.deleteById(new EntityId(1)));
    }

    @Test
    void methodsRejectNullArguments() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();

        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.findBy(null, CURRENT));
        assertThrows(NullPointerException.class, () -> repository.findBy(ScheduleQuery.all(), null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
    }

    private static InMemoryScheduleRepository repositoryWithMixedSchedules() {
        InMemoryScheduleRepository repository = new InMemoryScheduleRepository();
        repository.save(schedule(1, 2026, 6, 11, 8, 0, 2026, 6, 11, 9, 0));
        repository.save(schedule(2, 2026, 6, 11, 9, 0, 2026, 6, 11, 10, 0));
        repository.save(schedule(3, 2026, 6, 11, 10, 0, 2026, 6, 11, 11, 0));
        repository.save(schedule(4, 2026, 6, 12, 9, 0, 2026, 6, 12, 10, 0));
        return repository;
    }

    private static List<EntityId> idsOf(List<ScheduleItem> schedules) {
        return schedules.stream().map(ScheduleItem::getId).toList();
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
                new DateTimeRange(
                        LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute),
                        LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute)),
                "Room " + id,
                "Note " + id);
    }
}
