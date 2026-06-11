package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OperationResultTest {
    @Test
    void successResultCarriesPayload() {
        OperationResult<String> result = OperationResult.success("ok");

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("ok", result.getPayload());
        assertNull(result.getErrorCode());
        assertNull(result.getMessage());
    }

    @Test
    void successWithoutPayloadIsSuccessfulVoidResult() {
        OperationResult<Void> result = OperationResult.success();

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertNull(result.getPayload());
        assertNull(result.getErrorCode());
        assertNull(result.getMessage());
    }

    @Test
    void successWithNullPayloadIsSuccessfulResult() {
        OperationResult<String> result = OperationResult.success((String) null);

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertNull(result.getPayload());
        assertNull(result.getErrorCode());
        assertNull(result.getMessage());
    }

    @Test
    void failureResultCarriesErrorCodeAndMessage() {
        OperationResult<String> result = OperationResult.failure(ErrorCode.NOT_FOUND, "missing");

        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertNull(result.getPayload());
        assertEquals(ErrorCode.NOT_FOUND, result.getErrorCode());
        assertEquals("missing", result.getMessage());
    }

    @Test
    void successAccessorsAreStableAcrossRepeatedReads() {
        OperationResult<String> result = OperationResult.success("ok");

        assertEquals("ok", result.getPayload());
        assertEquals("ok", result.getPayload());
        assertTrue(result.isSuccess());
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertFalse(result.isFailure());
    }

    @Test
    void failureAccessorsAreStableAcrossRepeatedReads() {
        OperationResult<String> result = OperationResult.failure(ErrorCode.STATE_CONFLICT, "already done");

        assertEquals(ErrorCode.STATE_CONFLICT, result.getErrorCode());
        assertEquals(ErrorCode.STATE_CONFLICT, result.getErrorCode());
        assertEquals("already done", result.getMessage());
        assertEquals("already done", result.getMessage());
        assertNull(result.getPayload());
        assertNull(result.getPayload());
    }

    @Test
    void failureRejectsNullErrorCode() {
        assertThrows(NullPointerException.class, () -> OperationResult.failure(null, "missing"));
    }

    @Test
    void failureRejectsBlankMessage() {
        assertThrows(IllegalArgumentException.class,
                () -> OperationResult.failure(ErrorCode.VALIDATION_ERROR, "   "));
    }

    @Test
    void failureRejectsNullMessage() {
        assertThrows(IllegalArgumentException.class,
                () -> OperationResult.failure(ErrorCode.VALIDATION_ERROR, null));
    }
}
