package assistant.ai;

import java.io.IOException;

public interface AiHttpTransport {
    AiHttpResponse send(AiHttpRequest request) throws IOException, InterruptedException;
}
