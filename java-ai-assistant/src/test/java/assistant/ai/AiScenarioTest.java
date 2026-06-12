package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AiScenarioTest {
    @Test
    void nonStructuredScenariosHaveInstructionsWithoutTargetType() {
        assertAll(
                () -> assertNonStructured(AiScenario.GENERAL_QA, "个人学习与生活助手"),
                () -> assertNonStructured(AiScenario.STUDY_ADVICE, "学习建议"),
                () -> assertNonStructured(AiScenario.NOTE_SUMMARY, "笔记总结建议"));
    }

    @Test
    void structuredTaskSuggestionRequiresJsonTaskDraft() {
        AiScenario scenario = AiScenario.STRUCTURED_TASK_SUGGESTION;

        assertAll(
                () -> assertTrue(scenario.requiresStructuredJson()),
                () -> assertEquals("TASK_DRAFT", scenario.targetType().orElseThrow()),
                () -> assertTrue(scenario.systemInstruction().contains("只返回单个 JSON 对象")),
                () -> assertTrue(scenario.systemInstruction().contains("TASK_DRAFT")));
    }

    @Test
    void structuredStudyPlanSuggestionRequiresJsonStudyPlanDraft() {
        AiScenario scenario = AiScenario.STRUCTURED_STUDY_PLAN_SUGGESTION;

        assertAll(
                () -> assertTrue(scenario.requiresStructuredJson()),
                () -> assertEquals("STUDY_PLAN_DRAFT", scenario.targetType().orElseThrow()),
                () -> assertTrue(scenario.systemInstruction().contains("只返回单个 JSON 对象")),
                () -> assertTrue(scenario.systemInstruction().contains("STUDY_PLAN_DRAFT")));
    }

    private static void assertNonStructured(AiScenario scenario, String expectedInstructionText) {
        assertAll(
                () -> assertFalse(scenario.requiresStructuredJson()),
                () -> assertTrue(scenario.targetType().isEmpty()),
                () -> assertTrue(scenario.systemInstruction().contains(expectedInstructionText)));
    }
}
