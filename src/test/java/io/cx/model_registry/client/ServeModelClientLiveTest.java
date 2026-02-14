package io.cx.model_registry.client;

import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceState;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.servemodel.ExecutionState;
import io.cx.model_registry.dto.servemodel.ServeModel;
import io.cx.model_registry.dto.servemodel.ServeModelCreate;
import io.cx.model_registry.dto.servemodel.ServeModelList;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironment;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentCreate;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.cx.model_registry.dto.versions.ModelVersionCreate;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ServeModelClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

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

    @Test
    void serveModelClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());

        RegisteredModelCreate modelCreate = new RegisteredModelCreate();
        modelCreate.name("serve-live-model-" + suffix);
        modelCreate.externalId("serve-live-model-ext-" + suffix);
        modelCreate.description("serve-live-model-created-" + suffix);
        Response modelResponse = modelClient.createRegisteredModel(modelCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        RegisteredModel registeredModel = modelResponse.readEntity(RegisteredModel.class);
        assertThat(registeredModel).isNotNull();
        assertThat(registeredModel.id()).isNotBlank();

        ModelVersionCreate versionCreate = new ModelVersionCreate();
        versionCreate.name("serve-live-version-" + suffix);
        versionCreate.externalId("serve-live-version-ext-" + suffix);
        versionCreate.description("serve-live-version-created-" + suffix);
        versionCreate.registeredModelId(registeredModel.id());
        ModelVersion modelVersion = versionClient.createModelVersion(versionCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(modelVersion).isNotNull();
        assertThat(modelVersion.id()).isNotBlank();

        ServingEnvironmentCreate envCreate = new ServingEnvironmentCreate();
        envCreate.name("serve-live-env-" + suffix);
        envCreate.externalId("serve-live-env-ext-" + suffix);
        envCreate.description("serve-live-env-created-" + suffix);
        ServingEnvironment servingEnvironment = servingEnvironmentClient.createServingEnvironment(envCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(servingEnvironment).isNotNull();
        assertThat(servingEnvironment.id()).isNotBlank();

        InferenceServiceCreate serviceCreate = new InferenceServiceCreate();
        serviceCreate.name("serve-live-service-" + suffix);
        serviceCreate.externalId("serve-live-service-ext-" + suffix);
        serviceCreate.description("serve-live-service-created-" + suffix);
        serviceCreate.registeredModelId(registeredModel.id());
        serviceCreate.servingEnvironmentId(servingEnvironment.id());
        serviceCreate.modelVersionId(modelVersion.id());
        serviceCreate.runtime("kserve");
        serviceCreate.desiredState(InferenceServiceState.DEPLOYED);
        InferenceService inferenceService = inferenceServiceClient.createInferenceService(serviceCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(inferenceService).isNotNull();
        assertThat(inferenceService.id()).isNotBlank();

        ServeModelCreate serveCreate = new ServeModelCreate();
        serveCreate.name("serve-action-" + suffix);
        serveCreate.externalId("serve-action-ext-" + suffix);
        serveCreate.description("serve-action-created-" + suffix);
        serveCreate.modelVersionId(modelVersion.id());
        serveCreate.lastKnownState(ExecutionState.RUNNING);

        ServeModel createdServe = serveModelClient.createInferenceServiceServe(inferenceService.id(), serveCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createdServe).isNotNull();
        assertThat(createdServe.id()).isNotBlank();
        assertThat(createdServe.modelVersionId()).isEqualTo(modelVersion.id());

        ServeModelList serves = serveModelClient.getInferenceServiceServes(
                        inferenceService.id(),
                        null,
                        null,
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(serves).isNotNull();
        assertThat(serves.items()).isNotNull();
        assertThat(serves.items().stream().map(ServeModel::id)).contains(createdServe.id());
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/inference_services?pageSize=1")
                    .toURL()
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);

            int code = connection.getResponseCode();
            return code >= 200 && code < 500;
        } catch (IOException e) {
            return false;
        }
    }
}
