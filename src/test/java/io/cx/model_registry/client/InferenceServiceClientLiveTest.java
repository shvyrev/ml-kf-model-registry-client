package io.cx.model_registry.client;

import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceList;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceState;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceUpdate;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
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
class InferenceServiceClientLiveTest {

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

    @Test
    void inferenceServiceClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());

        RegisteredModelCreate modelCreate = new RegisteredModelCreate();
        modelCreate.name("is-live-model-" + suffix);
        modelCreate.externalId("is-live-model-ext-" + suffix);
        modelCreate.description("is-live-model-created-" + suffix);

        Response modelResponse = modelClient.createRegisteredModel(modelCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(modelResponse.getStatus()).isBetween(200, 201);
        RegisteredModel registeredModel = modelResponse.readEntity(RegisteredModel.class);
        assertThat(registeredModel).isNotNull();
        assertThat(registeredModel.id()).isNotBlank();

        ModelVersionCreate versionCreate = new ModelVersionCreate();
        versionCreate.name("is-live-version-" + suffix);
        versionCreate.externalId("is-live-version-ext-" + suffix);
        versionCreate.description("is-live-version-created-" + suffix);
        versionCreate.registeredModelId(registeredModel.id());

        ModelVersion modelVersion = versionClient.createModelVersion(versionCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(modelVersion).isNotNull();
        assertThat(modelVersion.id()).isNotBlank();
        assertThat(modelVersion.registeredModelId()).isEqualTo(registeredModel.id());

        ServingEnvironmentCreate envCreate = new ServingEnvironmentCreate();
        envCreate.name("is-live-env-" + suffix);
        envCreate.externalId("is-live-env-ext-" + suffix);
        envCreate.description("is-live-env-created-" + suffix);

        ServingEnvironment servingEnvironment = servingEnvironmentClient.createServingEnvironment(envCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(servingEnvironment).isNotNull();
        assertThat(servingEnvironment.id()).isNotBlank();

        InferenceServiceCreate serviceCreate = new InferenceServiceCreate();
        serviceCreate.name("is-live-service-" + suffix);
        serviceCreate.externalId("is-live-service-ext-" + suffix);
        serviceCreate.description("is-live-service-created-" + suffix);
        serviceCreate.registeredModelId(registeredModel.id());
        serviceCreate.servingEnvironmentId(servingEnvironment.id());
        serviceCreate.modelVersionId(modelVersion.id());
        serviceCreate.runtime("kserve");
        serviceCreate.desiredState(InferenceServiceState.DEPLOYED);

        InferenceService created = inferenceServiceClient.createInferenceService(serviceCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotBlank();
        assertThat(created.registeredModelId()).isEqualTo(registeredModel.id());
        assertThat(created.servingEnvironmentId()).isEqualTo(servingEnvironment.id());

        InferenceService loaded = inferenceServiceClient.getInferenceService(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loaded).isNotNull();
        assertThat(loaded.id()).isEqualTo(created.id());

        InferenceServiceUpdate update = new InferenceServiceUpdate();
        update.description("is-live-service-updated-" + suffix);
        update.runtime("kserve-updated");
        update.desiredState(InferenceServiceState.DEPLOYED);

        InferenceService updated = inferenceServiceClient.updateInferenceService(created.id(), update)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.description()).isEqualTo("is-live-service-updated-" + suffix);

        InferenceServiceList list = inferenceServiceClient.getInferenceServices(
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(list).isNotNull();
        assertThat(list.items()).isNotNull();
        assertThat(list.items().stream().map(InferenceService::id)).contains(created.id());

        RegisteredModel linkedModel = inferenceServiceClient.getInferenceServiceModel(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(linkedModel).isNotNull();
        assertThat(linkedModel.id()).isEqualTo(registeredModel.id());

        ModelVersion linkedVersion = inferenceServiceClient.getInferenceServiceVersion(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(linkedVersion).isNotNull();
        assertThat(linkedVersion.id()).isEqualTo(modelVersion.id());
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
