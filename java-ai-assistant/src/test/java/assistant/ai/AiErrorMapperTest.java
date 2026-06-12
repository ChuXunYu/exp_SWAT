package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import assistant.common.ErrorCode;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import org.junit.jupiter.api.Test;

class AiErrorMapperTest {
    private final AiErrorMapper mapper = new AiErrorMapper();

    @Test
    void mapsExplicitHttpStatuses() {
        assertAll(
                () -> assertEquals(ErrorCode.AI_AUTH_FAILED, mapper.mapHttpStatus(401)),
                () -> assertEquals(ErrorCode.AI_AUTH_FAILED, mapper.mapHttpStatus(403)),
                () -> assertEquals(ErrorCode.AI_RATE_LIMITED, mapper.mapHttpStatus(429)),
                () -> assertEquals(ErrorCode.AI_TIMEOUT, mapper.mapHttpStatus(408)),
                () -> assertEquals(ErrorCode.AI_TIMEOUT, mapper.mapHttpStatus(504)),
                () -> assertEquals(ErrorCode.AI_BAD_REQUEST, mapper.mapHttpStatus(400)),
                () -> assertEquals(ErrorCode.AI_BAD_REQUEST, mapper.mapHttpStatus(422)));
    }

    @Test
    void mapsStatusClasses() {
        assertAll(
                () -> assertEquals(ErrorCode.AI_BAD_REQUEST, mapper.mapHttpStatus(418)),
                () -> assertEquals(ErrorCode.AI_REMOTE_UNAVAILABLE, mapper.mapHttpStatus(302)),
                () -> assertEquals(ErrorCode.AI_REMOTE_UNAVAILABLE, mapper.mapHttpStatus(500)),
                () -> assertEquals(ErrorCode.AI_REMOTE_UNAVAILABLE, mapper.mapHttpStatus(100)),
                () -> assertEquals(ErrorCode.AI_REMOTE_UNAVAILABLE, mapper.mapHttpStatus(200)));
    }

    @Test
    void mapsExceptions() {
        assertAll(
                () -> assertEquals(ErrorCode.AI_TIMEOUT, mapper.mapException(new HttpTimeoutException("timeout"))),
                () -> assertEquals(ErrorCode.AI_NETWORK_ERROR, mapper.mapException(new IOException("io"))),
                () -> assertEquals(ErrorCode.AI_NETWORK_ERROR, mapper.mapException(new InterruptedException("interrupt"))),
                () -> assertEquals(ErrorCode.AI_REMOTE_UNAVAILABLE, mapper.mapException(new RuntimeException("runtime"))));
    }

    @Test
    void mapExceptionRejectsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> mapper.mapException(null));

        assertEquals("exception", exception.getMessage());
    }
}
