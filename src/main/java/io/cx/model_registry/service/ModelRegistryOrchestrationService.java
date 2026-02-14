package io.cx.model_registry.service;

import io.cx.model_registry.client.InferenceServiceClient;
import io.cx.model_registry.client.ModelClient;
import io.cx.model_registry.client.ServeModelClient;
import io.cx.model_registry.client.ServingEnvironmentClient;
import io.cx.model_registry.client.VersionClient;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.workflows.DeployModelVersionRequest;
import io.cx.model_registry.dto.workflows.DeployModelVersionResult;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateRequest;
import io.cx.model_registry.dto.workflows.ModelWithVersionCreateResult;
import io.cx.model_registry.exceptions.RestClientException;
import io.cx.model_registry.service.idempotency.WorkflowIdempotencyService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.function.Supplier;

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

    @ConfigProperty(name = "orchestration.retry.max-retries", defaultValue = "3")
    int retryMaxRetries;

    @ConfigProperty(name = "orchestration.retry.initial-backoff", defaultValue = "PT0.2S")
    Duration retryInitialBackoff;

    @ConfigProperty(name = "orchestration.retry.max-backoff", defaultValue = "PT3S")
    Duration retryMaxBackoff;

    public Uni<ModelWithVersionCreateResult> createModelWithVersion(ModelWithVersionCreateRequest request) {
        validateCreateModelWithVersionRequest(request);
        return createModelWithVersionInternal(request);
    }

    public Uni<ModelWithVersionCreateResult> createModelWithVersionIdempotent(ModelWithVersionCreateRequest request) {
        validateCreateModelWithVersionRequest(request);
        String key = resolveModelWithVersionKey(request);
        return idempotencyService.execute(
                "createModelWithVersion",
                key,
                request,
                ModelWithVersionCreateResult.class,
                () -> createModelWithVersionInternal(request)
        );
    }

    public Uni<DeployModelVersionResult> deployModelVersion(DeployModelVersionRequest request) {
        validateDeployModelVersionRequest(request);
        return deployModelVersionInternal(request);
    }

    public Uni<DeployModelVersionResult> deployModelVersionIdempotent(DeployModelVersionRequest request) {
        validateDeployModelVersionRequest(request);
        String key = resolveDeployModelVersionKey(request);
        return idempotencyService.execute(
                "deployModelVersion",
                key,
                request,
                DeployModelVersionResult.class,
                () -> deployModelVersionInternal(request)
        );
    }

    private Uni<ModelWithVersionCreateResult> createModelWithVersionInternal(ModelWithVersionCreateRequest request) {
        return withRetry(() -> modelClient.createRegisteredModel(request.model())
                .map(response -> response.readEntity(RegisteredModel.class)))
                .chain(model -> {
                    request.version().registeredModelId(model.id());
                    return withRetry(() -> versionClient.createModelVersion(request.version()))
                            .map(version -> new ModelWithVersionCreateResult(model, version));
                });
    }

    private Uni<DeployModelVersionResult> deployModelVersionInternal(DeployModelVersionRequest request) {
        return withRetry(() -> servingEnvironmentClient.createServingEnvironment(request.servingEnvironment()))
                .chain(servingEnvironment -> {
                    request.inferenceService().servingEnvironmentId(servingEnvironment.id());

                    return withRetry(() -> inferenceServiceClient.createInferenceService(request.inferenceService()))
                            .chain(inferenceService ->
                                    withRetry(() -> serveModelClient.createInferenceServiceServe(
                                            inferenceService.id(), request.serve()))
                                            .map(serveModel -> new DeployModelVersionResult(
                                                    servingEnvironment,
                                                    inferenceService,
                                                    serveModel
                                            )));
                });
    }

    private void validateCreateModelWithVersionRequest(ModelWithVersionCreateRequest request) {
        if (request == null || request.model() == null || request.version() == null) {
            throw new BadRequestException("Both 'model' and 'version' must be provided");
        }
    }

    private void validateDeployModelVersionRequest(DeployModelVersionRequest request) {
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
    }

    private <T> Uni<T> withRetry(Supplier<Uni<T>> actionSupplier) {
        return actionSupplier.get()
                .onFailure(this::isRetryable)
                .retry()
                .withBackOff(retryInitialBackoff, retryMaxBackoff)
                .atMost(retryMaxRetries);
    }

    private boolean isRetryable(Throwable throwable) {
        Throwable root = rootCause(throwable);
        if (root instanceof RestClientException restClientException) {
            int status = restClientException.status();
            return status == 0 || status == 429 || status >= 500;
        }
        return root instanceof ConnectException
                || root instanceof SocketTimeoutException
                || root instanceof ProcessingException;
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current == null ? throwable : current;
    }

    private String resolveModelWithVersionKey(ModelWithVersionCreateRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            return request.idempotencyKey();
        }
        String modelStable = firstNonBlank(request.model().externalId(), request.model().name());
        String versionStable = firstNonBlank(request.version().externalId(), request.version().name());
        return "createModelWithVersion:" + modelStable + ":" + versionStable;
    }

    private String resolveDeployModelVersionKey(DeployModelVersionRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            return request.idempotencyKey();
        }

        String envStable = firstNonBlank(
                request.servingEnvironment().externalId(),
                request.servingEnvironment().name()
        );
        String inferenceStable = firstNonBlank(
                request.inferenceService().externalId(),
                request.inferenceService().name()
        );
        String serveStable = firstNonBlank(
                request.serve().externalId(),
                request.serve().name(),
                request.serve().modelVersionId()
        );
        return "deployModelVersion:" + envStable + ":" + inferenceStable + ":" + serveStable;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "na";
    }
}
