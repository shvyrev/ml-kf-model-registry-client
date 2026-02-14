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

public class ModelRegistryEventFunctions {

    @Inject
    ModelRegistryOrchestrationService orchestrationService;

    @Funq("model-with-version-workflow")
    @CloudEventMapping(
            trigger = "io.cx.model_registry.model-with-version.requested",
            responseType = "io.cx.model_registry.model-with-version.completed"
    )
    public Uni<ModelWithVersionCreateResult> handleModelWithVersionRequested(ModelWithVersionCreateRequest request) {
        return orchestrationService.createModelWithVersion(request);
    }

    @Funq("deploy-model-version-workflow")
    @CloudEventMapping(
            trigger = "io.cx.model_registry.deploy-model-version.requested",
            responseType = "io.cx.model_registry.deploy-model-version.completed"
    )
    public Uni<DeployModelVersionResult> handleDeployModelVersionRequested(DeployModelVersionRequest request) {
        return orchestrationService.deployModelVersion(request);
    }
}
