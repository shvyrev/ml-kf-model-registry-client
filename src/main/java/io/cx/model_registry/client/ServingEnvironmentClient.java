package io.cx.model_registry.client;

import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceList;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironment;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentCreate;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentList;
import io.cx.model_registry.dto.servingenvironment.ServingEnvironmentUpdate;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * HTTP-клиент для работы с ServingEnvironment в Model Registry.
 * <p>
 * Реализует все операции, описанные в спецификации OpenAPI для тега ModelRegistryService,
 * связанные с сущностью ServingEnvironment.
 * Базовый URL и другие настройки задаются через конфигурацию с ключом "model-registry".
 * </p>
 */
@Path("/serving_environments")
@RegisterRestClient(configKey = "model-registry")
@RegisterProvider(RestClientExceptionMapper.class)
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface ServingEnvironmentClient {

    /**
     * Получение списка всех ServingEnvironment с поддержкой фильтрации, сортировки и пагинации.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/serving_environments.
     * Поддерживает SQL-подобные условия фильтрации через параметр filterQuery.
     * </p>
     *
     * @param filterQuery   SQL-подобный запрос для фильтрации (опционально).
     * @param pageSize      Количество элементов на странице (по умолчанию 100).
     * @param orderBy       Поле для сортировки (CREATE_TIME, LAST_UPDATE_TIME, ID).
     * @param sortOrder     Порядок сортировки ASC/DESC (по умолчанию ASC).
     * @param nextPageToken Токен для получения следующей страницы (опционально).
     * @return Список ServingEnvironment с метаданными пагинации.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ServingEnvironmentList> getServingEnvironments(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    /**
     * Создание нового ServingEnvironment.
     * <p>
     * Соответствует операции POST /api/model_registry/v1alpha3/serving_environments.
     * Тело запроса должно содержать DTO {@link ServingEnvironmentCreate}.
     * </p>
     *
     * @param servingEnvironment DTO с данными для создания ServingEnvironment.
     * @return Ответ с созданным ServingEnvironment (HTTP 201).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<ServingEnvironment> createServingEnvironment(ServingEnvironmentCreate servingEnvironment);

    /**
     * Получение ServingEnvironment по его идентификатору.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/serving_environments/{servingenvironmentId}.
     * </p>
     *
     * @param servingenvironmentId Уникальный идентификатор ServingEnvironment.
     * @return ServingEnvironment.
     */
    @GET
    @Path("/{servingenvironmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ServingEnvironment> getServingEnvironment(@PathParam("servingenvironmentId") String servingenvironmentId);

    /**
     * Обновление существующего ServingEnvironment.
     * <p>
     * Соответствует операции PATCH /api/model_registry/v1alpha3/serving_environments/{servingenvironmentId}.
     * Тело запроса содержит только те поля, которые необходимо изменить.
     * </p>
     *
     * @param servingenvironmentId Уникальный идентификатор ServingEnvironment.
     * @param servingEnvironment   DTO с обновляемыми полями ServingEnvironment.
     * @return Обновленный ServingEnvironment.
     */
    @PATCH
    @Path("/{servingenvironmentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ServingEnvironment> updateServingEnvironment(
            @PathParam("servingenvironmentId") String servingenvironmentId,
            ServingEnvironmentUpdate servingEnvironment
    );

    /**
     * Получение списка InferenceServices, связанных с ServingEnvironment.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/serving_environments/{servingenvironmentId}/inference_services.
     * Поддерживает фильтрацию, сортировку и пагинацию.
     * </p>
     *
     * @param servingenvironmentId Уникальный идентификатор ServingEnvironment.
     * @param filterQuery          SQL-подобный запрос для фильтрации (опционально).
     * @param name                 Имя InferenceService (опционально).
     * @param externalId           Внешний идентификатор InferenceService (опционально).
     * @param pageSize             Количество элементов на странице (по умолчанию 100).
     * @param orderBy              Поле для сортировки (CREATE_TIME, LAST_UPDATE_TIME, ID).
     * @param sortOrder            Порядок сортировки ASC/DESC (по умолчанию ASC).
     * @param nextPageToken        Токен для получения следующей страницы (опционально).
     * @return Список InferenceService с метаданными пагинации.
     */
    @GET
    @Path("/{servingenvironmentId}/inference_services")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<InferenceServiceList> getEnvironmentInferenceServices(
            @PathParam("servingenvironmentId") String servingenvironmentId,
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    /**
     * Создание нового InferenceService в указанном ServingEnvironment.
     * <p>
     * Соответствует операции POST /api/model_registry/v1alpha3/serving_environments/{servingenvironmentId}/inference_services.
     * Тело запроса должно содержать DTO {@link InferenceServiceCreate}.
     * </p>
     *
     * @param servingenvironmentId Уникальный идентификатор ServingEnvironment.
     * @param inferenceService     DTO с данными для создания InferenceService.
     * @return Ответ с созданным InferenceService (HTTP 201).
     */
    @POST
    @Path("/{servingenvironmentId}/inference_services")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<InferenceService> createEnvironmentInferenceService(
            @PathParam("servingenvironmentId") String servingenvironmentId,
            InferenceServiceCreate inferenceService
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
