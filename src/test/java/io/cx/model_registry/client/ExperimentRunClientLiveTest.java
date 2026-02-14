package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.Artifact;
import io.cx.model_registry.dto.artifacts.ArtifactList;
import io.cx.model_registry.dto.artifacts.MetricList;
import io.cx.model_registry.dto.artifacts.ModelArtifact;
import io.cx.model_registry.dto.experiments.Experiment;
import io.cx.model_registry.dto.experiments.ExperimentCreate;
import io.cx.model_registry.dto.experimentruns.ExperimentRun;
import io.cx.model_registry.dto.experimentruns.ExperimentRunCreate;
import io.cx.model_registry.dto.experimentruns.ExperimentRunList;
import io.cx.model_registry.dto.experimentruns.ExperimentRunState;
import io.cx.model_registry.dto.experimentruns.ExperimentRunStatus;
import io.cx.model_registry.dto.experimentruns.ExperimentRunUpdate;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ExperimentRunClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    ExperimentClient experimentClient;

    @Inject
    @RestClient
    ExperimentRunClient experimentRunClient;

    @Test
    void experimentRunClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());

        ExperimentCreate experimentCreate = new ExperimentCreate();
        experimentCreate.name("run-parent-exp-" + suffix);
        experimentCreate.externalId("run-parent-exp-ext-" + suffix);
        experimentCreate.description("run-parent-exp-created-" + suffix);

        Experiment parentExperiment = experimentClient.createExperiment(experimentCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(parentExperiment).isNotNull();
        assertThat(parentExperiment.id()).isNotBlank();

        ExperimentRunCreate runCreate = new ExperimentRunCreate();
        runCreate.experimentId(parentExperiment.id());
        runCreate.name("run-live-test-" + suffix);
        runCreate.externalId("run-live-ext-" + suffix);
        runCreate.description("run-created-" + suffix);
        runCreate.owner("qa-live");
        runCreate.status(ExperimentRunStatus.RUNNING);
        runCreate.state(ExperimentRunState.LIVE);

        ExperimentRun createdRun = experimentRunClient.createExperimentRun(runCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createdRun).isNotNull();
        assertThat(createdRun.id()).isNotBlank();
        assertThat(createdRun.experimentId()).isEqualTo(parentExperiment.id());

        ExperimentRun loadedRun = experimentRunClient.getExperimentRun(createdRun.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loadedRun).isNotNull();
        assertThat(loadedRun.id()).isEqualTo(createdRun.id());

        ExperimentRunUpdate runUpdate = new ExperimentRunUpdate();
        runUpdate.description("run-updated-" + suffix);
        runUpdate.owner("qa-live-updated");
        runUpdate.status(ExperimentRunStatus.FINISHED);
        runUpdate.state(ExperimentRunState.LIVE);

        ExperimentRun updatedRun = experimentRunClient.updateExperimentRun(createdRun.id(), runUpdate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updatedRun).isNotNull();
        assertThat(updatedRun.id()).isEqualTo(createdRun.id());
        assertThat(updatedRun.description()).isEqualTo("run-updated-" + suffix);

        ExperimentRunList runList = experimentRunClient.getExperimentRuns(
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(runList).isNotNull();
        assertThat(runList.items()).isNotNull();
        assertThat(runList.items().stream().map(ExperimentRun::id)).contains(createdRun.id());

        ArtifactList runArtifactsBefore = experimentRunClient.getExperimentRunArtifacts(
                        createdRun.id(),
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
        assertThat(runArtifactsBefore).isNotNull();
        assertThat(runArtifactsBefore.items()).isNotNull();

        ModelArtifact artifactUpsertRequest = new ModelArtifact();
        artifactUpsertRequest.name("run-artifact-live-" + suffix);
        artifactUpsertRequest.externalId("run-artifact-live-ext-" + suffix);
        artifactUpsertRequest.description("run-artifact-created-" + suffix);
        artifactUpsertRequest.uri("s3://run-artifacts/" + suffix);

        Artifact upsertedArtifact = experimentRunClient.upsertExperimentRunArtifact(createdRun.id(), artifactUpsertRequest)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(upsertedArtifact).isNotNull();
        assertThat(upsertedArtifact.id()).isNotBlank();

        ArtifactList runArtifactsAfter = experimentRunClient.getExperimentRunArtifacts(
                        createdRun.id(),
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
        assertThat(runArtifactsAfter).isNotNull();
        assertThat(runArtifactsAfter.items()).isNotNull();
        assertThat(runArtifactsAfter.items().stream().map(Artifact::id)).contains(upsertedArtifact.id());

        MetricList runMetrics = experimentRunClient.getExperimentRunMetricHistory(
                        createdRun.id(),
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
        assertThat(runMetrics).isNotNull();
        assertThat(runMetrics.items()).isNotNull();

        MetricList allRunsMetrics = experimentRunClient.getExperimentRunsMetricHistory(
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
        assertThat(allRunsMetrics).isNotNull();
        assertThat(allRunsMetrics.items()).isNotNull();
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/experiment_runs?pageSize=1")
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
