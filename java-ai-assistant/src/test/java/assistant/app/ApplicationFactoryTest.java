package assistant.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import assistant.ai.AiConfiguration;
import assistant.ai.AiConfigurationLoader;
import assistant.ai.AiScenario;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.testability.FixedTimeProvider;
import assistant.testability.TimeProvider;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ApplicationFactoryTest {
    private static final TimeProvider FIXED_TIME =
            new FixedTimeProvider(LocalDateTime.of(2026, 1, 15, 9, 0));

    @Test
    void createWithExplicitConfigurationBuildsAllServices() {
        ApplicationServices services = new ApplicationFactory().create(Map.of(), FIXED_TIME);

        assertAll(
                () -> assertNotNull(services.taskService()),
                () -> assertNotNull(services.scheduleService()),
                () -> assertNotNull(services.studyPlanService()),
                () -> assertNotNull(services.financeService()),
                () -> assertNotNull(services.noteService()),
                () -> assertNotNull(services.summaryService()),
                () -> assertNotNull(services.aiAssistantService()),
                () -> assertNotNull(services.draftLifecycleService()),
                () -> assertNotNull(services.timeProvider()),
                () -> assertTrue(services.summaryService().getDashboardSummary().isSuccess()));
    }

    @Test
    void createWithExplicitConfigurationDoesNotReadRealEnvironment() {
        ApplicationServices services = new ApplicationFactory().create(Map.of(), FIXED_TIME);

        OperationResult<String> result = services.aiAssistantService().ask(AiScenario.GENERAL_QA, "hello");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_NOT_CONFIGURED, result.getErrorCode()));
    }

    @Test
    void createUsesProvidedAiConfiguration() {
        AiConfigurationLoader loader = mock(AiConfigurationLoader.class);
        when(loader.load(Map.of(AiConfigurationLoader.API_KEY_NAME, "test-key")))
                .thenReturn(OperationResult.success(new AiConfiguration(
                        "https://",
                        "/chat",
                        "model",
                        "test-key",
                        Duration.ofSeconds(1))));
        ApplicationFactory factory = new ApplicationFactory(loader);

        ApplicationServices services = factory.create(Map.of(AiConfigurationLoader.API_KEY_NAME, "test-key"), FIXED_TIME);
        OperationResult<String> result = services.aiAssistantService().ask(AiScenario.GENERAL_QA, "hello");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()));
    }

    @Test
    void createReadsProductionConfigurationAndSystemPropertiesOverrideEnvironmentValues() {
        AiConfigurationLoader loader = mock(AiConfigurationLoader.class);
        when(loader.load(anyMap()))
                .thenReturn(OperationResult.failure(ErrorCode.AI_NOT_CONFIGURED, "missing API key"));
        ApplicationFactory factory = new ApplicationFactory(loader);
        String previousPathProperty = System.getProperty("PATH");
        String pathOverride = "system-property-path-override";

        try {
            System.setProperty("PATH", pathOverride);

            factory.create();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, String>> valuesCaptor = ArgumentCaptor.forClass(Map.class);
            org.mockito.Mockito.verify(loader).load(valuesCaptor.capture());
            Map<String, String> values = valuesCaptor.getValue();
            assertAll(
                    () -> assertEquals(pathOverride, values.get("PATH")),
                    () -> assertEquals(pathOverride, System.getProperty("PATH")));
        } finally {
            if (previousPathProperty == null) {
                System.clearProperty("PATH");
            } else {
                System.setProperty("PATH", previousPathProperty);
            }
        }
    }

    @Test
    void createRejectsNullConfigurationMap() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ApplicationFactory().create(null, FIXED_TIME));

        assertEquals("configurationValues", exception.getMessage());
    }

    @Test
    void createRejectsNullTimeProvider() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ApplicationFactory().create(Map.of(), null));

        assertEquals("timeProvider", exception.getMessage());
    }

    @Test
    void constructorRejectsNullLoader() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ApplicationFactory(null));

        assertEquals("aiConfigurationLoader", exception.getMessage());
    }

    @Test
    void applicationServicesRejectsNullComponents() {
        ApplicationServices services = new ApplicationFactory().create(Map.of(), FIXED_TIME);

        assertAll(
                () -> assertNullComponentRejected("taskService", () -> new ApplicationServices(
                        null,
                        services.scheduleService(),
                        services.studyPlanService(),
                        services.financeService(),
                        services.noteService(),
                        services.summaryService(),
                        services.aiAssistantService(),
                        services.draftLifecycleService(),
                        services.timeProvider())),
                () -> assertNullComponentRejected("scheduleService", () -> new ApplicationServices(
                        services.taskService(),
                        null,
                        services.studyPlanService(),
                        services.financeService(),
                        services.noteService(),
                        services.summaryService(),
                        services.aiAssistantService(),
                        services.draftLifecycleService(),
                        services.timeProvider())),
                () -> assertNullComponentRejected("studyPlanService", () -> new ApplicationServices(
                        services.taskService(),
                        services.scheduleService(),
                        null,
                        services.financeService(),
                        services.noteService(),
                        services.summaryService(),
                        services.aiAssistantService(),
                        services.draftLifecycleService(),
                        services.timeProvider())),
                () -> assertNullComponentRejected("financeService", () -> new ApplicationServices(
                        services.taskService(),
                        services.scheduleService(),
                        services.studyPlanService(),
                        null,
                        services.noteService(),
                        services.summaryService(),
                        services.aiAssistantService(),
                        services.draftLifecycleService(),
                        services.timeProvider())),
                () -> assertNullComponentRejected("noteService", () -> new ApplicationServices(
                        services.taskService(),
                        services.scheduleService(),
                        services.studyPlanService(),
                        services.financeService(),
                        null,
                        services.summaryService(),
                        services.aiAssistantService(),
                        services.draftLifecycleService(),
                        services.timeProvider())),
                () -> assertNullComponentRejected("summaryService", () -> new ApplicationServices(
                        services.taskService(),
                        services.scheduleService(),
                        services.studyPlanService(),
                        services.financeService(),
                        services.noteService(),
                        null,
                        services.aiAssistantService(),
                        services.draftLifecycleService(),
                        services.timeProvider())),
                () -> assertNullComponentRejected("aiAssistantService", () -> new ApplicationServices(
                        services.taskService(),
                        services.scheduleService(),
                        services.studyPlanService(),
                        services.financeService(),
                        services.noteService(),
                        services.summaryService(),
                        null,
                        services.draftLifecycleService(),
                        services.timeProvider())),
                () -> assertNullComponentRejected("draftLifecycleService", () -> new ApplicationServices(
                        services.taskService(),
                        services.scheduleService(),
                        services.studyPlanService(),
                        services.financeService(),
                        services.noteService(),
                        services.summaryService(),
                        services.aiAssistantService(),
                        null,
                        services.timeProvider())),
                () -> assertNullComponentRejected("timeProvider", () -> new ApplicationServices(
                        services.taskService(),
                        services.scheduleService(),
                        services.studyPlanService(),
                        services.financeService(),
                        services.noteService(),
                        services.summaryService(),
                        services.aiAssistantService(),
                        services.draftLifecycleService(),
                        null)));
    }

    private static void assertNullComponentRejected(String expectedMessage, Executable executable) {
        NullPointerException exception = assertThrows(NullPointerException.class, executable::execute);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private interface Executable {
        void execute();
    }

}
