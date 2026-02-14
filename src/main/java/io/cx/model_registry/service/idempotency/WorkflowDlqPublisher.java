package io.cx.model_registry.service.idempotency;

import io.smallrye.mutiny.Uni;

public interface WorkflowDlqPublisher {
    Uni<Void> publish(WorkflowDlqEvent event);
}

