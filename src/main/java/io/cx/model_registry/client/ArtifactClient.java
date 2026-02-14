package io.cx.model_registry.client;

import io.cx.model_registry.dto.artifacts.Artifact;
import io.cx.model_registry.dto.artifacts.ArtifactList;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


/**
 * HTTP-клиент для работы с артефактами (Artifact) в odel Registry.
 * <p>
 * Реализует все операции, описанные в спецификации OpenAPI для тега ModelRegistryService.
 * Базовый URL и другие настройки задаются через конфигурацию с ключом "model-registry".
 * </p>
 */
@Path("/artifacts")
@RegisterRestClient(configKey = "model-registry")
@RegisterProvider(RestClientExceptionMapper.class)
@RegisterClientHeaders(HttpClientHeadersFactory.class)
public interface ArtifactClient {


    /**
     * Получение списка всех артефактов с поддержкой фильтрации, сортировки и пагинации.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/artifacts.
     * Поддерживает фильтрацию по типу артефакта и сложные SQL‑подобные условия.
     * </p>
     *
     * @param filterQuery     SQL-подобный запрос для фильтрации (опционально).
     * @param artifactType    Тип артефакта для фильтрации (model-artifact, doc-artifact, dataset-artifact, metric, parameter).
     * @param pageSize        Количество элементов на странице (по умолчанию 100).
     * @param orderBy         Поле для сортировки (CREATE_TIME, LAST_UPDATE_TIME, ID).
     * @param sortOrder       Порядок сортировки ASC/DESC (по умолчанию ASC).
     * @param nextPageToken   Токен для получения следующей страницы (опционально).
     * @return Список артефактов с метаданными пагинации.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ArtifactList> getArtifacts(
            @QueryParam("filterQuery") String filterQuery,
            @QueryParam("artifactType") String artifactType,
            @QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
            @QueryParam("orderBy") @DefaultValue("ID") String orderBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("nextPageToken") String nextPageToken
    );

    /**
     * Создание нового артефакта.
     * <p>
     * Соответствует операции POST /api/model_registry/v1alpha3/artifacts.
     * Тело запроса должно содержать артефакт одного из поддерживаемых типов
     * (ModelArtifactDto, DocArtifactDto, DataSetDto, MetricDto, ParameterDto).
     * </p>
     *
     * @param artifact DTO с данными для создания артефакта.
     * @return Ответ с созданным артефактом (HTTP 201).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = "application/json")
    Uni<Response> createArtifact(Artifact artifact);

    /**
     * Получение артефакта по его идентификатору.
     * <p>
     * Соответствует операции GET /api/model_registry/v1alpha3/artifacts/{id}.
     * </p>
     *
     * @param id Уникальный идентификатор артефакта.
     * @return Артефакт.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Artifact> getArtifact(@PathParam("id") String id);

    /**
     * Обновление существующего артефакта.
     * <p>
     * Соответствует операции PATCH /api/model_registry/v1alpha3/artifacts/{id}.
     * Тело запроса содержит только те поля, которые необходимо изменить.
     * </p>
     *
     * @param id       Уникальный идентификатор артефакта.
     * @param artifact DTO с обновляемыми полями артефакта.
     * @return Обновленный артефакт.
     */
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Artifact> updateArtifact(
            @PathParam("id") String id,
            Artifact artifact
    );
}
