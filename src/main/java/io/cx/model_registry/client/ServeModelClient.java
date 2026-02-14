package io.cx.model_registry.client;

import io.cx.model_registry.dto.servemodel.ServeModel;
import io.cx.model_registry.dto.servemodel.ServeModelCreate;
import io.cx.model_registry.dto.servemodel.ServeModelList;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * HTTP-клиент для работы с действиями обслуживания модели (ServeModel) в Model Registry.
 * <p>
 * Реализует все операции, описанные в спецификации OpenAPI для тега ModelRegistryService,
 * связанные с сущностью ServeModel как подресурсом InferenceService.
 * Базовый URL и другие настройки задаются через конфигурацию с ключом "model-registry".
 * </p>
 */
@Path("/inference_services/{inferenceserviceId}/serves")
@RegisterRestClient(configKey = "model-registry")
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface ServeModelClient {

    /**
     * Получение списка всех ServeModel для указанного InferenceService с поддержкой фильтрации, сортировки и пагинации.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/inference_services/{inferenceserviceId}/serves.
     * Поддерживает SQL-подобные условия фильтрации через параметр filterQuery.
     * </p>
     *
     * @param inferenceserviceId Уникальный идентификатор InferenceService.
     * @param filterQuery        SQL-подобный запрос для фильтрации (опционально).
     * @param name               Имя ServeModel для поиска (опционально).
     * @param externalId         Внешний идентификатор для поиска (опционально).
     * @param pageSize           Количество элементов на странице (по умолчанию 100).
     * @param orderBy            Поле для сортировки (CREATE_TIME, LAST_UPDATE_TIME, ID).
     * @param sortOrder          Порядок сортировки ASC/DESC (по умолчанию ASC).
     * @param nextPageToken      Токен для получения следующей страницы (опционально).
     * @return Список ServeModel с метаданными пагинации.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ServeModelList> getServeModels(
            @PathParam("inferenceserviceId") String inferenceserviceId,
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("name") String name,
            @QueryParam("externalId") String externalId,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    /**
     * Создание нового действия обслуживания модели (ServeModel) для указанного InferenceService.
     * <p>
     * Соответствует операции POST /api/model_registry/v1alpha3/inference_services/{inferenceserviceId}/serves.
     * Тело запроса должно содержать DTO {@link ServeModelCreate}.
     * </p>
     *
     * @param inferenceserviceId Уникальный идентификатор InferenceService.
     * @param serveModel         DTO с данными для создания ServeModel.
     * @return Ответ с созданным ServeModel (HTTP 201).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<ServeModel> createServeModel(
            @PathParam("inferenceserviceId") String inferenceserviceId,
            ServeModelCreate serveModel
    );

    /**
     * Преобразование ответа сервера в исключение.
     * <p>
     * Используется для унифицированной обработки ошибок HTTP.
     * </p>
     *
     * @param response Ответ сервера.
     * @return Исключение с информацией об ошибке.
     */
    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        return new IllegalStateException(response.getStatusInfo().toString());
    }

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