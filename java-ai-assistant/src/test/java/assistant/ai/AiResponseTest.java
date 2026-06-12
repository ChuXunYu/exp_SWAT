package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AiResponseTest {
    @Test
    void constructorNormalizesContent() {
        AiResponse response = new AiResponse(" answer ");

        assertEquals("answer", response.content());
    }

    @Test
    void constructorRejectsNullOrBlankContent() {
        assertAll(
                () -> {
                    NullPointerException exception = assertThrows(NullPointerException.class, () -> new AiResponse(null));
                    assertEquals("content", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception =
                            assertThrows(IllegalArgumentException.class, () -> new AiResponse("   "));
                    assertEquals("content must not be blank", exception.getMessage());
                });
    }
}
