package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SuggestionDraftTypeTest {
    @Test
    void enumNamesAreStable() {
        assertEquals("TASK_DRAFT", SuggestionDraftType.TASK_DRAFT.name());
        assertEquals("STUDY_PLAN_DRAFT", SuggestionDraftType.STUDY_PLAN_DRAFT.name());
    }

    @Test
    void helperMethodsReflectType() {
        assertTrue(SuggestionDraftType.TASK_DRAFT.isTaskDraft());
        assertFalse(SuggestionDraftType.TASK_DRAFT.isStudyPlanDraft());
        assertTrue(SuggestionDraftType.STUDY_PLAN_DRAFT.isStudyPlanDraft());
        assertFalse(SuggestionDraftType.STUDY_PLAN_DRAFT.isTaskDraft());
    }
}
