package assistant.common;

import java.util.Objects;

public final class OperationResult<T> {
    private final boolean success;
    private final T payload;
    private final ErrorCode errorCode;
    private final String message;

    private OperationResult(boolean success, T payload, ErrorCode errorCode, String message) {
        this.success = success;
        this.payload = payload;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static <T> OperationResult<T> success(T payload) {
        return new OperationResult<>(true, payload, null, null);
    }

    public static OperationResult<Void> success() {
        return new OperationResult<>(true, null, null, null);
    }

    public static <T> OperationResult<T> failure(ErrorCode errorCode, String message) {
        Objects.requireNonNull(errorCode, "errorCode");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be null or blank");
        }
        return new OperationResult<>(false, null, errorCode, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public T getPayload() {
        return payload;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
