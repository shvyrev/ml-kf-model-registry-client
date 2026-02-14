package io.cx.model_registry.service.idempotency;

import java.time.Instant;

public record IdempotencyEntry(
        String key,
        String operation,
        WorkflowExecutionState state,
        String responseJson,
        String errorMessage,
        Instant updatedAt
) {

    public static IdempotencyEntry inProgress(String key, String operation, Instant now) {
        return new IdempotencyEntry(key, operation, WorkflowExecutionState.IN_PROGRESS, null, null, now);
    }

    public static IdempotencyEntry succeeded(String key, String operation, String responseJson, Instant now) {
        return new IdempotencyEntry(key, operation, WorkflowExecutionState.SUCCEEDED, responseJson, null, now);
    }

    public static IdempotencyEntry failed(String key, String operation, String errorMessage, Instant now) {
        return new IdempotencyEntry(key, operation, WorkflowExecutionState.FAILED, null, errorMessage, now);
    }
}

