package io.cx.model_registry.service;

import io.cx.model_registry.client.*;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.workflows.DeployModelVersionRequest;
import io.cx.model_registry.dto.workflows.DeployModelVersionResult;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateRequest;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateResult;
import io.cx.model_registry.service.idempotency.IdempotencyKeyResolver;
import io.cx.model_registry.service.idempotency.WorkflowIdempotencyService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    @Inject
    WorkflowIdempotencyService idempotencyService;

    @Inject
    IdempotencyKeyResolver keyResolver;

    public Uni<ModelWithVersionCreateResult> createModelWithVersion(
            @Valid @NotNull(message = "Request body must be provided") ModelWithVersionCreateRequest request
    ) {
        return createModelWithVersionInternal(request);
    }

    public Uni<ModelWithVersionCreateResult> createModelWithVersionIdempotent(
            @Valid @NotNull(message = "Request body must be provided") ModelWithVersionCreateRequest request
    ) {
        return keyResolver.resolve(request)
                .chain(key -> idempotencyService.execute(
                        "createModelWithVersion",
                        key,
                        ModelWithVersionCreateResult.class,
                        () -> createModelWithVersionInternal(request)
                ));
    }

    public Uni<DeployModelVersionResult> deployModelVersion(
            @Valid @NotNull(message = "Request body must be provided") DeployModelVersionRequest request
    ) {
        return deployModelVersionInternal(request);
    }

    public Uni<DeployModelVersionResult> deployModelVersionIdempotent(
            @Valid @NotNull(message = "Request body must be provided") DeployModelVersionRequest request
    ) {
        return keyResolver.resolve(request)
                .chain(key -> idempotencyService.execute(
                        "deployModelVersion",
                        key,
                        DeployModelVersionResult.class,
                        () -> deployModelVersionInternal(request)
                ));
    }

    private Uni<ModelWithVersionCreateResult> createModelWithVersionInternal(ModelWithVersionCreateRequest request) {
        return modelClient.createRegisteredModel(request.model())
                .map(response -> response.readEntity(RegisteredModel.class))
                .chain(model -> {
                    request.version().registeredModelId(model.id());
                    return versionClient.createModelVersion(request.version())
                            .map(version -> new ModelWithVersionCreateResult(model, version));
                });
    }

    private Uni<DeployModelVersionResult> deployModelVersionInternal(DeployModelVersionRequest request) {
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
