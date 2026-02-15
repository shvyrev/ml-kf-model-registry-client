package io.cx.model_registry.service.idempotency;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(
        includeClasses = {
                IdempotencyEntry.class,
                WorkflowExecutionState.class
        },
        schemaPackageName = "io.cx.model_registry.idempotency",
        schemaFileName = "workflow-idempotency.proto"
)
public interface WorkflowIdempotencySchema extends GeneratedSchema {
}
