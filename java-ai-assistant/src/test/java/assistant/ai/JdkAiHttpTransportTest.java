package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JdkAiHttpTransportTest {
    @Test
    void constructorRejectsNullHttpClient() {
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> new JdkAiHttpTransport(null));

        assertEquals("httpClient", exception.getMessage());
    }

    @Test
    void createRejectsInvalidConnectTimeout() {
        assertAll(
                () -> assertNullFieldRejected("connectTimeout", () -> JdkAiHttpTransport.create(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> JdkAiHttpTransport.create(Duration.ZERO)));
    }

    @Test
    void aiHttpRequestValidatesAndCopiesHeaders() {
        Map<String, String> headers = new java.util.LinkedHashMap<>();
        headers.put("X-Test", "value");
        AiHttpRequest request = new AiHttpRequest(
                URI.create("https://api.example.com/chat"),
                headers,
                "",
                Duration.ofSeconds(1));
        headers.put("X-Test", "changed");

        assertAll(
                () -> assertEquals("value", request.headers().get("X-Test")),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> request.headers().put("Another", "value")),
                () -> assertNullFieldRejected("uri", () -> new AiHttpRequest(null, Map.of(), "", Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiHttpRequest(URI.create("/chat"), Map.of(), "", Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiHttpRequest(
                                URI.create("mailto:test@example.com"),
                                Map.of(),
                                "",
                                Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("headers",
                        () -> new AiHttpRequest(URI.create("https://api.example.com"), null, "", Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("body",
                        () -> new AiHttpRequest(URI.create("https://api.example.com"), Map.of(), null, Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("timeout",
                        () -> new AiHttpRequest(URI.create("https://api.example.com"), Map.of(), "", null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiHttpRequest(URI.create("https://api.example.com"), Map.of(), "", Duration.ZERO)));
    }

    @Test
    void aiHttpRequestValidatesHeaderEntries() {
        assertAll(
                () -> assertNullFieldRejected("headerName",
                        () -> new AiHttpRequest(
                                URI.create("https://api.example.com"),
                                mapWithNullName(),
                                "",
                                Duration.ofSeconds(1))),
                () -> assertNullFieldRejected("headerValue",
                        () -> new AiHttpRequest(
                                URI.create("https://api.example.com"),
                                mapWithNullValue(),
                                "",
                                Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiHttpRequest(
                                URI.create("https://api.example.com"),
                                Map.of("Header", ""),
                                "",
                                Duration.ofSeconds(1))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AiHttpRequest(
                                URI.create("https://api.example.com"),
                                Map.of("   ", "value"),
                                "",
                                Duration.ofSeconds(1))));
    }

    @Test
    void aiHttpResponseValidatesFields() {
        AiHttpResponse response = new AiHttpResponse(200, "");

        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals("", response.body()),
                () -> assertThrows(IllegalArgumentException.class, () -> new AiHttpResponse(99, "")),
                () -> assertThrows(IllegalArgumentException.class, () -> new AiHttpResponse(600, "")),
                () -> assertNullFieldRejected("body", () -> new AiHttpResponse(200, null)));
    }

    @Test
    void sendConvertsRequestToJdkPostRequest() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> jdkResponse = mock(HttpResponse.class);
        when(jdkResponse.statusCode()).thenReturn(201);
        when(jdkResponse.body()).thenReturn("response-body");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(jdkResponse);
        JdkAiHttpTransport transport = new JdkAiHttpTransport(httpClient);
        AiHttpRequest request = new AiHttpRequest(
                URI.create("https://api.example.com/chat"),
                Map.of("Accept", "application/json", "X-Test", "value"),
                "{\"hello\":\"世界\"}",
                Duration.ofSeconds(3));

        AiHttpResponse response = transport.send(request);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest captured = requestCaptor.getValue();

        assertAll(
                () -> assertEquals(201, response.statusCode()),
                () -> assertEquals("response-body", response.body()),
                () -> assertEquals("POST", captured.method()),
                () -> assertEquals(URI.create("https://api.example.com/chat"), captured.uri()),
                () -> assertEquals(Optional.of(Duration.ofSeconds(3)), captured.timeout()),
                () -> assertEquals(List.of("application/json"), captured.headers().allValues("Accept")),
                () -> assertEquals(List.of("value"), captured.headers().allValues("X-Test")),
                () -> assertEquals("{\"hello\":\"世界\"}", publishedBody(captured)));
    }

    @Test
    void sendNormalizesNullResponseBodyToEmptyString() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> jdkResponse = mock(HttpResponse.class);
        when(jdkResponse.statusCode()).thenReturn(204);
        when(jdkResponse.body()).thenReturn(null);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(jdkResponse);

        AiHttpResponse response = new JdkAiHttpTransport(httpClient).send(new AiHttpRequest(
                URI.create("https://api.example.com/chat"),
                Map.of(),
                "",
                Duration.ofSeconds(3)));

        assertEquals("", response.body());
    }

    @Test
    void sendRejectsNullRequest() {
        HttpClient httpClient = mock(HttpClient.class);

        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> new JdkAiHttpTransport(httpClient).send(null));

        assertEquals("request", exception.getMessage());
    }

    private static String publishedBody(HttpRequest request) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                byte[] bytes = new byte[item.remaining()];
                item.get(bytes);
                output.writeBytes(bytes);
            }

            @Override
            public void onError(Throwable throwable) {
                throw new AssertionError(throwable);
            }

            @Override
            public void onComplete() {
            }
        });
        return output.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, String> mapWithNullName() {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put(null, "value");
        return map;
    }

    private static Map<String, String> mapWithNullValue() {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put("Header", null);
        return map;
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }
}
