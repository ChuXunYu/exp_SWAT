package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

class ErrorCodeTest {
    @Test
    void containsRequiredBusinessAndAiErrorCodes() {
        EnumSet<ErrorCode> errorCodes = EnumSet.allOf(ErrorCode.class);

        assertTrue(errorCodes.contains(ErrorCode.VALIDATION_ERROR));
        assertTrue(errorCodes.contains(ErrorCode.NOT_FOUND));
        assertTrue(errorCodes.contains(ErrorCode.STATE_CONFLICT));
        assertTrue(errorCodes.contains(ErrorCode.SCHEDULE_CONFLICT));
        assertTrue(errorCodes.contains(ErrorCode.AI_NOT_CONFIGURED));
        assertTrue(errorCodes.contains(ErrorCode.AI_AUTH_FAILED));
        assertTrue(errorCodes.contains(ErrorCode.AI_RATE_LIMITED));
        assertTrue(errorCodes.contains(ErrorCode.AI_TIMEOUT));
        assertTrue(errorCodes.contains(ErrorCode.AI_BAD_REQUEST));
        assertTrue(errorCodes.contains(ErrorCode.AI_REMOTE_UNAVAILABLE));
        assertTrue(errorCodes.contains(ErrorCode.AI_NETWORK_ERROR));
        assertTrue(errorCodes.contains(ErrorCode.AI_EMPTY_RESPONSE));
        assertTrue(errorCodes.contains(ErrorCode.AI_MALFORMED_RESPONSE));
        assertTrue(errorCodes.contains(ErrorCode.SYSTEM_ERROR));
    }

    @Test
    void valueOfReturnsStableErrorCodeByName() {
        assertEquals(ErrorCode.VALIDATION_ERROR, ErrorCode.valueOf("VALIDATION_ERROR"));
    }
}
