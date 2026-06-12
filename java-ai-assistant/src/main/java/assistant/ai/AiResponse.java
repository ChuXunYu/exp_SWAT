package assistant.ai;

import java.util.Objects;

public record AiResponse(String content) {
    public AiResponse {
        content = Objects.requireNonNull(content, "content").strip();
        if (content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }
}
