package assistant.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.task.TaskService;
import assistant.testability.FixedTimeProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DemoDataFactoryTest {
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 1, 15, 9, 0);

    @Test
    void loadCreatesVisibleDemoDataThroughServices() {
        ApplicationServices services = services();

        new DemoDataFactory().load(services);

        assertAll(
                () -> assertFalse(services.taskService().listTasks().getPayload().isEmpty()),
                () -> assertFalse(services.scheduleService().listSchedules().getPayload().isEmpty()),
                () -> assertFalse(services.studyPlanService().listStudyPlans().getPayload().isEmpty()),
                () -> assertFalse(services.financeService().listTransactions().getPayload().isEmpty()),
                () -> assertFalse(services.noteService().listNotes().getPayload().isEmpty()),
                () -> assertTrue(services.summaryService().getDashboardSummary().isSuccess()),
                () -> assertFalse(services.summaryService()
                        .getDashboardSummary()
                        .getPayload()
                        .noteTagDistribution()
                        .isEmpty()));
    }

    @Test
    void loadUsesFixedTimeProviderForRelativeDates() {
        ApplicationServices services = services();
        LocalDate today = FIXED_NOW.toLocalDate();

        new DemoDataFactory().load(services);

        assertAll(
                () -> assertTrue(services.taskService().listTasks().getPayload().stream()
                        .anyMatch(task -> task.dueDate().equals(today))),
                () -> assertTrue(services.scheduleService().listSchedules().getPayload().stream()
                        .allMatch(schedule -> schedule.startDateTime().toLocalDate().equals(today))),
                () -> assertTrue(services.financeService().listTransactions().getPayload().stream()
                        .allMatch(transaction -> transaction.date().getMonth().equals(today.getMonth()))));
    }

    @Test
    void loadRejectsNullServices() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new DemoDataFactory().load(null));

        assertEquals("services", exception.getMessage());
    }

    @Test
    void loadPropagatesServiceFailureAsIllegalStateException() {
        ApplicationServices baseServices = services();
        TaskService failingTaskService = mock(TaskService.class);
        when(failingTaskService.createTask(anyString(), anyString(), any(), any()))
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "task rejected"));
        ApplicationServices services = new ApplicationServices(
                failingTaskService,
                baseServices.scheduleService(),
                baseServices.studyPlanService(),
                baseServices.financeService(),
                baseServices.noteService(),
                baseServices.summaryService(),
                baseServices.aiAssistantService(),
                baseServices.draftLifecycleService(),
                baseServices.timeProvider());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> new DemoDataFactory().load(services));

        assertAll(
                () -> assertTrue(exception.getMessage().contains("failed to load demo data")),
                () -> assertTrue(exception.getMessage().contains(ErrorCode.VALIDATION_ERROR.name())));
    }

    private static ApplicationServices services() {
        return new ApplicationFactory().create(Map.of(), new FixedTimeProvider(FIXED_NOW));
    }
}
