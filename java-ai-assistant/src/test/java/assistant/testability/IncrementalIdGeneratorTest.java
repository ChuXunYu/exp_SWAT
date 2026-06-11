package assistant.testability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.EntityId;
import org.junit.jupiter.api.Test;

class IncrementalIdGeneratorTest {
    @Test
    void defaultConstructorStartsAtOne() {
        IncrementalIdGenerator generator = new IncrementalIdGenerator();

        assertEquals(new EntityId(1), generator.nextId());
    }

    @Test
    void customStartIsInclusive() {
        IncrementalIdGenerator generator = new IncrementalIdGenerator(10);

        assertEquals(new EntityId(10), generator.nextId());
    }

    @Test
    void nextIdReturnsSequentialValues() {
        IncrementalIdGenerator generator = new IncrementalIdGenerator();

        assertEquals(new EntityId(1), generator.nextId());
        assertEquals(new EntityId(2), generator.nextId());
        assertEquals(new EntityId(3), generator.nextId());
    }

    @Test
    void rejectsZeroStart() {
        assertThrows(IllegalArgumentException.class, () -> new IncrementalIdGenerator(0));
    }

    @Test
    void rejectsNegativeStart() {
        assertThrows(IllegalArgumentException.class, () -> new IncrementalIdGenerator(-5));
    }

    @Test
    void nextIdReturnsEntityIdInstances() {
        IdGenerator generator = new IncrementalIdGenerator();

        EntityId id = generator.nextId();

        assertInstanceOf(EntityId.class, id);
        assertEquals(1, id.value());
    }

    @Test
    void independentGeneratorsKeepIndependentSequences() {
        IncrementalIdGenerator first = new IncrementalIdGenerator(1);
        IncrementalIdGenerator second = new IncrementalIdGenerator(10);

        assertEquals(new EntityId(1), first.nextId());
        assertEquals(new EntityId(10), second.nextId());
        assertEquals(new EntityId(2), first.nextId());
        assertEquals(new EntityId(11), second.nextId());
    }

    @Test
    void throwsWhenIdSpaceIsExhaustedAfterLongMaxValue() {
        IncrementalIdGenerator generator = new IncrementalIdGenerator(Long.MAX_VALUE);

        assertEquals(new EntityId(Long.MAX_VALUE), generator.nextId());
        assertThrows(IllegalStateException.class, generator::nextId);
    }
}
