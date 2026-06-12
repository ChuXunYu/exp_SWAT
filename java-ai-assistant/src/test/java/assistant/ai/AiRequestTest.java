package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiRequestTest {
    @Test
    void nonStreamingCreatesRequestWithStreamFalse() {
        AiRequest request = AiRequest.nonStreaming("model", List.of(userMessage()));

        assertFalse(request.stream());
    }

    @Test
    void constructorNormalizesModelAndCopiesMessages() {
        ArrayList<AiMessage> messages = new ArrayList<>(List.of(userMessage()));

        AiRequest request = new AiRequest(" model ", messages, true);
        messages.add(new AiMessage(AiRole.ASSISTANT, "answer"));

        assertAll(
                () -> assertEquals("model", request.model()),
                () -> assertEquals(1, request.messages().size()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> request.messages().add(new AiMessage(AiRole.ASSISTANT, "answer"))));
    }

    @Test
    void constructorRejectsNullsAndBlankModel() {
        assertAll(
                () -> assertNullFieldRejected("model", () -> new AiRequest(null, List.of(userMessage()), false)),
                () -> assertNullFieldRejected("messages", () -> new AiRequest("model", null, false)),
                () -> assertNullFieldRejected("message", () -> new AiRequest("model", listWithNull(), false)),
                () -> assertThrows(IllegalArgumentException.class, () -> new AiRequest("   ", List.of(userMessage()), false)));
    }

    @Test
    void constructorRejectsEmptyMessages() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new AiRequest("model", List.of(), false));

        assertEquals("messages must not be empty", exception.getMessage());
    }

    private static AiMessage userMessage() {
        return new AiMessage(AiRole.USER, "question");
    }

    private static List<AiMessage> listWithNull() {
        ArrayList<AiMessage> messages = new ArrayList<>();
        messages.add(null);
        return messages;
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
