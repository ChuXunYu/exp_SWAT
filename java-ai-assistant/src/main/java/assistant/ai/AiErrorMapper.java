package assistant.ai;

import assistant.common.ErrorCode;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.Objects;

public final class AiErrorMapper {
    public ErrorCode mapHttpStatus(int statusCode) {
        return switch (statusCode) {
            case 401, 403 -> ErrorCode.AI_AUTH_FAILED;
            case 429 -> ErrorCode.AI_RATE_LIMITED;
            case 408, 504 -> ErrorCode.AI_TIMEOUT;
            case 400, 422 -> ErrorCode.AI_BAD_REQUEST;
            default -> {
                if (statusCode >= 400 && statusCode <= 499) {
                    yield ErrorCode.AI_BAD_REQUEST;
                }
                yield ErrorCode.AI_REMOTE_UNAVAILABLE;
            }
        };
    }

    public ErrorCode mapException(Exception exception) {
        Objects.requireNonNull(exception, "exception");
        if (exception instanceof HttpTimeoutException) {
            return ErrorCode.AI_TIMEOUT;
        }
        if (exception instanceof IOException) {
            return ErrorCode.AI_NETWORK_ERROR;
        }
        if (exception instanceof InterruptedException) {
            return ErrorCode.AI_NETWORK_ERROR;
        }
        return ErrorCode.AI_REMOTE_UNAVAILABLE;
    }
}
