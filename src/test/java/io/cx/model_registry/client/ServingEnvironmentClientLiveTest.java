package io.cx.model_registry.client;

import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceList;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceState;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironment;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentCreate;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentList;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentUpdate;
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
class ServingEnvironmentClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    ServingEnvironmentClient servingEnvironmentClient;

    @Inject
    @RestClient
    ModelClient modelClient;

    @Inject
    @RestClient
    VersionClient versionClient;

    @Test
    void servingEnvironmentClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());
        String envName = "se-live-env-" + suffix;
        String envExternalId = "se-live-env-ext-" + suffix;
        String updatedDescription = "se-live-env-updated-" + suffix;

        ServingEnvironmentCreate envCreate = new ServingEnvironmentCreate();
        envCreate.name(envName);
        envCreate.externalId(envExternalId);
        envCreate.description("se-live-env-created-" + suffix);

        ServingEnvironment createdEnv = servingEnvironmentClient.createServingEnvironment(envCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createdEnv).isNotNull();
        assertThat(createdEnv.id()).isNotBlank();
        assertThat(createdEnv.name()).isEqualTo(envName);

        ServingEnvironment loadedEnv = servingEnvironmentClient.getServingEnvironment(createdEnv.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loadedEnv).isNotNull();
        assertThat(loadedEnv.id()).isEqualTo(createdEnv.id());

        ServingEnvironmentUpdate envUpdate = new ServingEnvironmentUpdate();
        envUpdate.description(updatedDescription);

        ServingEnvironment updatedEnv = servingEnvironmentClient.updateServingEnvironment(createdEnv.id(), envUpdate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updatedEnv).isNotNull();
        assertThat(updatedEnv.id()).isEqualTo(createdEnv.id());
        assertThat(updatedEnv.description()).isEqualTo(updatedDescription);

        ServingEnvironmentList envList = servingEnvironmentClient.getServingEnvironments(
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(envList).isNotNull();
        assertThat(envList.items()).isNotNull();
        assertThat(envList.items().stream().map(ServingEnvironment::id)).contains(createdEnv.id());

        RegisteredModelCreate modelCreate = new RegisteredModelCreate();
        modelCreate.name("se-live-model-" + suffix);
        modelCreate.externalId("se-live-model-ext-" + suffix);
        modelCreate.description("se-live-model-created-" + suffix);
        Response modelResponse = modelClient.createRegisteredModel(modelCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        RegisteredModel model = modelResponse.readEntity(RegisteredModel.class);
        assertThat(model).isNotNull();
        assertThat(model.id()).isNotBlank();

        ModelVersionCreate versionCreate = new ModelVersionCreate();
        versionCreate.name("se-live-version-" + suffix);
        versionCreate.externalId("se-live-version-ext-" + suffix);
        versionCreate.description("se-live-version-created-" + suffix);
        versionCreate.registeredModelId(model.id());
        ModelVersion version = versionClient.createModelVersion(versionCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(version).isNotNull();
        assertThat(version.id()).isNotBlank();

        InferenceServiceList beforeServices = servingEnvironmentClient.getEnvironmentInferenceServices(
                        createdEnv.id(),
                        null,
                        null,
                        null,
                        100,
                        "ID",
                        "ASC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(beforeServices).isNotNull();
        assertThat(beforeServices.items()).isNotNull();

        InferenceServiceCreate serviceCreate = new InferenceServiceCreate();
        serviceCreate.name("se-live-is-" + suffix);
        serviceCreate.externalId("se-live-is-ext-" + suffix);
        serviceCreate.description("se-live-is-created-" + suffix);
        serviceCreate.registeredModelId(model.id());
        serviceCreate.servingEnvironmentId(createdEnv.id());
        serviceCreate.modelVersionId(version.id());
        serviceCreate.runtime("kserve");
        serviceCreate.desiredState(InferenceServiceState.DEPLOYED);

        InferenceService createdService = servingEnvironmentClient.createEnvironmentInferenceService(createdEnv.id(), serviceCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createdService).isNotNull();
        assertThat(createdService.id()).isNotBlank();
        assertThat(createdService.servingEnvironmentId()).isEqualTo(createdEnv.id());

        InferenceServiceList afterServices = servingEnvironmentClient.getEnvironmentInferenceServices(
                        createdEnv.id(),
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
        assertThat(afterServices).isNotNull();
        assertThat(afterServices.items()).isNotNull();
        assertThat(afterServices.items().stream().map(InferenceService::id)).contains(createdService.id());
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/serving_environments?pageSize=1")
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
