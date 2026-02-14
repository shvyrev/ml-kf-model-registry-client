package io.cx.model_registry.client;

import io.cx.model_registry.dto.inferenceservices.InferenceService;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceCreate;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceList;
import io.cx.model_registry.dto.inferenceservices.InferenceServiceUpdate;
import io.cx.model_registry.dto.models.RegisteredModel;
import io.cx.model_registry.dto.versions.ModelVersion;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * HTTP-клиент для работы с InferenceService в Model Registry.
 * <p>
 * Реализует все операции, описанные в спецификации OpenAPI для тега ModelRegistryService,
 * связанные с сущностью InferenceService.
 * Базовый URL и другие настройки задаются через конфигурацию с ключом "model-registry".
 * </p>
 */
@Path("/inference_services")
@RegisterRestClient(configKey = "model-registry")
@RegisterProvider(RestClientExceptionMapper.class)
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface InferenceServiceClient {



    /**
     * Получение списка всех InferenceService с поддержкой фильтрации, сортировки и пагинации.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/inference_services.
     * Поддерживает SQL-подобные условия фильтрации через параметр filterQuery.
     * </p>
     *
     * @param filterQuery   SQL-подобный запрос для фильтрации (опционально).
     * @param pageSize      Количество элементов на странице (по умолчанию 100).
     * @param orderBy       Поле для сортировки (CREATE_TIME, LAST_UPDATE_TIME, ID).
     * @param sortOrder     Порядок сортировки ASC/DESC (по умолчанию ASC).
     * @param nextPageToken Токен для получения следующей страницы (опционально).
     * @return Список InferenceService с метаданными пагинации.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<InferenceServiceList> getInferenceServices(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    /**
     * Создание нового InferenceService.
     * <p>
     * Соответствует операции POST /api/model_registry/v1alpha3/inference_services.
     * Тело запроса должно содержать DTO {@link InferenceServiceCreate}.
     * </p>
     *
     * @param inferenceService DTO с данными для создания InferenceService.
     * @return Ответ с созданным InferenceService (HTTP 200).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<InferenceService> createInferenceService(InferenceServiceCreate inferenceService);

    /**
     * Получение InferenceService по его идентификатору.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/inference_services/{inferenceserviceId}.
     * </p>
     *
     * @param inferenceserviceId Уникальный идентификатор InferenceService.
     * @return InferenceService.
     */
    @GET
    @Path("/{inferenceserviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<InferenceService> getInferenceService(@PathParam("inferenceserviceId") String inferenceserviceId);

    /**
     * Обновление существующего InferenceService.
     * <p>
     * Соответствует операции PATCH /api/model_registry/v1alpha3/inference_services/{inferenceserviceId}.
     * Тело запроса содержит только те поля, которые необходимо изменить.
     * </p>
     *
     * @param inferenceserviceId Уникальный идентификатор InferenceService.
     * @param inferenceService   DTO с обновляемыми полями InferenceService.
     * @return Обновленный InferenceService.
     */
    @PATCH
    @Path("/{inferenceserviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<InferenceService> updateInferenceService(
            @PathParam("inferenceserviceId") String inferenceserviceId,
            InferenceServiceUpdate inferenceService
    );

    /**
     * Получение RegisteredModel, связанного с InferenceService.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/inference_services/{inferenceserviceId}/model.
     * </p>
     *
     * @param inferenceserviceId Уникальный идентификатор InferenceService.
     * @return Зарегистрированная модель (RegisteredModel).
     */
    @GET
    @Path("/{inferenceserviceId}/model")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<RegisteredModel> getInferenceServiceModel(@PathParam("inferenceserviceId") String inferenceserviceId);

    /**
     * Получение текущей ModelVersion, связанной с InferenceService.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/inference_services/{inferenceserviceId}/version.
     * </p>
     *
     * @param inferenceserviceId Уникальный идентификатор InferenceService.
     * @return Версия модели (ModelVersion).
     */
    @GET
    @Path("/{inferenceserviceId}/version")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelVersion> getInferenceServiceVersion(@PathParam("inferenceserviceId") String inferenceserviceId);



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
