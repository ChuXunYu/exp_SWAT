package assistant.app;

import assistant.ai.AiAssistantService;
import assistant.ai.DraftLifecycleService;
import assistant.finance.FinanceService;
import assistant.note.NoteService;
import assistant.schedule.ScheduleService;
import assistant.study.StudyPlanService;
import assistant.summary.SummaryService;
import assistant.task.TaskService;
import assistant.testability.TimeProvider;
import java.util.Objects;

public record ApplicationServices(
        TaskService taskService,
        ScheduleService scheduleService,
        StudyPlanService studyPlanService,
        FinanceService financeService,
        NoteService noteService,
        SummaryService summaryService,
        AiAssistantService aiAssistantService,
        DraftLifecycleService draftLifecycleService,
        TimeProvider timeProvider) {
    public ApplicationServices {
        Objects.requireNonNull(taskService, "taskService");
        Objects.requireNonNull(scheduleService, "scheduleService");
        Objects.requireNonNull(studyPlanService, "studyPlanService");
        Objects.requireNonNull(financeService, "financeService");
        Objects.requireNonNull(noteService, "noteService");
        Objects.requireNonNull(summaryService, "summaryService");
        Objects.requireNonNull(aiAssistantService, "aiAssistantService");
        Objects.requireNonNull(draftLifecycleService, "draftLifecycleService");
        Objects.requireNonNull(timeProvider, "timeProvider");
    }
}
