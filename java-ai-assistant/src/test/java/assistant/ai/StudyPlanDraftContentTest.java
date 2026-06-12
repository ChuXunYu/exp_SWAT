package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.Progress;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StudyPlanDraftContentTest {
    private static final LocalDate START = LocalDate.of(2026, 7, 1);
    private static final LocalDate END = LocalDate.of(2026, 7, 31);

    @Test
    void constructorNormalizesFieldsAndCopiesBreakdown() {
        List<String> breakdown = new ArrayList<>(List.of(" Basics ", " ", "\u2003Practice\u2003"));

        StudyPlanDraftContent content =
                new StudyPlanDraftContent(" Learn Java ", START, END, 20, Progress.of(10), breakdown);
        breakdown.add("Later mutation");

        assertEquals("Learn Java", content.goalName());
        assertEquals(List.of("Basics", "Practice"), content.breakdown());
        assertTrue(content.hasBreakdown());
        assertThrows(UnsupportedOperationException.class, () -> content.breakdown().add("extra"));
    }

    @Test
    void emptyBreakdownReportsAbsent() {
        StudyPlanDraftContent content =
                new StudyPlanDraftContent("Learn Java", START, END, 20, Progress.zero(), List.of(" "));

        assertEquals(List.of(), content.breakdown());
        assertFalse(content.hasBreakdown());
    }

    @Test
    void rejectsInvalidRequiredFields() {
        assertThrows(NullPointerException.class,
                () -> new StudyPlanDraftContent(null, START, END, 20, Progress.zero(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanDraftContent(" \t", START, END, 20, Progress.zero(), List.of()));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanDraftContent("Goal", null, END, 20, Progress.zero(), List.of()));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanDraftContent("Goal", START, null, 20, Progress.zero(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanDraftContent("Goal", END, START, 20, Progress.zero(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanDraftContent("Goal", START, END, 0, Progress.zero(), List.of()));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanDraftContent("Goal", START, END, 20, null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanDraftContent("Goal", START, END, 20, Progress.zero(), null));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanDraftContent("Goal", START, END, 20, Progress.zero(), List.of("ok", null)));
    }
}
