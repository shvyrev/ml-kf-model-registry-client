package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.Artifact;
import io.cx.model_registry.dto.artifacts.ModelArtifact;
import io.cx.model_registry.dto.artifacts.ModelArtifactCreate;
import io.cx.model_registry.dto.experiments.Experiment;
import io.cx.model_registry.dto.experiments.ExperimentCreate;
import io.cx.model_registry.dto.experimentruns.ExperimentRun;
import io.cx.model_registry.dto.experimentruns.ExperimentRunCreate;
import io.cx.model_registry.dto.experimentruns.ExperimentRunState;
import io.cx.model_registry.dto.experimentruns.ExperimentRunStatus;
import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceState;
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
class SearchClientLiveTest {

    private static final String BASE_URL = "http://localhost:8089/api/model_registry/v1alpha3";

    @Inject
    @RestClient
    SearchClient searchClient;

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
    ExperimentClient experimentClient;

    @Inject
    @RestClient
    ExperimentRunClient experimentRunClient;

    @Inject
    @RestClient
    ArtifactClient artifactClient;

    @Inject
    @RestClient
    ModelArtifactClient modelArtifactClient;

    @Test
    void searchClient_shouldSupportAllMethodsAgainstLiveModelRegistry() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + BASE_URL);

        String suffix = String.valueOf(System.currentTimeMillis());

        RegisteredModelCreate modelCreate = new RegisteredModelCreate();
        modelCreate.name("search-model-" + suffix);
        modelCreate.externalId("search-model-ext-" + suffix);
        modelCreate.description("search-model-created-" + suffix);
        Response modelResponse = modelClient.createRegisteredModel(modelCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        RegisteredModel registeredModel = modelResponse.readEntity(RegisteredModel.class);
        assertThat(registeredModel).isNotNull();

        ModelVersionCreate versionCreate = new ModelVersionCreate();
        versionCreate.name("search-version-" + suffix);
        versionCreate.externalId("search-version-ext-" + suffix);
        versionCreate.description("search-version-created-" + suffix);
        versionCreate.registeredModelId(registeredModel.id());
        ModelVersion modelVersion = versionClient.createModelVersion(versionCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(modelVersion).isNotNull();

        ServingEnvironmentCreate envCreate = new ServingEnvironmentCreate();
        envCreate.name("search-env-" + suffix);
        envCreate.externalId("search-env-ext-" + suffix);
        envCreate.description("search-env-created-" + suffix);
        ServingEnvironment servingEnvironment = servingEnvironmentClient.createServingEnvironment(envCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(servingEnvironment).isNotNull();

        InferenceServiceCreate serviceCreate = new InferenceServiceCreate();
        serviceCreate.name("search-is-" + suffix);
        serviceCreate.externalId("search-is-ext-" + suffix);
        serviceCreate.description("search-is-created-" + suffix);
        serviceCreate.registeredModelId(registeredModel.id());
        serviceCreate.servingEnvironmentId(servingEnvironment.id());
        serviceCreate.modelVersionId(modelVersion.id());
        serviceCreate.runtime("kserve");
        serviceCreate.desiredState(InferenceServiceState.DEPLOYED);
        InferenceService inferenceService = inferenceServiceClient.createInferenceService(serviceCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(inferenceService).isNotNull();

        ExperimentCreate experimentCreate = new ExperimentCreate();
        experimentCreate.name("search-exp-" + suffix);
        experimentCreate.externalId("search-exp-ext-" + suffix);
        experimentCreate.description("search-exp-created-" + suffix);
        Experiment experiment = experimentClient.createExperiment(experimentCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(experiment).isNotNull();

        ExperimentRunCreate runCreate = new ExperimentRunCreate();
        runCreate.experimentId(experiment.id());
        runCreate.name("search-run-" + suffix);
        runCreate.externalId("search-run-ext-" + suffix);
        runCreate.description("search-run-created-" + suffix);
        runCreate.status(ExperimentRunStatus.RUNNING);
        runCreate.state(ExperimentRunState.LIVE);
        ExperimentRun experimentRun = experimentRunClient.createExperimentRun(runCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(experimentRun).isNotNull();

        ModelArtifact genericArtifact = new ModelArtifact();
        genericArtifact.name("search-artifact-" + suffix);
        genericArtifact.externalId("search-artifact-ext-" + suffix);
        genericArtifact.description("search-artifact-created-" + suffix);
        genericArtifact.uri("s3://search-artifacts/" + suffix);
        Response artifactResponse = artifactClient.createArtifact(genericArtifact)
                .await()
                .atMost(Duration.ofSeconds(30));
        Artifact artifact = artifactResponse.readEntity(Artifact.class);
        assertThat(artifact).isNotNull();

        ModelArtifactCreate modelArtifactCreate = new ModelArtifactCreate();
        modelArtifactCreate.name("search-model-artifact-" + suffix);
        modelArtifactCreate.externalId("search-model-artifact-ext-" + suffix);
        modelArtifactCreate.description("search-model-artifact-created-" + suffix);
        modelArtifactCreate.uri("s3://search-model-artifacts/" + suffix);
        Response modelArtifactResponse = modelArtifactClient.createModelArtifact(modelArtifactCreate)
                .await()
                .atMost(Duration.ofSeconds(30));
        ModelArtifact modelArtifact = modelArtifactResponse.readEntity(ModelArtifact.class);
        assertThat(modelArtifact).isNotNull();

        ModelVersion foundVersion = searchClient.findModelVersion(null, versionCreate.externalId(), registeredModel.id());
        assertThat(foundVersion).isNotNull();
        assertThat(foundVersion.id()).isEqualTo(modelVersion.id());

        ModelArtifact foundModelArtifact = searchClient.findModelArtifact(null, modelArtifactCreate.externalId(), null)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(foundModelArtifact).isNotNull();
        assertThat(foundModelArtifact.id()).isEqualTo(modelArtifact.id());

        Artifact foundArtifact = searchClient.findArtifact(null, genericArtifact.externalId(), null)
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(foundArtifact).isNotNull();
        assertThat(foundArtifact.id()).isEqualTo(artifact.id());

        Experiment foundExperiment = searchClient.findExperiment(null, experimentCreate.externalId())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(foundExperiment).isNotNull();
        assertThat(foundExperiment.id()).isEqualTo(experiment.id());

        ExperimentRun foundRun = searchClient.findExperimentRun(null, runCreate.externalId(), experiment.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(foundRun).isNotNull();
        assertThat(foundRun.id()).isEqualTo(experimentRun.id());

        InferenceService foundService = searchClient.findInferenceService(null, serviceCreate.externalId(), servingEnvironment.id())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(foundService).isNotNull();
        assertThat(foundService.id()).isEqualTo(inferenceService.id());

        ServingEnvironment foundEnv = searchClient.findServingEnvironment(null, envCreate.externalId())
                .await()
                .atMost(Duration.ofSeconds(30));
        assertThat(foundEnv).isNotNull();
        assertThat(foundEnv.id()).isEqualTo(servingEnvironment.id());
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
