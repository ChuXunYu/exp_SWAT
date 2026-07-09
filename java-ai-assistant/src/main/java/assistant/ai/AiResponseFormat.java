package assistant.ai;

import java.util.Objects;

public record AiResponseFormat(String type) {
    private static final String JSON_OBJECT = "json_object";

    public AiResponseFormat {
        type = Objects.requireNonNull(type, "type").strip();
        if (type.isBlank()) {
            throw new IllegalArgumentException("type must not be blank");
        }
    }

    public static AiResponseFormat jsonObject() {
        return new AiResponseFormat(JSON_OBJECT);
    }

    public boolean isJsonObject() {
        return JSON_OBJECT.equals(type);
    }
}
