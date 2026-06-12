package assistant.ai;

import java.util.Objects;

public record AiHttpResponse(int statusCode, String body) {
    public AiHttpResponse {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("statusCode must be between 100 and 599");
        }
        body = Objects.requireNonNull(body, "body");
    }
}
