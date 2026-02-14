package io.cx.model_registry.client;

import io.cx.model_registry.dto.experiments.Experiment;
import io.cx.model_registry.dto.experiments.ExperimentCreate;
import io.cx.model_registry.dto.experiments.ExperimentList;
import io.cx.model_registry.dto.experiments.ExperimentState;
import io.cx.model_registry.dto.experiments.ExperimentUpdate;
import io.cx.model_registry.dto.experimentruns.ExperimentRun;
import io.cx.model_registry.dto.experimentruns.ExperimentRunCreate;
import io.cx.model_registry.dto.experimentruns.ExperimentRunList;
import io.cx.model_registry.dto.experimentruns.ExperimentRunState;
import io.cx.model_registry.dto.experimentruns.ExperimentRunStatus;
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
class ExperimentClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    ExperimentClient experimentClient;

    @Test
    void experimentClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());
        String experimentName = "experiment-live-test-" + suffix;
        String experimentExternalId = "experiment-live-ext-" + suffix;
        String updatedDescription = "experiment-updated-" + suffix;

        ExperimentCreate createRequest = new ExperimentCreate();
        createRequest.name(experimentName);
        createRequest.externalId(experimentExternalId);
        createRequest.description("experiment-created-" + suffix);
        createRequest.owner("qa-live");

        Experiment created = experimentClient.createExperiment(createRequest)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotBlank();
        assertThat(created.name()).isEqualTo(experimentName);

        Experiment loaded = experimentClient.getExperiment(created.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(loaded).isNotNull();
        assertThat(loaded.id()).isEqualTo(created.id());
        assertThat(loaded.externalId()).isEqualTo(experimentExternalId);

        ExperimentUpdate updateRequest = new ExperimentUpdate();
        updateRequest.description(updatedDescription);
        updateRequest.owner("qa-live-updated");
        updateRequest.state(ExperimentState.LIVE);

        Experiment updated = experimentClient.updateExperiment(created.id(), updateRequest)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.description()).isEqualTo(updatedDescription);

        ExperimentList experiments = experimentClient.getExperiments(
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(experiments).isNotNull();
        assertThat(experiments.items()).isNotNull();
        assertThat(experiments.items().stream().map(Experiment::id)).contains(created.id());

        ExperimentRunList runsBefore = experimentClient.getExperimentExperimentRuns(
                        created.id(),
                        null,
                        100,
                        "ID",
                        "ASC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(runsBefore).isNotNull();
        assertThat(runsBefore.items()).isNotNull();

        String runName = "experiment-run-live-" + suffix;
        ExperimentRunCreate runCreate = new ExperimentRunCreate();
        runCreate.experimentId(created.id());
        runCreate.name(runName);
        runCreate.externalId("experiment-run-live-ext-" + suffix);
        runCreate.owner("qa-live");
        runCreate.status(ExperimentRunStatus.RUNNING);
        runCreate.state(ExperimentRunState.LIVE);

        ExperimentRun createdRun = experimentClient.createExperimentExperimentRun(created.id(), runCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(createdRun).isNotNull();
        assertThat(createdRun.id()).isNotBlank();
        assertThat(createdRun.experimentId()).isEqualTo(created.id());

        ExperimentRunList runsAfter = experimentClient.getExperimentExperimentRuns(
                        created.id(),
                        null,
                        100,
                        "ID",
                        "DESC",
                        null
                )
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(runsAfter).isNotNull();
        assertThat(runsAfter.items()).isNotNull();
        assertThat(runsAfter.items().stream().map(ExperimentRun::id)).contains(createdRun.id());
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(BASE_URL + "/experiments?pageSize=1")
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
