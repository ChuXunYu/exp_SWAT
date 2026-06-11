package assistant.common;

import java.util.Objects;

public record EntityId(long value) implements Comparable<EntityId> {
    public EntityId {
        if (value <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
    }

    @Override
    public int compareTo(EntityId other) {
        Objects.requireNonNull(other, "other");
        return Long.compare(value, other.value());
    }
}
