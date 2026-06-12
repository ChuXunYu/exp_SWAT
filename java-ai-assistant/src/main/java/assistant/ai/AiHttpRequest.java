package assistant.ai;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record AiHttpRequest(
        URI uri,
        Map<String, String> headers,
        String body,
        Duration timeout) {
    public AiHttpRequest {
        Objects.requireNonNull(uri, "uri");
        if (!uri.isAbsolute() || isBlank(uri.getScheme()) || isBlank(uri.getHost())) {
            throw new IllegalArgumentException("uri must be absolute");
        }
        Objects.requireNonNull(headers, "headers");
        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String name = Objects.requireNonNull(entry.getKey(), "headerName");
            String value = Objects.requireNonNull(entry.getValue(), "headerValue");
            if (name.strip().isBlank()) {
                throw new IllegalArgumentException("header name must not be blank");
            }
            if (value.isEmpty()) {
                throw new IllegalArgumentException("header value must not be empty");
            }
            copy.put(name, value);
        }
        headers = Map.copyOf(copy);
        body = Objects.requireNonNull(body, "body");
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
