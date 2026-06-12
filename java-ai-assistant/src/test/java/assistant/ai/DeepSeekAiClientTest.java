package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeepSeekAiClientTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void constructorRejectsNullDependencies() {
        AiConfiguration configuration = configured();
        FakeTransport transport = new FakeTransport();
        assertAll(
                () -> assertNullFieldRejected("configuration",
                        () -> new DeepSeekAiClient(null, transport, OBJECT_MAPPER, new AiErrorMapper())),
                () -> assertNullFieldRejected("transport",
                        () -> new DeepSeekAiClient(configuration, null, OBJECT_MAPPER, new AiErrorMapper())),
                () -> assertNullFieldRejected("objectMapper",
                        () -> new DeepSeekAiClient(configuration, transport, null, new AiErrorMapper())),
                () -> assertNullFieldRejected("errorMapper",
                        () -> new DeepSeekAiClient(configuration, transport, OBJECT_MAPPER, null)));
    }

    @Test
    void chatBuildsOpenAiCompatibleRequestAndParsesSuccessResponse() throws Exception {
        FakeTransport transport = new FakeTransport();
        transport.response = new AiHttpResponse(
                200,
                "{\"choices\":[{\"message\":{\"content\":\"  Answer text  \"}}]}");
        DeepSeekAiClient client = new DeepSeekAiClient(configured(), transport, OBJECT_MAPPER, new AiErrorMapper());

        OperationResult<AiResponse> result = client.chat(request());
        JsonNode body = OBJECT_MAPPER.readTree(transport.receivedRequest.body());

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals("Answer text", result.getPayload().content()),
                () -> assertEquals(1, transport.calls),
                () -> assertEquals("https://api.example.com/chat/completions", transport.receivedRequest.uri().toString()),
                () -> assertEquals("Bearer placeholder-key", transport.receivedRequest.headers().get("Authorization")),
                () -> assertEquals("application/json", transport.receivedRequest.headers().get("Content-Type")),
                () -> assertEquals("application/json", transport.receivedRequest.headers().get("Accept")),
                () -> assertEquals(Duration.ofSeconds(5), transport.receivedRequest.timeout()),
                () -> assertEquals("model-a", body.get("model").asText()),
                () -> assertFalse(body.get("stream").asBoolean()),
                () -> assertEquals("user", body.get("messages").get(0).get("role").asText()),
                () -> assertEquals("hello", body.get("messages").get(0).get("content").asText()));
    }

    @Test
    void chatSerializesStreamingRequestWithAllMessagesInOrder() throws Exception {
        FakeTransport transport = new FakeTransport();
        DeepSeekAiClient client = new DeepSeekAiClient(configured(), transport, OBJECT_MAPPER, new AiErrorMapper());
        AiRequest streamingRequest = new AiRequest(
                "model-a",
                List.of(
                        new AiMessage(AiRole.SYSTEM, "system prompt"),
                        new AiMessage(AiRole.USER, "user question"),
                        new AiMessage(AiRole.ASSISTANT, "assistant context")),
                true);

        OperationResult<AiResponse> result = client.chat(streamingRequest);
        JsonNode body = OBJECT_MAPPER.readTree(transport.receivedRequest.body());
        JsonNode messages = body.get("messages");

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertTrue(body.get("stream").asBoolean()),
                () -> assertEquals(3, messages.size()),
                () -> assertEquals("system", messages.get(0).get("role").asText()),
                () -> assertEquals("system prompt", messages.get(0).get("content").asText()),
                () -> assertEquals("user", messages.get(1).get("role").asText()),
                () -> assertEquals("user question", messages.get(1).get("content").asText()),
                () -> assertEquals("assistant", messages.get(2).get("role").asText()),
                () -> assertEquals("assistant context", messages.get(2).get("content").asText()));
    }

    @Test
    void chatRejectsNullRequestWithoutCallingTransport() {
        FakeTransport transport = new FakeTransport();
        DeepSeekAiClient client = new DeepSeekAiClient(configured(), transport, OBJECT_MAPPER, new AiErrorMapper());

        OperationResult<AiResponse> result = client.chat(null);

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("AI request is required", result.getMessage()),
                () -> assertEquals(0, transport.calls));
    }

    @Test
    void chatRejectsInvalidEndpointWithoutCallingTransport() {
        FakeTransport transport = new FakeTransport();
        AiConfiguration configuration = new AiConfiguration("https://", "/chat", "model", "key", Duration.ofSeconds(5));
        DeepSeekAiClient client = new DeepSeekAiClient(configuration, transport, OBJECT_MAPPER, new AiErrorMapper());

        OperationResult<AiResponse> result = client.chat(request());

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("invalid DeepSeek endpoint", result.getMessage()),
                () -> assertEquals(0, transport.calls));
    }

    @Test
    void chatMapsSerializationFailureWithoutCallingTransport() {
        FakeTransport transport = new FakeTransport();
        ObjectMapper failingObjectMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws JsonProcessingException {
                throw new JsonProcessingException("cannot serialize") {
                };
            }
        };
        DeepSeekAiClient client = new DeepSeekAiClient(
                configured(),
                transport,
                failingObjectMapper,
                new AiErrorMapper());

        OperationResult<AiResponse> result = client.chat(request());

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_BAD_REQUEST, result.getErrorCode()),
                () -> assertEquals("AI request could not be serialized", result.getMessage()),
                () -> assertEquals(0, transport.calls));
    }

    @Test
    void chatMapsEmptyResponseShapes() {
        assertEmptyResponse("");
        assertEmptyResponse("   ");
        assertEmptyResponse("{\"choices\":[]}");
        assertEmptyResponse("{\"choices\":{}}");
        assertEmptyResponse("{\"choices\":[{}]}");
        assertEmptyResponse("{\"choices\":[{\"message\":{}}]}");
        assertEmptyResponse("{\"choices\":[{\"message\":{\"content\":null}}]}");
        assertEmptyResponse("{\"choices\":[{\"message\":{\"content\":\"   \"}}]}");
    }

    @Test
    void chatMapsMalformedResponseShapes() {
        assertMalformedResponse("{");
        assertMalformedResponse("{\"choices\":[1]}");
        assertMalformedResponse("{\"choices\":[{\"message\":1}]}");
        assertMalformedResponse("{\"choices\":[{\"message\":{\"content\":1}}]}");
    }

    @Test
    void chatMapsHttpStatusFailuresWithoutParsingBody() {
        assertStatusFailure(401, ErrorCode.AI_AUTH_FAILED, "DeepSeek authentication failed");
        assertStatusFailure(403, ErrorCode.AI_AUTH_FAILED, "DeepSeek authentication failed");
        assertStatusFailure(429, ErrorCode.AI_RATE_LIMITED, "DeepSeek rate limit exceeded");
        assertStatusFailure(408, ErrorCode.AI_TIMEOUT, "AI request timed out");
        assertStatusFailure(504, ErrorCode.AI_TIMEOUT, "AI request timed out");
        assertStatusFailure(400, ErrorCode.AI_BAD_REQUEST, "AI request was rejected");
        assertStatusFailure(422, ErrorCode.AI_BAD_REQUEST, "AI request was rejected");
        assertStatusFailure(418, ErrorCode.AI_BAD_REQUEST, "AI request was rejected");
        assertStatusFailure(302, ErrorCode.AI_REMOTE_UNAVAILABLE, "DeepSeek service is unavailable");
        assertStatusFailure(500, ErrorCode.AI_REMOTE_UNAVAILABLE, "DeepSeek service is unavailable");
        assertStatusFailure(100, ErrorCode.AI_REMOTE_UNAVAILABLE, "DeepSeek service is unavailable");
    }

    @Test
    void chatMapsTransportExceptions() {
        assertExceptionFailure(
                new IOException("io"),
                ErrorCode.AI_NETWORK_ERROR,
                "AI network request failed",
                false);
        assertExceptionFailure(
                new HttpTimeoutException("timeout"),
                ErrorCode.AI_TIMEOUT,
                "AI request timed out",
                false);
        assertExceptionFailure(
                new InterruptedException("interrupt"),
                ErrorCode.AI_NETWORK_ERROR,
                "AI request was interrupted",
                true);
        Thread.interrupted();
    }

    private static void assertEmptyResponse(String body) {
        OperationResult<AiResponse> result = resultForResponseBody(body);

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_EMPTY_RESPONSE, result.getErrorCode()),
                () -> assertEquals("AI response is empty", result.getMessage()));
    }

    private static void assertMalformedResponse(String body) {
        OperationResult<AiResponse> result = resultForResponseBody(body);

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_MALFORMED_RESPONSE, result.getErrorCode()),
                () -> assertEquals("AI response format is invalid", result.getMessage()));
    }

    private static OperationResult<AiResponse> resultForResponseBody(String body) {
        FakeTransport transport = new FakeTransport();
        transport.response = new AiHttpResponse(200, body);
        return new DeepSeekAiClient(configured(), transport, OBJECT_MAPPER, new AiErrorMapper()).chat(request());
    }

    private static void assertStatusFailure(int statusCode, ErrorCode errorCode, String message) {
        FakeTransport transport = new FakeTransport();
        transport.response = new AiHttpResponse(statusCode, "{");
        OperationResult<AiResponse> result =
                new DeepSeekAiClient(configured(), transport, OBJECT_MAPPER, new AiErrorMapper()).chat(request());

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(errorCode, result.getErrorCode()),
                () -> assertEquals(message, result.getMessage()));
    }

    private static void assertExceptionFailure(
            Exception exception,
            ErrorCode errorCode,
            String message,
            boolean interrupted) {
        FakeTransport transport = new FakeTransport();
        transport.exception = exception;
        OperationResult<AiResponse> result =
                new DeepSeekAiClient(configured(), transport, OBJECT_MAPPER, new AiErrorMapper()).chat(request());

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(errorCode, result.getErrorCode()),
                () -> assertEquals(message, result.getMessage()),
                () -> assertEquals(interrupted, Thread.currentThread().isInterrupted()));
    }

    private static AiConfiguration configured() {
        return new AiConfiguration(
                "https://api.example.com",
                "/chat/completions",
                "model-a",
                "placeholder-key",
                Duration.ofSeconds(5));
    }

    private static AiRequest request() {
        return AiRequest.nonStreaming("model-a", List.of(new AiMessage(AiRole.USER, "hello")));
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static final class FakeTransport implements AiHttpTransport {
        private AiHttpResponse response = new AiHttpResponse(
                200,
                "{\"choices\":[{\"message\":{\"content\":\"answer\"}}]}");
        private Exception exception;
        private AiHttpRequest receivedRequest;
        private int calls;

        @Override
        public AiHttpResponse send(AiHttpRequest request) throws IOException, InterruptedException {
            calls++;
            receivedRequest = request;
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            if (exception instanceof InterruptedException interruptedException) {
                throw interruptedException;
            }
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            return response;
        }
    }
}
