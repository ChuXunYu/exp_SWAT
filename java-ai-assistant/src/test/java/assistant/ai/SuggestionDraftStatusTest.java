package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SuggestionDraftStatusTest {
    @Test
    void enumNamesAreStable() {
        assertEquals("CONFIRMABLE", SuggestionDraftStatus.CONFIRMABLE.name());
        assertEquals("CANCELLED", SuggestionDraftStatus.CANCELLED.name());
        assertEquals("IMPORTED", SuggestionDraftStatus.IMPORTED.name());
    }

    @Test
    void confirmableAndTerminalHelpersReflectStatus() {
        assertTrue(SuggestionDraftStatus.CONFIRMABLE.isConfirmable());
        assertFalse(SuggestionDraftStatus.CONFIRMABLE.isTerminal());
        assertFalse(SuggestionDraftStatus.CANCELLED.isConfirmable());
        assertTrue(SuggestionDraftStatus.CANCELLED.isTerminal());
        assertFalse(SuggestionDraftStatus.IMPORTED.isConfirmable());
        assertTrue(SuggestionDraftStatus.IMPORTED.isTerminal());
    }
}
