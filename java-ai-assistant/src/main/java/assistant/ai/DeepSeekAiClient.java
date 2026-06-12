package assistant.ai;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DeepSeekAiClient implements AiClient {
    private final AiConfiguration configuration;
    private final AiHttpTransport transport;
    private final ObjectMapper objectMapper;
    private final AiErrorMapper errorMapper;

    public DeepSeekAiClient(AiConfiguration configuration, AiHttpTransport transport) {
        this(configuration, transport, new ObjectMapper(), new AiErrorMapper());
    }

    public DeepSeekAiClient(
            AiConfiguration configuration,
            AiHttpTransport transport,
            ObjectMapper objectMapper,
            AiErrorMapper errorMapper) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.errorMapper = Objects.requireNonNull(errorMapper, "errorMapper");
    }

    @Override
    public OperationResult<AiResponse> chat(AiRequest request) {
        if (request == null) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "AI request is required");
        }

        URI uri;
        try {
            uri = URI.create(configuration.baseUrl() + configuration.chatCompletionsPath());
            if (!uri.isAbsolute() || isBlank(uri.getScheme()) || isBlank(uri.getHost())) {
                return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "invalid DeepSeek endpoint");
            }
        } catch (IllegalArgumentException exception) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "invalid DeepSeek endpoint");
        }

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(toWireRequest(request));
        } catch (JsonProcessingException exception) {
            return OperationResult.failure(ErrorCode.AI_BAD_REQUEST, "AI request could not be serialized");
        }

        AiHttpResponse response;
        try {
            response = transport.send(new AiHttpRequest(uri, headers(), requestBody, configuration.timeout()));
        } catch (HttpTimeoutException exception) {
            return failure(errorMapper.mapException(exception));
        } catch (IOException exception) {
            return failure(errorMapper.mapException(exception));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return OperationResult.failure(ErrorCode.AI_NETWORK_ERROR, "AI request was interrupted");
        }

        if (response.statusCode() < 200 || response.statusCode() > 299) {
            return failure(errorMapper.mapHttpStatus(response.statusCode()));
        }

        return parseResponse(response.body());
    }

    private WireRequest toWireRequest(AiRequest request) {
        List<WireMessage> messages = request.messages().stream()
                .map(message -> new WireMessage(message.role().wireValue(), message.content()))
                .toList();
        return new WireRequest(request.model(), messages, request.stream());
    }

    private Map<String, String> headers() {
        return Map.of(
                "Authorization", "Bearer " + configuration.apiKey(),
                "Content-Type", "application/json",
                "Accept", "application/json");
    }

    private OperationResult<AiResponse> parseResponse(String body) {
        if (body == null || body.isBlank()) {
            return OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (JsonProcessingException exception) {
            return OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, "AI response format is invalid");
        }

        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty");
        }

        JsonNode firstChoice = choices.get(0);
        if (!firstChoice.isObject()) {
            return OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, "AI response format is invalid");
        }

        JsonNode message = firstChoice.get("message");
        if (message == null || message.isNull()) {
            return OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty");
        }
        if (!message.isObject()) {
            return OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, "AI response format is invalid");
        }

        JsonNode content = message.get("content");
        if (content == null || content.isNull()) {
            return OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty");
        }
        if (!content.isTextual()) {
            return OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, "AI response format is invalid");
        }
        if (content.asText().strip().isBlank()) {
            return OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty");
        }
        return OperationResult.success(new AiResponse(content.asText()));
    }

    private OperationResult<AiResponse> failure(ErrorCode errorCode) {
        return OperationResult.failure(errorCode, messageFor(errorCode));
    }

    private String messageFor(ErrorCode errorCode) {
        return switch (errorCode) {
            case AI_AUTH_FAILED -> "DeepSeek authentication failed";
            case AI_RATE_LIMITED -> "DeepSeek rate limit exceeded";
            case AI_TIMEOUT -> "AI request timed out";
            case AI_BAD_REQUEST -> "AI request was rejected";
            case AI_REMOTE_UNAVAILABLE -> "DeepSeek service is unavailable";
            case AI_NETWORK_ERROR -> "AI network request failed";
            case AI_EMPTY_RESPONSE -> "AI response is empty";
            case AI_MALFORMED_RESPONSE -> "AI response format is invalid";
            default -> "DeepSeek service is unavailable";
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record WireRequest(String model, List<WireMessage> messages, boolean stream) {
    }

    private record WireMessage(String role, String content) {
    }
}
