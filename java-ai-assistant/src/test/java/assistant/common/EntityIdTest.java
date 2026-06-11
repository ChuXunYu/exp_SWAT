package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EntityIdTest {
    @Test
    void acceptsPositiveValueAndExposesAccessor() {
        EntityId id = new EntityId(42);

        assertEquals(42, id.value());
    }

    @Test
    void rejectsZeroValue() {
        assertThrows(IllegalArgumentException.class, () -> new EntityId(0));
    }

    @Test
    void rejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> new EntityId(-1));
    }

    @Test
    void equalityUsesUnderlyingValue() {
        assertEquals(new EntityId(42), new EntityId(42));
        assertNotEquals(new EntityId(42), new EntityId(43));
    }

    @Test
    void hashCodeUsesUnderlyingValue() {
        assertEquals(new EntityId(42).hashCode(), new EntityId(42).hashCode());
    }

    @Test
    void canBeUsedAsMapKeyByUnderlyingValue() {
        Map<EntityId, String> valuesById = new HashMap<>();
        valuesById.put(new EntityId(42), "task");

        assertEquals("task", valuesById.get(new EntityId(42)));
    }

    @Test
    void toStringUsesStableReadableFormat() {
        assertEquals("EntityId[value=42]", new EntityId(42).toString());
    }

    @Test
    void compareToSortsByNumericValueAscending() {
        List<EntityId> ids = new ArrayList<>(List.of(new EntityId(3), new EntityId(1), new EntityId(2)));

        ids.sort(EntityId::compareTo);

        assertEquals(List.of(new EntityId(1), new EntityId(2), new EntityId(3)), ids);
    }

    @Test
    void compareToRejectsNull() {
        assertThrows(NullPointerException.class, () -> new EntityId(1).compareTo(null));
    }
}
