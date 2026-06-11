package assistant.testability;

import assistant.common.EntityId;

public final class IncrementalIdGenerator implements IdGenerator {
    private long nextValue;
    private boolean exhausted;

    public IncrementalIdGenerator() {
        this(1);
    }

    public IncrementalIdGenerator(long startInclusive) {
        if (startInclusive <= 0) {
            throw new IllegalArgumentException("startInclusive must be positive");
        }
        this.nextValue = startInclusive;
    }

    @Override
    public EntityId nextId() {
        if (exhausted) {
            throw new IllegalStateException("id space exhausted");
        }

        EntityId id = new EntityId(nextValue);
        if (nextValue == Long.MAX_VALUE) {
            exhausted = true;
        } else {
            nextValue++;
        }
        return id;
    }
}
