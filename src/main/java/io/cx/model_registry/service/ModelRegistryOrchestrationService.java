package io.cx.model_registry.service;

import io.cx.model_registry.client.InferenceServiceClient;
import io.cx.model_registry.client.ModelClient;
import io.cx.model_registry.client.ServeModelClient;
import io.cx.model_registry.client.ServingEnvironmentClient;
import io.cx.model_registry.client.VersionClient;
import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.servemodel.ServeModel;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironment;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.cx.model_registry.dto.workflows.DeployModelVersionRequest;
import io.cx.model_registry.dto.workflows.DeployModelVersionResult;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateRequest;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ModelRegistryOrchestrationService {

    @Inject
    @RestClient
    ModelClient modelClient;

    @Inject
    @RestClient
    VersionClient versionClient;

    @Inject
    @RestClient
    ServingEnvironmentClient servingEnvironmentClient;

    @Inject
    @RestClient
    InferenceServiceClient inferenceServiceClient;

    @Inject
    @RestClient
    ServeModelClient serveModelClient;

    public Uni<ModelWithVersionCreateResult> createModelWithVersion(ModelWithVersionCreateRequest request) {
        if (request == null || request.model() == null || request.version() == null) {
            throw new BadRequestException("Both 'model' and 'version' must be provided");
        }

        return modelClient.createRegisteredModel(request.model())
                .map(response -> response.readEntity(RegisteredModel.class))
                .chain(model -> {
                    request.version().registeredModelId(model.id());
                    return versionClient.createModelVersion(request.version())
                            .map(version -> new ModelWithVersionCreateResult(model, version));
                });
    }

    public Uni<DeployModelVersionResult> deployModelVersion(DeployModelVersionRequest request) {
        if (request == null
                || request.servingEnvironment() == null
                || request.inferenceService() == null
                || request.serve() == null) {
            throw new BadRequestException(
                    "'servingEnvironment', 'inferenceService' and 'serve' must be provided");
        }

        if (request.serve().modelVersionId() == null || request.serve().modelVersionId().isBlank()) {
            throw new BadRequestException("'serve.modelVersionId' must be provided");
        }

        return servingEnvironmentClient.createServingEnvironment(request.servingEnvironment())
                .chain(servingEnvironment -> {
                    request.inferenceService().servingEnvironmentId(servingEnvironment.id());

                    return inferenceServiceClient.createInferenceService(request.inferenceService())
                            .chain(inferenceService ->
                                    serveModelClient.createInferenceServiceServe(inferenceService.id(), request.serve())
                                            .map(serveModel -> new DeployModelVersionResult(
                                                    servingEnvironment,
                                                    inferenceService,
                                                    serveModel
                                            )));
                });
    }
}
