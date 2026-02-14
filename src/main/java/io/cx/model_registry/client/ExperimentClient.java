package io.cx.model_registry.client;

import io.cx.model_registry.dto.experiments.Experiment;
import io.cx.model_registry.dto.experiments.ExperimentCreate;
import io.cx.model_registry.dto.experiments.ExperimentList;
import io.cx.model_registry.dto.experiments.ExperimentUpdate;
import io.cx.model_registry.dto.experimentruns.ExperimentRun;
import io.cx.model_registry.dto.experimentruns.ExperimentRunCreate;
import io.cx.model_registry.dto.experimentruns.ExperimentRunList;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * HTTP-клиент для работы с экспериментами (Experiment) в Model Registry.
 * <p>
 * Реализует все операции, описанные в спецификации OpenAPI для тега ModelRegistryService,
 * связанные с сущностью Experiment.
 * Базовый URL и другие настройки задаются через конфигурацию с ключом "model-registry".
 * </p>
 */
@Path("/experiments")
@RegisterRestClient(configKey = "model-registry")
@RegisterProvider(RestClientExceptionMapper.class)
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface ExperimentClient {

    /**
     * Получение списка всех экспериментов с поддержкой фильтрации, сортировки и пагинации.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/experiments.
     * Поддерживает SQL-подобные условия фильтрации через параметр filterQuery.
     * </p>
     *
     * @param filterQuery   SQL-подобный запрос для фильтрации (опционально).
     * @param pageSize      Количество элементов на странице (по умолчанию 100).
     * @param orderBy       Поле для сортировки (CREATE_TIME, LAST_UPDATE_TIME, ID).
     * @param sortOrder     Порядок сортировки ASC/DESC (по умолчанию ASC).
     * @param nextPageToken Токен для получения следующей страницы (опционально).
     * @return Список экспериментов с метаданными пагинации.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ExperimentList> getExperiments(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    /**
     * Создание нового эксперимента.
     * <p>
     * Соответствует операции POST /api/model_registry/v1alpha3/experiments.
     * Тело запроса должно содержать DTO {@link ExperimentCreate}.
     * </p>
     *
     * @param experiment DTO с данными для создания эксперимента.
     * @return Ответ с созданным экспериментом (HTTP 201).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<Experiment> createExperiment(ExperimentCreate experiment);

    /**
     * Получение эксперимента по его идентификатору.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/experiments/{experimentId}.
     * </p>
     *
     * @param experimentId Уникальный идентификатор эксперимента.
     * @return Эксперимент.
     */
    @GET
    @Path("/{experimentId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Experiment> getExperiment(@PathParam("experimentId") String experimentId);

    /**
     * Обновление существующего эксперимента.
     * <p>
     * Соответствует операции PATCH /api/model_registry/v1alpha3/experiments/{experimentId}.
     * Тело запроса содержит только те поля, которые необходимо изменить.
     * </p>
     *
     * @param experimentId Уникальный идентификатор эксперимента.
     * @param experiment   DTO с обновляемыми полями эксперимента.
     * @return Обновленный эксперимент.
     */
    @PATCH
    @Path("/{experimentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Experiment> updateExperiment(
            @PathParam("experimentId") String experimentId,
            ExperimentUpdate experiment
    );

    @GET
    @Path("/{experimentId}/experiment_runs")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ExperimentRunList> getExperimentExperimentRuns(
            @PathParam("experimentId") String experimentId,
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    @POST
    @Path("/{experimentId}/experiment_runs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<ExperimentRun> createExperimentExperimentRun(
            @PathParam("experimentId") String experimentId,
            ExperimentRunCreate experimentRun
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
