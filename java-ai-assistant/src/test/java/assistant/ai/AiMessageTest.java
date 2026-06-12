package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AiMessageTest {
    @Test
    void roleWireValuesAreOpenAiCompatible() {
        assertAll(
                () -> assertEquals("system", AiRole.SYSTEM.wireValue()),
                () -> assertEquals("user", AiRole.USER.wireValue()),
                () -> assertEquals("assistant", AiRole.ASSISTANT.wireValue()));
    }

    @Test
    void constructorNormalizesContent() {
        AiMessage message = new AiMessage(AiRole.USER, " hello ");

        assertEquals("hello", message.content());
    }

    @Test
    void constructorRejectsNullRoleOrContent() {
        assertAll(
                () -> assertNullFieldRejected("role", () -> new AiMessage(null, "hello")),
                () -> assertNullFieldRejected("content", () -> new AiMessage(AiRole.USER, null)));
    }

    @Test
    void constructorRejectsBlankContent() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new AiMessage(AiRole.USER, "   "));

        assertEquals("content must not be blank", exception.getMessage());
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
