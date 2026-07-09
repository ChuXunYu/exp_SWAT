package assistant.ai;

import java.util.List;
import java.util.Objects;

public record AiRequest(
        String model,
        List<AiMessage> messages,
        boolean stream,
        AiResponseFormat responseFormat,
        Integer maxTokens) {
    public AiRequest {
        model = Objects.requireNonNull(model, "model").strip();
        if (model.isBlank()) {
            throw new IllegalArgumentException("model must not be blank");
        }
        Objects.requireNonNull(messages, "messages");
        messages = messages.stream()
                .map(message -> Objects.requireNonNull(message, "message"))
                .toList();
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
        if (maxTokens != null && maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
    }

    public AiRequest(String model, List<AiMessage> messages, boolean stream) {
        this(model, messages, stream, null, null);
    }

    public static AiRequest nonStreaming(String model, List<AiMessage> messages) {
        return new AiRequest(model, messages, false);
    }

    public static AiRequest structuredJson(String model, List<AiMessage> messages, int maxTokens) {
        return new AiRequest(model, messages, false, AiResponseFormat.jsonObject(), maxTokens);
    }
}
