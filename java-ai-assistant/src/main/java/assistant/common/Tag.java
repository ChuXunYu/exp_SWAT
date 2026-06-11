package assistant.common;

import java.util.Locale;
import java.util.Objects;

public record Tag(String value) {
    public Tag {
        value = Objects.requireNonNull(value, "value").strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        value = value.toLowerCase(Locale.ROOT);
    }

    public static Tag of(String value) {
        return new Tag(value);
    }

    public String displayName() {
        return value;
    }
}
