package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.Artifact;
import io.cx.model_registry.dto.artifacts.ModelArtifact;
import io.cx.model_registry.dto.experiments.Experiment;
import io.cx.model_registry.dto.experimentruns.ExperimentRun;
import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironment;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@RegisterRestClient(configKey = "model-registry")
@RegisterProvider(RestClientExceptionMapper.class)
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface SearchClient {

    // Поиск ModelVersion по имени, externalId или parentResourceId
    @GET
    @Path("/model_version")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelVersion> findModelVersion(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("parentResourceId") String parentResourceId
    );


    /**
     * Поиск артефакта модели по имени, внешнему идентификатору или идентификатору родительского ресурса.
     *
     * @param name             Имя артефакта модели (опционально).
     * @param externalId       Внешний идентификатор артефакта модели (опционально).
     * @param parentResourceId Идентификатор родительского ресурса (опционально).
     * @return Найденный артефакт модели.
     */
    @GET
    @Path("/model_artifact")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelArtifact> findModelArtifact(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("parentResourceId") String parentResourceId
    );


    /**
     * Поиск артефакта по имени, внешнему идентификатору или идентификатору родительского ресурса.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/artifact.
     * Возвращает первый артефакт, соответствующий критериям поиска.
     * </p>
     *
     * @param name             Имя артефакта (опционально).
     * @param externalId       Внешний идентификатор артефакта (опционально).
     * @param parentResourceId Идентификатор родительского ресурса (опционально).
     * @return Найденный артефакт или ошибка 404, если артефакт не найден.
     */
    @GET
    @Path("/artifact")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Artifact> findArtifact(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("parentResourceId") String parentResourceId
    );


    /**
     * Поиск эксперимента по имени или внешнему идентификатору.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/experiment.
     * Возвращает первый эксперимент, соответствующий критериям поиска.
     * </p>
     *
     * @param name       Имя эксперимента (опционально).
     * @param externalId Внешний идентификатор эксперимента (опционально).
     * @return Найденный эксперимент или ошибка 404, если эксперимент не найден.
     */
    @GET
    @Path("/experiment")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Experiment> findExperiment(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId
    );

    @GET
    @Path("/experiment_run")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ExperimentRun> findExperimentRun(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("parentResourceId") String parentResourceId
    );

    /**
     * Поиск InferenceService по параметрам name, externalId или parentResourceId.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/inference_service.
     * Возвращает один InferenceService, соответствующий критериям поиска.
     * </p>
     *
     * @param name             Имя InferenceService (опционально).
     * @param externalId       Внешний идентификатор (опционально).
     * @param parentResourceId Идентификатор родительского ресурса (опционально).
     * @return Найденный InferenceService.
     */
    @GET
    @Path("/inference_service")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<InferenceService> findInferenceService(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("parentResourceId") String parentResourceId
    );

    /**
     * Поиск ServingEnvironment по имени или внешнему идентификатору.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/serving_environment.
     * Возвращает один ServingEnvironment, соответствующий критериям поиска.
     * </p>
     *
     * @param name       Имя ServingEnvironment (опционально).
     * @param externalId Внешний идентификатор ServingEnvironment (опционально).
     * @return Найденный ServingEnvironment.
     */
    @GET
    @Path("/serving_environment")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ServingEnvironment> findServingEnvironment(
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId
    );

//    @ClientExceptionMapper
//    static RuntimeException mapException(Response response, @Context RestClientRequestContext requestContext) {
//        String url = requestContext.getUri().toString();
//        String method = requestContext.getHttpMethod();
//        String body = extractBody(response);
//        String message = extractMessage(response, body);
//
//        return new RestClientException(message)
//                .url(url)
//                .method(method)
//                .status(response.getStatus())
//                .headers(response.getHeaders())
//                .body(body);
//    }
}
