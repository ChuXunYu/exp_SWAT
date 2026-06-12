package assistant.ai;

import java.time.Duration;
import java.util.Objects;

public record AiConfiguration(
        String baseUrl,
        String chatCompletionsPath,
        String model,
        String apiKey,
        Duration timeout) {
    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    public static final String DEFAULT_CHAT_COMPLETIONS_PATH = "/chat/completions";
    public static final String DEFAULT_MODEL = "deepseek-v4-flash";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    public AiConfiguration {
        baseUrl = requireBaseUrl(baseUrl);
        chatCompletionsPath = requirePath(chatCompletionsPath);
        model = requireNonBlank(model, "model");
        apiKey = Objects.requireNonNull(apiKey, "apiKey").strip();
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    public static AiConfiguration defaultWithoutApiKey() {
        return new AiConfiguration(
                DEFAULT_BASE_URL,
                DEFAULT_CHAT_COMPLETIONS_PATH,
                DEFAULT_MODEL,
                "",
                DEFAULT_TIMEOUT);
    }

    public boolean hasApiKey() {
        return !apiKey.isBlank();
    }

    @Override
    public String toString() {
        return "AiConfiguration[baseUrl=" + baseUrl
                + ", chatCompletionsPath=" + chatCompletionsPath
                + ", model=" + model
                + ", apiKeyConfigured=" + hasApiKey()
                + ", timeout=" + timeout
                + "]";
    }

    private static String requireBaseUrl(String value) {
        String normalized = requireNonBlank(value, "baseUrl");
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        return normalized;
    }

    private static String requirePath(String value) {
        String normalized = requireNonBlank(value, "chatCompletionsPath");
        if (!normalized.startsWith("/")) {
            throw new IllegalArgumentException("chatCompletionsPath must start with /");
        }
        return normalized;
    }

    private static String requireNonBlank(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).strip();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }
}
