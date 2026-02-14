package io.cx.model_registry.resource;

import io.cx.model_registry.dto.workflows.DeployModelVersionRequest;
import io.cx.model_registry.dto.workflows.DeployModelVersionResult;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateRequest;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateResult;
import io.cx.model_registry.service.ModelRegistryOrchestrationService;
import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEventMapping;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ModelRegistryEventFunctions {

    @Inject
    ModelRegistryOrchestrationService orchestrationService;

    @Funq("model-with-version-workflow")
    @CloudEventMapping(
            trigger = "io.cx.model_registry.model-with-version.requested",
            responseType = "io.cx.model_registry.model-with-version.completed"
    )
    public Uni<ModelWithVersionCreateResult> handleModelWithVersionRequested(
            @Valid @NotNull(message = "Event data must be provided") ModelWithVersionCreateRequest request
    ) {
        return orchestrationService.createModelWithVersionIdempotent(request);
    }

    @Funq("deploy-model-version-workflow")
    @CloudEventMapping(
            trigger = "io.cx.model_registry.deploy-model-version.requested",
            responseType = "io.cx.model_registry.deploy-model-version.completed"
    )
    public Uni<DeployModelVersionResult> handleDeployModelVersionRequested(
            @Valid @NotNull(message = "Event data must be provided") DeployModelVersionRequest request
    ) {
        return orchestrationService.deployModelVersionIdempotent(request);
    }
}
