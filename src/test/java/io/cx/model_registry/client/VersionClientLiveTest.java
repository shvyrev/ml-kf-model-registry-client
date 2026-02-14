package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.Artifact;
import io.cx.model_registry.dto.artifacts.ArtifactList;
import io.cx.model_registry.dto.artifacts.ModelArtifact;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.models.RegisteredModelCreate;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.cx.model_registry.dto.versions.ModelVersionCreate;
import io.cx.model_registry.dto.versions.ModelVersionList;
import io.cx.model_registry.dto.versions.ModelVersionState;
import io.cx.model_registry.dto.versions.ModelVersionUpdate;
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
class VersionClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    ModelClient modelClient;

    @Inject
    @RestClient
    VersionClient versionClient;

    @Test
    void versionClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());

        RegisteredModelCreate modelCreate = new RegisteredModelCreate();
        modelCreate.name("version-live-model-" + suffix);
        modelCreate.externalId("version-live-model-ext-" + suffix);
        modelCreate.description("version-live-model-created-" + suffix);
        Response modelResponse = modelClient.createRegisteredModel(modelCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        RegisteredModel model = modelResponse.readEntity(RegisteredModel.class);
        assertThat(model).isNotNull();
        assertThat(model.id()).isNotBlank();

        ModelVersionCreate create = new ModelVersionCreate();
        create.name("version-live-" + suffix);
        create.externalId("version-live-ext-" + suffix);
        create.description("version-live-created-" + suffix);
        create.registeredModelId(model.id());

        ModelVersion created = versionClient.createModelVersion(create)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotBlank();
        assertThat(created.registeredModelId()).isEqualTo(model.id());

        ModelVersion loaded = versionClient.getModelVersion(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loaded).isNotNull();
        assertThat(loaded.id()).isEqualTo(created.id());

        ModelVersionUpdate update = new ModelVersionUpdate();
        update.description("version-live-updated-" + suffix);
        update.author("qa-live");
        update.state(ModelVersionState.LIVE);

        ModelVersion updated = versionClient.updateModelVersion(created.id(), update)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.description()).isEqualTo("version-live-updated-" + suffix);

        ModelVersionList versions = versionClient.getModelVersions(
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(versions).isNotNull();
        assertThat(versions.items()).isNotNull();
        assertThat(versions.items().stream().map(ModelVersion::id)).contains(created.id());

        ModelVersionList listAlias = versionClient.list()
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(listAlias).isNotNull();
        assertThat(listAlias.items()).isNotNull();
        assertThat(listAlias.items().stream().map(ModelVersion::id)).contains(created.id());

        ArtifactList artifactsBefore = versionClient.getModelVersionArtifacts(
                        created.id(),
                        null,
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
        assertThat(artifactsBefore).isNotNull();
        assertThat(artifactsBefore.items()).isNotNull();

        ModelArtifact artifact = new ModelArtifact();
        artifact.name("version-artifact-live-" + suffix);
        artifact.externalId("version-artifact-live-ext-" + suffix);
        artifact.description("version-artifact-created-" + suffix);
        artifact.uri("s3://version-artifacts/" + suffix);

        Artifact upserted = versionClient.upsertModelVersionArtifact(created.id(), artifact)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(upserted).isNotNull();
        assertThat(upserted.id()).isNotBlank();

        ArtifactList artifactsAfter = versionClient.getModelVersionArtifacts(
                        created.id(),
                        null,
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
        assertThat(artifactsAfter).isNotNull();
        assertThat(artifactsAfter.items()).isNotNull();
        assertThat(artifactsAfter.items().stream().map(Artifact::id)).contains(upserted.id());
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/model_versions?pageSize=1")
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
