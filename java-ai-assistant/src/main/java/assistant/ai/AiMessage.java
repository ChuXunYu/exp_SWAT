package assistant.ai;

import java.util.Objects;

public record AiMessage(AiRole role, String content) {
    public AiMessage {
        Objects.requireNonNull(role, "role");
        content = Objects.requireNonNull(content, "content").strip();
        if (content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }
}
