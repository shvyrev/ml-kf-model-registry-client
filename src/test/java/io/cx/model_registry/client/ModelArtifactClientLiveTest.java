package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.ModelArtifact;
import io.cx.model_registry.dto.artifacts.ModelArtifactCreate;
import io.cx.model_registry.dto.artifacts.ModelArtifactList;
import io.cx.model_registry.dto.artifacts.ModelArtifactUpdate;
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
class ModelArtifactClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    ModelArtifactClient modelArtifactClient;

    @Test
    void modelArtifactClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());
        String name = "model-artifact-live-test-" + suffix;
        String externalId = "model-artifact-live-ext-" + suffix;
        String updatedDescription = "model-artifact-updated-" + suffix;

        ModelArtifactCreate create = new ModelArtifactCreate();
        create.name(name);
        create.externalId(externalId);
        create.description("model-artifact-created-" + suffix);
        create.uri("s3://model-artifacts/" + suffix);

        Response createResponse = modelArtifactClient.createModelArtifact(create)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createResponse.getStatus()).isBetween(200, 201);

        ModelArtifact created = createResponse.readEntity(ModelArtifact.class);
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotBlank();
        assertThat(created.name()).isEqualTo(name);

        ModelArtifact loaded = modelArtifactClient.getModelArtifact(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loaded).isNotNull();
        assertThat(loaded.id()).isEqualTo(created.id());
        assertThat(loaded.externalId()).isEqualTo(externalId);

        ModelArtifactUpdate update = new ModelArtifactUpdate();
        update.setDescription(updatedDescription);
        update.setUri("s3://model-artifacts/" + suffix + "/updated");

        ModelArtifact updated = modelArtifactClient.updateModelArtifact(created.id(), update)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.description()).isEqualTo(updatedDescription);

        ModelArtifactList list = modelArtifactClient.getModelArtifacts(
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
        assertThat(list.items().stream().map(ModelArtifact::id)).contains(created.id());
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/model_artifacts?pageSize=1")
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
