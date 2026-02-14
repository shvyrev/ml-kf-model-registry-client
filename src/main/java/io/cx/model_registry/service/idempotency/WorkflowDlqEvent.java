package io.cx.model_registry.service.idempotency;

import java.time.Instant;

public record WorkflowDlqEvent(
        String key,
        String operation,
        String errorType,
        String errorMessage,
        String requestJson,
        Instant timestamp
) {
}

