package io.cx.model_registry.resource;

import io.cx.model_registry.dto.workflows.DeployModelVersionRequest;
import io.cx.model_registry.dto.workflows.DeployModelVersionResult;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateRequest;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateResult;
import io.cx.model_registry.service.ModelRegistryOrchestrationService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/workflows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelRegistryWorkflowResource {

    @Inject
    ModelRegistryOrchestrationService orchestrationService;

    @POST
    @Path("/model-with-version")
    public Uni<ModelWithVersionCreateResult> createModelWithVersion(ModelWithVersionCreateRequest request) {
        return orchestrationService.createModelWithVersion(request);
    }

    @POST
    @Path("/deploy-model-version")
    public Uni<DeployModelVersionResult> deployModelVersion(DeployModelVersionRequest request) {
        return orchestrationService.deployModelVersion(request);
    }
}
