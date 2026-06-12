package assistant.ai;

import assistant.common.OperationResult;
import assistant.summary.LocalContext;

public interface ContextProvider {
    OperationResult<LocalContext> getLocalContext();
}
