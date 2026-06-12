package assistant.ai;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public final class AiConfigurationLoader {
    public static final String API_KEY_NAME = "DEEPSEEK_API_KEY";
    public static final String BASE_URL_NAME = "DEEPSEEK_BASE_URL";
    public static final String MODEL_NAME = "DEEPSEEK_MODEL";
    public static final String TIMEOUT_SECONDS_NAME = "DEEPSEEK_TIMEOUT_SECONDS";

    public OperationResult<AiConfiguration> load(Map<String, String> values) {
        Objects.requireNonNull(values, "values");

        OperationResult<Duration> timeoutResult = loadTimeout(values.get(TIMEOUT_SECONDS_NAME));
        if (timeoutResult.isFailure()) {
            return OperationResult.failure(timeoutResult.getErrorCode(), timeoutResult.getMessage());
        }

        try {
            return OperationResult.success(new AiConfiguration(
                    valueOrDefault(values.get(BASE_URL_NAME), AiConfiguration.DEFAULT_BASE_URL),
                    AiConfiguration.DEFAULT_CHAT_COMPLETIONS_PATH,
                    valueOrDefault(values.get(MODEL_NAME), AiConfiguration.DEFAULT_MODEL),
                    valueOrDefault(values.get(API_KEY_NAME), ""),
                    timeoutResult.getPayload()));
        } catch (IllegalArgumentException exception) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "invalid DeepSeek configuration");
        }
    }

    private static OperationResult<Duration> loadTimeout(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return OperationResult.success(AiConfiguration.DEFAULT_TIMEOUT);
        }

        try {
            int seconds = Integer.parseInt(rawValue.strip());
            if (seconds <= 0) {
                return invalidTimeout();
            }
            return OperationResult.success(Duration.ofSeconds(seconds));
        } catch (NumberFormatException exception) {
            return invalidTimeout();
        }
    }

    private static OperationResult<Duration> invalidTimeout() {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "invalid DeepSeek timeout seconds");
    }

    private static String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.strip();
    }
}
