package assistant.ai;

import assistant.common.OperationResult;

public interface AiClient {
    OperationResult<AiResponse> chat(AiRequest request);
}
