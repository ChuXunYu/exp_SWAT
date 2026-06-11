package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BusinessExceptionTest {
    @Test
    void carriesErrorCodeAndMessage() {
        BusinessException exception = new BusinessException(ErrorCode.VALIDATION_ERROR, "invalid");

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
        assertEquals("invalid", exception.getMessage());
    }

    @Test
    void carriesCauseWhenProvided() {
        IllegalStateException cause = new IllegalStateException("root");

        BusinessException exception = new BusinessException(ErrorCode.SYSTEM_ERROR, "failed", cause);

        assertEquals(ErrorCode.SYSTEM_ERROR, exception.getErrorCode());
        assertEquals("failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void allowsNullMessage() {
        BusinessException exception = new BusinessException(ErrorCode.NOT_FOUND, null);

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertNull(exception.getMessage());
    }

    @Test
    void allowsNullMessageAndNullCauseWhenCauseConstructorIsUsed() {
        BusinessException exception = new BusinessException(ErrorCode.SYSTEM_ERROR, null, null);

        assertEquals(ErrorCode.SYSTEM_ERROR, exception.getErrorCode());
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void rejectsNullErrorCode() {
        assertThrows(NullPointerException.class, () -> new BusinessException(null, "invalid"));
    }

    @Test
    void rejectsNullErrorCodeWhenCauseProvided() {
        RuntimeException cause = new RuntimeException("root");

        assertThrows(NullPointerException.class, () -> new BusinessException(null, "invalid", cause));
    }
}
