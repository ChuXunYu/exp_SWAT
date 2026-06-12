package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiConfigurationLoaderTest {
    private final AiConfigurationLoader loader = new AiConfigurationLoader();

    @Test
    void loadUsesDefaultsWhenMapIsEmpty() {
        OperationResult<AiConfiguration> result = loader.load(Map.of());

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(AiConfiguration.DEFAULT_BASE_URL, result.getPayload().baseUrl()),
                () -> assertEquals(AiConfiguration.DEFAULT_MODEL, result.getPayload().model()),
                () -> assertEquals(AiConfiguration.DEFAULT_TIMEOUT, result.getPayload().timeout()),
                () -> assertFalse(result.getPayload().hasApiKey()));
    }

    @Test
    void loadUsesProvidedValuesFromMap() {
        OperationResult<AiConfiguration> result = loader.load(Map.of(
                AiConfigurationLoader.API_KEY_NAME, " placeholder-key ",
                AiConfigurationLoader.BASE_URL_NAME, " https://api.example.com/ ",
                AiConfigurationLoader.MODEL_NAME, " model-a ",
                AiConfigurationLoader.TIMEOUT_SECONDS_NAME, " 7 "));

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals("placeholder-key", result.getPayload().apiKey()),
                () -> assertEquals("https://api.example.com", result.getPayload().baseUrl()),
                () -> assertEquals("model-a", result.getPayload().model()),
                () -> assertEquals(Duration.ofSeconds(7), result.getPayload().timeout()));
    }

    @Test
    void loadTreatsBlankOverridesAsDefaultsExceptApiKey() {
        OperationResult<AiConfiguration> result = loader.load(Map.of(
                AiConfigurationLoader.API_KEY_NAME, "   ",
                AiConfigurationLoader.BASE_URL_NAME, "   ",
                AiConfigurationLoader.MODEL_NAME, "   ",
                AiConfigurationLoader.TIMEOUT_SECONDS_NAME, "   "));

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals("", result.getPayload().apiKey()),
                () -> assertEquals(AiConfiguration.DEFAULT_BASE_URL, result.getPayload().baseUrl()),
                () -> assertEquals(AiConfiguration.DEFAULT_MODEL, result.getPayload().model()),
                () -> assertEquals(AiConfiguration.DEFAULT_TIMEOUT, result.getPayload().timeout()));
    }

    @Test
    void loadReturnsValidationFailureForInvalidTimeout() {
        assertInvalidTimeout("abc");
        assertInvalidTimeout("0");
        assertInvalidTimeout("-1");
    }

    @Test
    void loadReturnsValidationFailureForInvalidConfiguration() {
        OperationResult<AiConfiguration> result = loader.load(Map.of(AiConfigurationLoader.BASE_URL_NAME, "///"));

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("invalid DeepSeek configuration", result.getMessage()));
    }

    @Test
    void loadRejectsNullMap() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> loader.load(null));

        assertEquals("values", exception.getMessage());
    }

    private void assertInvalidTimeout(String value) {
        OperationResult<AiConfiguration> result =
                loader.load(Map.of(AiConfigurationLoader.TIMEOUT_SECONDS_NAME, value));

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("invalid DeepSeek timeout seconds", result.getMessage()));
    }
}
