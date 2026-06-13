package assistant.app;

import assistant.ai.AiAssistantService;
import assistant.ai.AiClient;
import assistant.ai.AiConfiguration;
import assistant.ai.AiConfigurationLoader;
import assistant.ai.ContextProvider;
import assistant.ai.DeepSeekAiClient;
import assistant.ai.DraftImportService;
import assistant.ai.DraftLifecycleService;
import assistant.ai.InMemorySuggestionDraftRepository;
import assistant.ai.JdkAiHttpTransport;
import assistant.ai.PromptBuilder;
import assistant.ai.StructuredSuggestionDraftService;
import assistant.ai.StructuredSuggestionParser;
import assistant.ai.SuggestionDraftRepository;
import assistant.common.OperationResult;
import assistant.finance.FinanceService;
import assistant.finance.FinanceStatisticsService;
import assistant.finance.InMemoryTransactionRepository;
import assistant.note.InMemoryNoteRepository;
import assistant.note.NoteSearchPolicy;
import assistant.note.NoteService;
import assistant.schedule.InMemoryScheduleRepository;
import assistant.schedule.ScheduleConflictPolicy;
import assistant.schedule.ScheduleService;
import assistant.study.InMemoryStudyPlanRepository;
import assistant.study.StudyPlanAnalysisService;
import assistant.study.StudyPlanService;
import assistant.summary.SummaryService;
import assistant.task.InMemoryTaskRepository;
import assistant.task.TaskService;
import assistant.testability.IdGenerator;
import assistant.testability.IncrementalIdGenerator;
import assistant.testability.SystemTimeProvider;
import assistant.testability.TimeProvider;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ApplicationFactory {
    private final AiConfigurationLoader aiConfigurationLoader;

    public ApplicationFactory() {
        this(new AiConfigurationLoader());
    }

    ApplicationFactory(AiConfigurationLoader aiConfigurationLoader) {
        this.aiConfigurationLoader = Objects.requireNonNull(aiConfigurationLoader, "aiConfigurationLoader");
    }

    public ApplicationServices create() {
        return createWith(readMergedConfiguration(), new SystemTimeProvider());
    }

    public ApplicationServices create(Map<String, String> configurationValues) {
        return create(configurationValues, new SystemTimeProvider());
    }

    public ApplicationServices create(Map<String, String> configurationValues, TimeProvider timeProvider) {
        Objects.requireNonNull(configurationValues, "configurationValues");
        Objects.requireNonNull(timeProvider, "timeProvider");
        return createWith(configurationValues, timeProvider);
    }

    private ApplicationServices createWith(Map<String, String> configurationValues, TimeProvider timeProvider) {
        IdGenerator idGenerator = new IncrementalIdGenerator();
        TaskService taskService = new TaskService(new InMemoryTaskRepository(), idGenerator);
        ScheduleService scheduleService = new ScheduleService(
                new InMemoryScheduleRepository(), idGenerator, timeProvider, new ScheduleConflictPolicy());
        StudyPlanService studyPlanService = new StudyPlanService(
                new InMemoryStudyPlanRepository(), idGenerator, timeProvider, new StudyPlanAnalysisService());
        FinanceService financeService =
                new FinanceService(new InMemoryTransactionRepository(), idGenerator, new FinanceStatisticsService());
        NoteService noteService =
                new NoteService(new InMemoryNoteRepository(), idGenerator, timeProvider, new NoteSearchPolicy());
        SummaryService summaryService = new SummaryService(
                taskService, scheduleService, studyPlanService, financeService, noteService, timeProvider);

        AiConfiguration aiConfiguration = loadAiConfiguration(configurationValues);
        PromptBuilder promptBuilder = new PromptBuilder();
        AiClient aiClient =
                new DeepSeekAiClient(aiConfiguration, JdkAiHttpTransport.create(aiConfiguration.timeout()));
        AiAssistantService aiAssistantService =
                new AiAssistantService(aiConfiguration, contextProvider(summaryService), promptBuilder, aiClient);

        SuggestionDraftRepository draftRepository = new InMemorySuggestionDraftRepository();
        StructuredSuggestionParser structuredSuggestionParser = new StructuredSuggestionParser();
        StructuredSuggestionDraftService structuredSuggestionDraftService =
                new StructuredSuggestionDraftService(
                        aiAssistantService,
                        structuredSuggestionParser,
                        draftRepository,
                        idGenerator);
        DraftImportService draftImportService = new DraftImportService(taskService, studyPlanService);
        DraftLifecycleService draftLifecycleService = new DraftLifecycleService(draftRepository, draftImportService);

        return new ApplicationServices(
                taskService,
                scheduleService,
                studyPlanService,
                financeService,
                noteService,
                summaryService,
                aiAssistantService,
                structuredSuggestionDraftService,
                draftLifecycleService,
                timeProvider);
    }

    private Map<String, String> readMergedConfiguration() {
        LinkedHashMap<String, String> values = new LinkedHashMap<>(System.getenv());
        for (String name : System.getProperties().stringPropertyNames()) {
            values.put(name, System.getProperty(name));
        }
        return Collections.unmodifiableMap(values);
    }

    private AiConfiguration loadAiConfiguration(Map<String, String> values) {
        OperationResult<AiConfiguration> result = aiConfigurationLoader.load(values);
        if (result.isFailure()) {
            return AiConfiguration.defaultWithoutApiKey();
        }
        return result.getPayload();
    }

    private ContextProvider contextProvider(SummaryService summaryService) {
        return summaryService::buildLocalContext;
    }
}
