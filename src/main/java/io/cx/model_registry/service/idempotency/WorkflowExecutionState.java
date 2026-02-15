package io.cx.model_registry.service.idempotency;

import org.infinispan.protostream.annotations.Proto;

@Proto
public enum WorkflowExecutionState {
    IN_PROGRESS,
    SUCCEEDED,
    FAILED
}
