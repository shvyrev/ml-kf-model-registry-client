package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.ModelArtifact;
import io.cx.model_registry.dto.artifacts.ModelArtifactCreate;
import io.cx.model_registry.dto.artifacts.ModelArtifactList;
import io.cx.model_registry.dto.artifacts.ModelArtifactUpdate;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * HTTP-клиент для работы с артефактами модели (ModelArtifact) в Model Registry.
 * <p>
 * Реализует все операции, описанные в спецификации OpenAPI для тега ModelRegistryService.
 * Базовый URL и другие настройки задаются через конфигурацию с ключом "model-registry".
 * </p>
 */
@Path("/model_artifacts")
@RegisterRestClient(configKey = "model-registry")
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface ModelArtifactClient {

    /**
     * Получение списка всех артефактов модели с поддержкой фильтрации, сортировки и пагинации.
     *
     * @param filterQuery   SQL-подобный запрос для фильтрации (опционально).
     * @param pageSize      Количество элементов на странице (по умолчанию 100).
     * @param orderBy       Поле для сортировки (по умолчанию ID).
     * @param sortOrder     Порядок сортировки ASC/DESC (по умолчанию ASC).
     * @param nextPageToken Токен для получения следующей страницы (опционально).
     * @return Список артефактов модели с метаданными пагинации.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelArtifactList> getModelArtifacts(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    /**
     * Создание нового артефакта модели.
     *
     * @param modelArtifactCreate DTO с данными для создания артефакта модели.
     * @return Ответ с созданным артефактом модели (HTTP 201).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<Response> createModelArtifact(ModelArtifactCreate modelArtifactCreate);

    /**
     * Получение артефакта модели по его идентификатору.
     *
     * @param modelArtifactId Уникальный идентификатор артефакта модели.
     * @return Артефакт модели.
     */
    @GET
    @Path("/modelartifactId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelArtifact> getModelArtifact(@PathParam("modelartifactId") String modelArtifactId);

    /**
     * Обновление существующего артефакта модели.
     *
     * @param modelArtifactId      Уникальный идентификатор артефакта модели.
     * @param modelArtifactUpdate DTO с обновляемыми полями артефакта модели.
     * @return Обновленный артефакт модели.
     */
    @PATCH
    @Path("/{modelartifactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ModelArtifact> updateModelArtifact(
            @PathParam("modelartifactId") String modelArtifactId,
            ModelArtifactUpdate modelArtifactUpdate
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