package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProgressTest {
    @Test
    void acceptsZeroProgress() {
        Progress progress = new Progress(0);

        assertEquals(0, progress.value());
        assertFalse(progress.isComplete());
        assertEquals("0%", progress.toPercentageString());
    }

    @Test
    void acceptsMiddleProgress() {
        Progress progress = new Progress(75);

        assertEquals(75, progress.value());
        assertFalse(progress.isComplete());
        assertEquals("75%", progress.toPercentageString());
    }

    @Test
    void acceptsCompleteProgress() {
        Progress progress = new Progress(100);

        assertEquals(100, progress.value());
        assertTrue(progress.isComplete());
        assertEquals("100%", progress.toPercentageString());
    }

    @Test
    void rejectsNegativeProgress() {
        assertThrows(IllegalArgumentException.class, () -> new Progress(-1));
    }

    @Test
    void rejectsProgressGreaterThanOneHundred() {
        assertThrows(IllegalArgumentException.class, () -> new Progress(101));
    }

    @Test
    void zeroFactoryReturnsZeroProgress() {
        Progress progress = Progress.zero();

        assertEquals(0, progress.value());
        assertFalse(progress.isComplete());
    }

    @Test
    void completeFactoryReturnsCompleteProgress() {
        Progress progress = Progress.complete();

        assertEquals(100, progress.value());
        assertTrue(progress.isComplete());
    }

    @Test
    void ofFactoryUsesSameValidationAsConstructor() {
        Progress progress = Progress.of(50);

        assertEquals(50, progress.value());
        assertThrows(IllegalArgumentException.class, () -> Progress.of(-1));
        assertThrows(IllegalArgumentException.class, () -> Progress.of(101));
    }

    @Test
    void equalityAndHashCodeUseProgressValue() {
        Progress progress = new Progress(75);
        Progress same = new Progress(75);
        Progress different = new Progress(76);

        assertEquals(same, progress);
        assertEquals(same.hashCode(), progress.hashCode());
        assertNotEquals(different, progress);
    }

    @Test
    void toStringUsesRecordComponentNameAndValue() {
        assertEquals("Progress[value=75]", new Progress(75).toString());
    }
}
