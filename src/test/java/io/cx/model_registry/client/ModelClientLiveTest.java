package io.cx.model_registry.client;

import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.models.RegisteredModelList;
import io.cx.model_registry.dto.models.RegisteredModelState;
import io.cx.model_registry.dto.models.RegisteredModelUpdate;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.cx.model_registry.dto.versions.ModelVersionList;
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
class ModelClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    ModelClient modelClient;

    @Test
    void modelClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());
        String name = "model-live-test-" + suffix;
        String externalId = "model-live-ext-" + suffix;
        String updatedDescription = "model-updated-" + suffix;

        RegisteredModelCreate create = new RegisteredModelCreate();
        create.name(name);
        create.externalId(externalId);
        create.description("model-created-" + suffix);
        create.owner("qa-live");

        Response createResponse = modelClient.createRegisteredModel(create)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createResponse.getStatus()).isBetween(200, 201);

        RegisteredModel created = createResponse.readEntity(RegisteredModel.class);
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotBlank();
        assertThat(created.name()).isEqualTo(name);

        RegisteredModel found = modelClient.findRegisteredModel(null, externalId)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(created.id());

        RegisteredModel loaded = modelClient.getRegisteredModel(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loaded).isNotNull();
        assertThat(loaded.id()).isEqualTo(created.id());

        RegisteredModelUpdate update = new RegisteredModelUpdate();
        update.description(updatedDescription);
        update.owner("qa-live-updated");
        update.state(RegisteredModelState.LIVE);

        RegisteredModel updated = modelClient.updateRegisteredModel(created.id(), update)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.description()).isEqualTo(updatedDescription);

        RegisteredModelList list = modelClient.getRegisteredModels(
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
        assertThat(list.items().stream().map(RegisteredModel::id)).contains(created.id());

        ModelVersion versionCreate = new ModelVersion();
        versionCreate.name("model-version-live-" + suffix);
        versionCreate.externalId("model-version-live-ext-" + suffix);
        versionCreate.description("model-version-created-" + suffix);
        versionCreate.registeredModelId(created.id());

        ModelVersion createdVersion = modelClient.createRegisteredModelVersion(created.id(), versionCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createdVersion).isNotNull();
        assertThat(createdVersion.id()).isNotBlank();
        assertThat(createdVersion.registeredModelId()).isEqualTo(created.id());

        Response versionsResponse = modelClient.getRegisteredModelVersions(
                        created.id(),
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
        assertThat(versionsResponse.getStatus()).isEqualTo(200);

        ModelVersionList versions = versionsResponse.readEntity(ModelVersionList.class);
        assertThat(versions).isNotNull();
        assertThat(versions.items()).isNotNull();
        assertThat(versions.items().stream().map(ModelVersion::id)).contains(createdVersion.id());
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/registered_models?pageSize=1")
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
