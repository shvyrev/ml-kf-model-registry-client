package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.Artifact;
import io.cx.model_registry.dto.artifacts.ArtifactList;
import io.cx.model_registry.dto.artifacts.ModelArtifact;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@QuarkusTest
class ArtifactClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    ArtifactClient artifactClient;

    @Test
    void artifactClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());
        String name = "artifact-live-test-" + suffix;
        String externalId = "artifact-live-test-ext-" + suffix;
        String updatedDescription = "updated-description-" + suffix;

        ModelArtifact createRequest = new ModelArtifact();
        createRequest.uri("s3://test-bucket/" + suffix);
        createRequest.name(name);
        createRequest.externalId(externalId);
        createRequest.description("created-description-" + suffix);

        Response createResponse = artifactClient.createArtifact(createRequest)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createResponse.getStatus()).isBetween(200, 201);

        Artifact created = createResponse.readEntity(Artifact.class);
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotBlank();
        assertThat(created.name()).isEqualTo(name);

        Artifact loaded = artifactClient.getArtifact(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loaded).isNotNull();
        assertThat(loaded.id()).isEqualTo(created.id());
        assertThat(loaded.externalId()).isEqualTo(externalId);

        ModelArtifact updateRequest = new ModelArtifact();
        updateRequest.uri("s3://test-bucket/" + suffix + "/updated");
        updateRequest.description(updatedDescription);
        Artifact updated = artifactClient.updateArtifact(created.id(), updateRequest)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.description()).isEqualTo(updatedDescription);

        ArtifactList listResult = artifactClient.getArtifacts(
                        null,
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));

        log.info("$ "+ "artifactClient_shouldSupportAllMethodsAgainstLiveModelRegistry() called : {}", listResult.items());

        assertThat(listResult).isNotNull();
        assertThat(listResult.items()).isNotNull();
        assertThat(listResult.items().stream().map(Artifact::id)).contains(created.id());
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/artifacts?pageSize=1")
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
