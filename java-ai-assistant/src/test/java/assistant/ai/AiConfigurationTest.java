package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class AiConfigurationTest {
    @Test
    void defaultWithoutApiKeyUsesDeepSeekDefaults() {
        AiConfiguration configuration = AiConfiguration.defaultWithoutApiKey();

        assertAll(
                () -> assertEquals("https://api.deepseek.com", configuration.baseUrl()),
                () -> assertEquals("/chat/completions", configuration.chatCompletionsPath()),
                () -> assertEquals("deepseek-v4-flash", configuration.model()),
                () -> assertEquals(Duration.ofSeconds(20), configuration.timeout()),
                () -> assertEquals("", configuration.apiKey()),
                () -> assertFalse(configuration.hasApiKey()));
    }

    @Test
    void constructorNormalizesStringsAndDetectsApiKey() {
        AiConfiguration configuration = new AiConfiguration(
                " https://api.example.com/// ",
                " /chat/completions ",
                " model-a ",
                " placeholder-key ",
                Duration.ofSeconds(5));

        assertAll(
                () -> assertEquals("https://api.example.com", configuration.baseUrl()),
                () -> assertEquals("/chat/completions", configuration.chatCompletionsPath()),
                () -> assertEquals("model-a", configuration.model()),
                () -> assertEquals("placeholder-key", configuration.apiKey()),
                () -> assertTrue(configuration.hasApiKey()));
    }

    @Test
    void constructorRejectsNullRequiredFields() {
        assertAll(
                () -> assertNullFieldRejected("baseUrl",
                        () -> new AiConfiguration(null, "/chat", "model", "", Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("chatCompletionsPath",
                        () -> new AiConfiguration("https://api.example.com", null, "model", "", Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("model",
                        () -> new AiConfiguration("https://api.example.com", "/chat", null, "", Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("apiKey",
                        () -> new AiConfiguration("https://api.example.com", "/chat", "model", null, Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("timeout",
                        () -> new AiConfiguration("https://api.example.com", "/chat", "model", "", null)));
    }

    @Test
    void constructorRejectsBlankOrInvalidValues() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiConfiguration("   ", "/chat", "model", "", Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiConfiguration("///", "/chat", "model", "", Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiConfiguration("https://api.example.com", "   ", "model", "", Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiConfiguration("https://api.example.com", "chat", "model", "", Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiConfiguration("https://api.example.com", "/chat", "   ", "", Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiConfiguration("https://api.example.com", "/chat", "model", "", Duration.ZERO)));
    }

    @Test
    void toStringDoesNotExposeApiKeyValue() {
        AiConfiguration configuration = new AiConfiguration(
                "https://api.example.com",
                "/chat",
                "model",
                "placeholder-secret",
                Duration.ofSeconds(1));

        String text = configuration.toString();

        assertAll(
                () -> assertTrue(text.contains("apiKeyConfigured=true")),
                () -> assertFalse(text.contains("placeholder-secret")));
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
