package io.cx.model_registry.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ModelRegistryWorkflowResourceLiveTest {

    private static final String MODEL_REGISTRY_BASE = "http://localhost:8089/api/model_registry/v1alpha3";

    @Test
    void createModelWithVersionWorkflow_shouldCreateBothEntities() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + MODEL_REGISTRY_BASE);

        String suffix = String.valueOf(System.currentTimeMillis());

        Response response = given()
                .contentType("application/json")
                .body("""
                        {
                          "model": {
                            "name": "wf-model-%s",
                            "externalId": "wf-model-ext-%s",
                            "description": "wf-model-created-%s"
                          },
                          "version": {
                            "name": "wf-version-%s",
                            "externalId": "wf-version-ext-%s",
                            "description": "wf-version-created-%s"
                          }
                        }
                        """.formatted(suffix, suffix, suffix, suffix, suffix, suffix))
                .when()
                .post("/api/v1/workflows/model-with-version")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String modelId = response.path("model.id");
        String modelName = response.path("model.name");
        String versionId = response.path("version.id");
        String versionName = response.path("version.name");
        String versionModelId = response.path("version.registeredModelId");

        assertThat(modelId).isNotBlank();
        assertThat(modelName).isEqualTo("wf-model-" + suffix);
        assertThat(versionId).isNotBlank();
        assertThat(versionName).isEqualTo("wf-version-" + suffix);
        assertThat(versionModelId).isEqualTo(modelId);
    }

    @Test
    void deployModelVersionWorkflow_shouldCreateEnvironmentInferenceServiceAndServe() {
        Assumptions.assumeTrue(isModelRegistryAvailable(),
                "Model Registry is not available at " + MODEL_REGISTRY_BASE);

        String suffix = String.valueOf(System.currentTimeMillis());

        Response seedResponse = given()
                .contentType("application/json")
                .body("""
                        {
                          "model": {
                            "name": "wf-deploy-model-%s",
                            "externalId": "wf-deploy-model-ext-%s",
                            "description": "wf-deploy-model-created-%s"
                          },
                          "version": {
                            "name": "wf-deploy-version-%s",
                            "externalId": "wf-deploy-version-ext-%s",
                            "description": "wf-deploy-version-created-%s"
                          }
                        }
                        """.formatted(suffix, suffix, suffix, suffix, suffix, suffix))
                .when()
                .post("/api/v1/workflows/model-with-version")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String modelId = seedResponse.path("model.id");
        String modelVersionId = seedResponse.path("version.id");

        Response deployResponse = given()
                .contentType("application/json")
                .body("""
                        {
                          "servingEnvironment": {
                            "name": "wf-deploy-env-%s",
                            "externalId": "wf-deploy-env-ext-%s",
                            "description": "wf-deploy-env-created-%s"
                          },
                          "inferenceService": {
                            "name": "wf-deploy-is-%s",
                            "externalId": "wf-deploy-is-ext-%s",
                            "description": "wf-deploy-is-created-%s",
                            "registeredModelId": "%s",
                            "modelVersionId": "%s",
                            "runtime": "kserve"
                          },
                          "serve": {
                            "name": "wf-deploy-serve-%s",
                            "externalId": "wf-deploy-serve-ext-%s",
                            "description": "wf-deploy-serve-created-%s",
                            "modelVersionId": "%s"
                          }
                        }
                        """.formatted(
                        suffix, suffix, suffix,
                        suffix, suffix, suffix,
                        modelId,
                        modelVersionId,
                        suffix, suffix, suffix,
                        modelVersionId
                ))
                .when()
                .post("/api/v1/workflows/deploy-model-version")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String envId = deployResponse.path("servingEnvironment.id");
        String envName = deployResponse.path("servingEnvironment.name");
        String inferenceServiceId = deployResponse.path("inferenceService.id");
        String inferenceServiceModelId = deployResponse.path("inferenceService.registeredModelId");
        String serveId = deployResponse.path("serve.id");
        String serveVersionId = deployResponse.path("serve.modelVersionId");

        assertThat(envId).isNotBlank();
        assertThat(envName).isEqualTo("wf-deploy-env-" + suffix);
        assertThat(inferenceServiceId).isNotBlank();
        assertThat(inferenceServiceModelId).isEqualTo(modelId);
        assertThat(serveId).isNotBlank();
        assertThat(serveVersionId).isEqualTo(modelVersionId);
    }

    private boolean isModelRegistryAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create(MODEL_REGISTRY_BASE + "/registered_models?pageSize=1")
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
