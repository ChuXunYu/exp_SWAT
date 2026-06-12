package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.summary.DashboardSummary;
import assistant.summary.LocalContext;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiAssistantServiceTest {
    @Test
    void constructorRejectsNullDependencies() {
        assertAll(
                () -> assertNullFieldRejected("configuration",
                        () -> new AiAssistantService(null, new FakeContextProvider(), new PromptBuilder(), new FakeAiClient())),
                () -> assertNullFieldRejected("contextProvider",
                        () -> new AiAssistantService(configured(), null, new PromptBuilder(), new FakeAiClient())),
                () -> assertNullFieldRejected("promptBuilder",
                        () -> new AiAssistantService(configured(), new FakeContextProvider(), null, new FakeAiClient())),
                () -> assertNullFieldRejected("aiClient",
                        () -> new AiAssistantService(configured(), new FakeContextProvider(), new PromptBuilder(), null)));
    }

    @Test
    void askReturnsNotConfiguredWithoutCallingCollaborators() {
        FakeContextProvider contextProvider = new FakeContextProvider();
        PromptBuilder promptBuilder = org.mockito.Mockito.mock(PromptBuilder.class);
        FakeAiClient aiClient = new FakeAiClient();
        AiAssistantService service = new AiAssistantService(
                AiConfiguration.defaultWithoutApiKey(),
                contextProvider,
                promptBuilder,
                aiClient);

        OperationResult<String> result = service.ask(AiScenario.GENERAL_QA, "question");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_NOT_CONFIGURED, result.getErrorCode()),
                () -> assertEquals("DeepSeek API key is not configured", result.getMessage()),
                () -> assertEquals(0, contextProvider.calls),
                () -> verify(promptBuilder, never()).build(any(), any(), any(), any()),
                () -> assertEquals(0, aiClient.calls));
    }

    @Test
    void askReturnsValidationFailureForNullScenario() {
        FakeContextProvider contextProvider = new FakeContextProvider();
        FakeAiClient aiClient = new FakeAiClient();
        AiAssistantService service = new AiAssistantService(configured(), contextProvider, new PromptBuilder(), aiClient);

        OperationResult<String> result = service.ask(null, "question");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("ai scenario is required", result.getMessage()),
                () -> assertEquals(0, contextProvider.calls),
                () -> assertEquals(0, aiClient.calls));
    }

    @Test
    void askReturnsValidationFailureForBlankQuestionWithoutCallingContextOrClient() {
        assertBlankQuestionFails(null);
        assertBlankQuestionFails("   ");
    }

    private static void assertBlankQuestionFails(String userQuestion) {
        FakeContextProvider contextProvider = new FakeContextProvider();
        FakeAiClient aiClient = new FakeAiClient();
        AiAssistantService service = new AiAssistantService(configured(), contextProvider, new PromptBuilder(), aiClient);

        OperationResult<String> result = service.ask(AiScenario.GENERAL_QA, userQuestion);

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("user question must not be blank", result.getMessage()),
                () -> assertEquals(0, contextProvider.calls),
                () -> assertEquals(0, aiClient.calls));
    }

    @Test
    void askPropagatesContextProviderFailure() {
        FakeContextProvider contextProvider = new FakeContextProvider();
        contextProvider.result = OperationResult.failure(ErrorCode.SYSTEM_ERROR, "context failed");
        PromptBuilder promptBuilder = org.mockito.Mockito.mock(PromptBuilder.class);
        FakeAiClient aiClient = new FakeAiClient();
        AiAssistantService service = new AiAssistantService(configured(), contextProvider, promptBuilder, aiClient);

        OperationResult<String> result = service.ask(AiScenario.GENERAL_QA, "question");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.SYSTEM_ERROR, result.getErrorCode()),
                () -> assertEquals("context failed", result.getMessage()),
                () -> assertEquals(1, contextProvider.calls),
                () -> verify(promptBuilder, never()).build(any(), any(), any(), any()),
                () -> assertEquals(0, aiClient.calls));
    }

    @Test
    void askPropagatesPromptBuilderFailure() {
        PromptBuilder promptBuilder = org.mockito.Mockito.mock(PromptBuilder.class);
        when(promptBuilder.build(any(), any(), any(), any()))
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "prompt failed"));
        FakeAiClient aiClient = new FakeAiClient();
        AiAssistantService service = new AiAssistantService(configured(), new FakeContextProvider(), promptBuilder, aiClient);

        OperationResult<String> result = service.ask(AiScenario.GENERAL_QA, "question");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("prompt failed", result.getMessage()),
                () -> verify(promptBuilder).build(any(), any(), any(), any()),
                () -> assertEquals(0, aiClient.calls));
    }

    @Test
    void askSendsBuiltRequestToClientAndReturnsContent() {
        PromptBuilder promptBuilder = org.mockito.Mockito.mock(PromptBuilder.class);
        AiRequest request = AiRequest.nonStreaming("model-a", List.of(new AiMessage(AiRole.USER, "question")));
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn(OperationResult.success(request));
        FakeAiClient aiClient = new FakeAiClient();
        AiConfiguration configuration = configured();
        FakeContextProvider contextProvider = new FakeContextProvider();
        AiAssistantService service = new AiAssistantService(configuration, contextProvider, promptBuilder, aiClient);

        OperationResult<String> result = service.ask(AiScenario.STUDY_ADVICE, "question");

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals("answer", result.getPayload()),
                () -> verify(promptBuilder).build(
                        eq(AiScenario.STUDY_ADVICE),
                        eq("question"),
                        eq(configuration),
                        eq(contextProvider.context)),
                () -> assertSame(request, aiClient.receivedRequest),
                () -> assertEquals(1, aiClient.calls));
    }

    @Test
    void askPropagatesAiClientFailures() {
        for (ErrorCode errorCode : List.of(
                ErrorCode.AI_AUTH_FAILED,
                ErrorCode.AI_RATE_LIMITED,
                ErrorCode.AI_TIMEOUT,
                ErrorCode.AI_BAD_REQUEST,
                ErrorCode.AI_REMOTE_UNAVAILABLE,
                ErrorCode.AI_NETWORK_ERROR,
                ErrorCode.AI_EMPTY_RESPONSE,
                ErrorCode.AI_MALFORMED_RESPONSE)) {
            FakeAiClient aiClient = new FakeAiClient();
            aiClient.result = OperationResult.failure(errorCode, "client failed");
            AiAssistantService service = new AiAssistantService(
                    configured(),
                    new FakeContextProvider(),
                    successfulPromptBuilder(),
                    aiClient);

            OperationResult<String> result = service.ask(AiScenario.GENERAL_QA, "question");

            assertAll(
                    () -> assertTrue(result.isFailure()),
                    () -> assertEquals(errorCode, result.getErrorCode()),
                    () -> assertEquals("client failed", result.getMessage()));
        }
    }

    @Test
    void askReturnsEmptyResponseFailureWhenClientPayloadIsNull() {
        FakeAiClient aiClient = new FakeAiClient();
        aiClient.result = OperationResult.success((AiResponse) null);
        AiAssistantService service =
                new AiAssistantService(configured(), new FakeContextProvider(), successfulPromptBuilder(), aiClient);

        OperationResult<String> result = service.ask(AiScenario.GENERAL_QA, "question");

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.AI_EMPTY_RESPONSE, result.getErrorCode()),
                () -> assertEquals("AI response is empty", result.getMessage()));
    }

    @Test
    void askDoesNotModifyLocalBusinessData() {
        FakeContextProvider contextProvider = new FakeContextProvider();
        PromptBuilder promptBuilder = successfulPromptBuilder();
        FakeAiClient aiClient = new FakeAiClient();
        AiAssistantService service = new AiAssistantService(configured(), contextProvider, promptBuilder, aiClient);

        OperationResult<String> result = service.ask(AiScenario.GENERAL_QA, "question");

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertFalse(contextProvider.context.todayTaskLines().contains("modified")),
                () -> assertEquals(1, contextProvider.calls),
                () -> verify(promptBuilder).build(any(), any(), any(), any()),
                () -> assertEquals(1, aiClient.calls));
    }

    private static AiConfiguration configured() {
        return new AiConfiguration("https://api.example.com", "/chat", "model-a", "placeholder-key", Duration.ofSeconds(5));
    }

    private static void assertNullFieldRejected(String expectedMessage, Runnable action) {
        NullPointerException exception = assertThrows(NullPointerException.class, action::run);

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static LocalContext context() {
        LocalDate today = LocalDate.of(2026, 6, 12);
        DashboardSummary summary = new DashboardSummary(
                today,
                today,
                today,
                today,
                today,
                List.of(),
                List.of(),
                List.of(),
                0,
                0,
                assistant.finance.FinanceStatistics.zero(),
                List.of(),
                0,
                Map.of());
        return LocalContext.from(summary);
    }

    private static final class FakeContextProvider implements ContextProvider {
        private final LocalContext context = context();
        private OperationResult<LocalContext> result = OperationResult.success(context);
        private int calls;

        @Override
        public OperationResult<LocalContext> getLocalContext() {
            calls++;
            return result;
        }
    }

    private static PromptBuilder successfulPromptBuilder() {
        PromptBuilder promptBuilder = org.mockito.Mockito.mock(PromptBuilder.class);
        AiRequest request = AiRequest.nonStreaming("model-a", List.of(new AiMessage(AiRole.USER, "question")));
        when(promptBuilder.build(any(), any(), any(), any())).thenReturn(OperationResult.success(request));
        return promptBuilder;
    }

    private static final class FakeAiClient implements AiClient {
        private OperationResult<AiResponse> result = OperationResult.success(new AiResponse("answer"));
        private AiRequest receivedRequest;
        private int calls;

        @Override
        public OperationResult<AiResponse> chat(AiRequest request) {
            calls++;
            receivedRequest = request;
            return result;
        }
    }
}
