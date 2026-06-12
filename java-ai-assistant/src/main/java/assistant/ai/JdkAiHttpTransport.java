package assistant.ai;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public final class JdkAiHttpTransport implements AiHttpTransport {
    private final HttpClient httpClient;

    public JdkAiHttpTransport(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public static JdkAiHttpTransport create(Duration connectTimeout) {
        Objects.requireNonNull(connectTimeout, "connectTimeout");
        if (connectTimeout.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException("connectTimeout must be positive");
        }
        return new JdkAiHttpTransport(HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build());
    }

    @Override
    public AiHttpResponse send(AiHttpRequest request) throws IOException, InterruptedException {
        Objects.requireNonNull(request, "request");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(request.uri())
                .timeout(request.timeout())
                .POST(HttpRequest.BodyPublishers.ofString(request.body(), StandardCharsets.UTF_8));
        request.headers().forEach(builder::header);

        HttpResponse<String> response = httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = response.body();
        return new AiHttpResponse(response.statusCode(), body == null ? "" : body);
    }
}
