package assistant.ai;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.summary.LocalContext;
import java.util.Objects;

public final class AiAssistantService {
    private final AiConfiguration configuration;
    private final ContextProvider contextProvider;
    private final PromptBuilder promptBuilder;
    private final AiClient aiClient;

    public AiAssistantService(
            AiConfiguration configuration,
            ContextProvider contextProvider,
            PromptBuilder promptBuilder,
            AiClient aiClient) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider");
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder");
        this.aiClient = Objects.requireNonNull(aiClient, "aiClient");
    }

    public OperationResult<String> ask(AiScenario scenario, String userQuestion) {
        if (scenario == null) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "ai scenario is required");
        }
        if (!configuration.hasApiKey()) {
            return OperationResult.failure(ErrorCode.AI_NOT_CONFIGURED, "DeepSeek API key is not configured");
        }
        if (userQuestion == null || userQuestion.isBlank()) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "user question must not be blank");
        }

        OperationResult<LocalContext> contextResult = contextProvider.getLocalContext();
        if (contextResult.isFailure()) {
            return propagateFailure(contextResult, "AI local context unavailable");
        }

        OperationResult<AiRequest> requestResult =
                promptBuilder.build(scenario, userQuestion, configuration, contextResult.getPayload());
        if (requestResult.isFailure()) {
            return propagateFailure(requestResult, "AI prompt build failed");
        }

        OperationResult<AiResponse> responseResult = aiClient.chat(requestResult.getPayload());
        if (responseResult.isFailure()) {
            return propagateFailure(responseResult, "AI client failed");
        }
        AiResponse response = responseResult.getPayload();
        if (response == null) {
            return OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty");
        }
        return OperationResult.success(response.content());
    }

    private static <T> OperationResult<T> propagateFailure(OperationResult<?> failure, String fallbackMessage) {
        Objects.requireNonNull(failure, "failure");
        if (!failure.isFailure()) {
            throw new IllegalArgumentException("failure result is required");
        }
        return OperationResult.failure(
                failure.getErrorCode(),
                stableMessage(failure.getMessage(), fallbackMessage));
    }

    private static String stableMessage(String message, String fallbackMessage) {
        if (message != null && !message.isBlank()) {
            return message;
        }
        return Objects.requireNonNull(fallbackMessage, "fallbackMessage");
    }
}
